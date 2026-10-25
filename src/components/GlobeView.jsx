
import { useEffect, useRef } from 'react'
import * as Cesium from 'cesium'
import { Plus, Minus, LocateFixed } from 'lucide-react'
import 'cesium/Build/Cesium/Widgets/widgets.css'
import { CITIES, STATUS_COLORS } from '../data/drones'
import { TICK_MS } from '../services/telemetryClient'

const token = import.meta.env.VITE_CESIUM_ION_TOKEN

Cesium.Ion.defaultAccessToken = token

const HOME = Cesium.Cartesian3.fromDegrees(79.5, 19.5, 8_000_000)

// Reusable Cartesian3 object for smooth drone interpolation
const scratch = new Cesium.Cartesian3()

export default function GlobeView({ drones, selectedId, onSelect }) {
  const containerRef = useRef(null)
  const viewerRef = useRef(null)
  const pointsCollectionRef = useRef(null)
  const pointsRef = useRef(new Map())
  const lastTickRef = useRef(0)
  const lastFocusedIdRef = useRef(null)
  const selectedRef = useRef(selectedId)
  const onSelectRef = useRef(onSelect)

  useEffect(() => {
    selectedRef.current = selectedId
    onSelectRef.current = onSelect
  }, [selectedId, onSelect])

  // 1. Initialize the globe, markers, and labels.
  useEffect(() => {
    if (!containerRef.current) return
    const pointEntries = pointsRef.current

    const baseLayer = token
      ? Cesium.ImageryLayer.fromWorldImagery()
      : new Cesium.ImageryLayer(
          new Cesium.OpenStreetMapImageryProvider({
            url: 'https://tile.openstreetmap.org/',
          }),
        )

    const viewer = new Cesium.Viewer(containerRef.current, {
      baseLayer,
      baseLayerPicker: false,
      geocoder: false,
      homeButton: false,
      sceneModePicker: false,
      navigationHelpButton: false,
      animation: false,
      timeline: false,
      fullscreenButton: false,
      infoBox: false,
      selectionIndicator: false,
    })

    viewer.scene.globe.baseColor =
      Cesium.Color.fromCssColorString('#0a2747')

    const points = viewer.scene.primitives.add(
      new Cesium.PointPrimitiveCollection(),
    )
    pointsCollectionRef.current = points
    lastTickRef.current = performance.now()

    // Selected drone ring
    const ring = points.add({
      position: Cesium.Cartesian3.ZERO,
      show: false,
      color: Cesium.Color.TRANSPARENT,
      pixelSize: 24,
      outlineColor: Cesium.Color.fromCssColorString('#19d3c5'),
      outlineWidth: 2,
    })

    // City labels
    const labels = viewer.scene.primitives.add(
      new Cesium.LabelCollection(),
    )

    CITIES.forEach((city) => {
      labels.add({
        position: Cesium.Cartesian3.fromDegrees(
          city.lon,
          city.lat,
          0,
        ),
        text: city.name,
        font: '14px sans-serif',
        showBackground: true,
        backgroundColor: Cesium.Color.fromCssColorString(
          '#040c16',
        ).withAlpha(0.85),
        pixelOffset: new Cesium.Cartesian2(0, -26),
      })
    })

    // City center markers
    CITIES.forEach((city) => {
      points.add({
        position: Cesium.Cartesian3.fromDegrees(
          city.lon,
          city.lat,
          0,
        ),
        color: Cesium.Color.fromCssColorString('#ffd166'),
        pixelSize: 7,
      })
    })

    // Animate drone positions on every rendered frame
    const removePreRender =
      viewer.scene.preRender.addEventListener(() => {
        const progress = Math.min(
          1,
          (performance.now() - lastTickRef.current) / TICK_MS,
        )

        pointEntries.forEach((entry) => {
          Cesium.Cartesian3.lerp(
            entry.from,
            entry.to,
            progress,
            scratch,
          )

          // Each drone needs its own position object.
          // Copy scratch to avoid all markers sharing one position.
          entry.point.position = Cesium.Cartesian3.clone(
            scratch,
            entry.point.position,
          )
        })

        const selected = pointsRef.current.get(
          selectedRef.current,
        )

        if (selected) {
          ring.position = selected.point.position
          ring.show = true
        } else {
          ring.show = false
        }
      })

    // Handle drone marker clicks
    const handler = new Cesium.ScreenSpaceEventHandler(
      viewer.scene.canvas,
    )

    handler.setInputAction((click) => {
      const picked = viewer.scene.pick(click.position)

      if (picked && typeof picked.id === 'string') {
        onSelectRef.current(picked.id)
      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

    viewerRef.current = viewer

    viewer.camera.flyTo({
      destination: HOME,
      duration: 2.5,
    })

    return () => {
      removePreRender()
      handler.destroy()
      viewer.destroy()
      pointEntries.clear()
      pointsCollectionRef.current = null
      viewerRef.current = null
    }
  }, [])

  // 2. Keep markers synchronized with the backend fleet roster and telemetry.
  useEffect(() => {
    const points = pointsCollectionRef.current
    if (!points) return

    const droneIds = new Set(drones.map((drone) => drone.id))
    drones.forEach((drone) => {
      const entry = pointsRef.current.get(drone.id)
      const target = Cesium.Cartesian3.fromDegrees(
        drone.lon,
        drone.lat,
        drone.alt,
      )

      if (entry) {
        entry.from = Cesium.Cartesian3.clone(entry.point.position)
        entry.to = target
        entry.point.color = Cesium.Color.fromCssColorString(
          STATUS_COLORS[drone.status] ?? '#19d3c5',
        )
        return
      }

      const point = points.add({
        id: drone.id,
        position: target,
        color: Cesium.Color.fromCssColorString(
          STATUS_COLORS[drone.status] ?? '#19d3c5',
        ),
        pixelSize: 11,
        outlineColor: Cesium.Color.fromCssColorString('#040c16'),
        outlineWidth: 1.5,
      })
      pointsRef.current.set(drone.id, {
        point,
        from: Cesium.Cartesian3.clone(target),
        to: Cesium.Cartesian3.clone(target),
      })
    })

    pointsRef.current.forEach((entry, id) => {
      if (!droneIds.has(id)) {
        points.remove(entry.point)
        pointsRef.current.delete(id)
      }
    })

    lastTickRef.current = performance.now()
  }, [drones])

  useEffect(() => {
    const drone = drones.find((item) => item.id === selectedId)
    const camera = viewerRef.current?.camera
    if (!drone || !camera || lastFocusedIdRef.current === selectedId) return

    lastFocusedIdRef.current = selectedId
    camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(
        drone.lon,
        drone.lat,
        Math.max(drone.alt + 120_000, 180_000),
      ),
      duration: 1.5,
    })
  }, [selectedId, drones])

  // 3. Zoom controls
  const zoom = (direction) => {
    const camera = viewerRef.current?.camera

    if (!camera) return

    const height = camera.positionCartographic.height

    if (direction > 0) {
      camera.zoomIn(height * 0.4)
    } else {
      camera.zoomOut(height * 0.4)
    }
  }

  const centerOnIndia = () => {
    viewerRef.current?.camera.flyTo({
      destination: HOME,
      duration: 1.5,
    })
  }

  return (
    <div
      style={{
        position: 'relative',
        width: '100%',
        height: '100%',
      }}
    >
      <div
        ref={containerRef}
        style={{ width: '100%', height: '100%' }}
      />

      <div className="map-controls">
        <div className="ctrl-group">
          <button
            aria-label="Zoom in"
            onClick={() => zoom(1)}
          >
            <Plus size={18} />
          </button>

          <button
            aria-label="Zoom out"
            onClick={() => zoom(-1)}
          >
            <Minus size={18} />
          </button>
        </div>

        <div className="ctrl-group">
          <button
            aria-label="Center on India"
            onClick={centerOnIndia}
          >
            <LocateFixed size={18} />
          </button>
        </div>
      </div>

      <div className="legend">
        <span>
          <i className="dot active" />
          Active
        </span>

        <span>
          <i className="dot idle" />
          Idle
        </span>

        <span>
          <i className="dot offline" />
          Offline
        </span>
      </div>
    </div>
  )
}
