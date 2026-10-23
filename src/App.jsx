import { useCallback, useEffect, useMemo, useState } from 'react'
import { House, Target, Plane, Globe, ChartColumn, Settings } from 'lucide-react'
import GlobeView from './components/GlobeView'
import MissionDag from './components/MissionDag'
import { DRONES } from './data/drones'
import {
  createDrone,
  createMission,
  deleteMission,
  executeMission,
  getBackendOverview,
} from './services/backendClient'
import { subscribe } from './services/telemetryClient'
import './App.css'

const NAV = [
  ['Dashboard', House, 'fleet-overview'],
  ['Missions', Target, 'mission-list'],
  ['Drones', Plane, 'drone-roster'],
  // ['Map View', Globe, 'map-view'],
  ['Analytics', ChartColumn, 'backend-overview'],
  // ['Settings', Settings, 'backend-overview'],
]

const coord = (lat, lon) =>
  `${Math.abs(lat).toFixed(4)}° ${lat >= 0 ? 'N' : 'S'}, ${Math.abs(lon).toFixed(4)}° ${lon >= 0 ? 'E' : 'W'}`

const displayStatus = (status) => {
  switch (status?.toUpperCase()) {
    case 'AVAILABLE': return 'idle'
    case 'IN_PROGRESS':
    case 'RUNNING': return 'active'
    case 'OFFLINE': return 'offline'
    default: return 'idle'
  }
}

function latestTelemetry(telemetry, droneId) {
  return telemetry
    .filter((item) => String(item.droneId) === String(droneId))
    .sort((a, b) => Date.parse(b.recordedAt) - Date.parse(a.recordedAt))[0]
}

export default function App() {
  const [nav, setNav] = useState('Dashboard')
  const [selectedId, setSelectedId] = useState(null)
  const [simulation, setSimulation] = useState(DRONES)
  const [backend, setBackend] = useState({
    status: 'loading',
    drones: [],
    missions: [],
    telemetry: [],
    workflowSignals: [],
  })
  const [busy, setBusy] = useState(false)
  const [actionError, setActionError] = useState('')
  const [droneForm, setDroneForm] = useState({ name: '', model: '' })
  const [missionForm, setMissionForm] = useState({
    droneId: '',
    missionName: '',
    destination: '',
  })

  const refreshBackend = useCallback(async () => {
    try {
      const overview = await getBackendOverview()
      setBackend({ status: 'online', ...overview })
      setSelectedId((current) =>
        overview.drones.some((drone) => String(drone.id) === current)
          ? current
          : overview.drones.length
            ? String(overview.drones[0].id)
            : null,
      )
      setMissionForm((current) => {
        const availableDrones = overview.drones.filter(
          (drone) => drone.status?.toUpperCase() === 'AVAILABLE',
        )
        const selectedIsAvailable = availableDrones.some(
          (drone) => String(drone.id) === current.droneId,
        )
        return {
          ...current,
          droneId: selectedIsAvailable
            ? current.droneId
            : String(availableDrones[0]?.id || ''),
        }
      })
    } catch (error) {
      setBackend((current) => ({
        ...current,
        status: 'offline',
        error: error instanceof Error ? error.message : 'Unable to reach the backend API',
      }))
    }
  }, [])

  useEffect(() => subscribe(setSimulation), [])

  useEffect(() => {
    const initialRefresh = setTimeout(refreshBackend, 0)
    const timer = setInterval(refreshBackend, 4000)
    return () => {
      clearTimeout(initialRefresh)
      clearInterval(timer)
    }
  }, [refreshBackend])

  const drones = useMemo(() => backend.drones.map((drone) => {
    const sample = simulation[(Number(drone.id) - 1 + simulation.length) % simulation.length]
    const telemetry = latestTelemetry(backend.telemetry, drone.id)
    const mission = backend.missions.find(
      (item) => String(item.drone?.id) === String(drone.id) &&
        ['PENDING', 'RUNNING', 'IN_PROGRESS'].includes(item.status?.toUpperCase()),
    )

    return {
      ...sample,
      id: String(drone.id),
      backendId: drone.id,
      name: drone.name,
      model: drone.model,
      status: displayStatus(drone.status),
      lat: telemetry?.latitude ?? sample.lat,
      lon: telemetry?.longitude ?? sample.lon,
      alt: telemetry?.altitude ?? sample.alt,
      battery: telemetry?.batteryLevel ?? sample.battery,
      speed: sample.speed,
      heading: sample.heading,
      mission: mission?.missionName || 'No active mission',
      missionId: mission?.id,
      telemetryIsLive: Boolean(telemetry),
      dest: sample.dest,
    }
  }), [backend.drones, backend.missions, backend.telemetry, simulation])

  const selected = drones.find((drone) => drone.id === selectedId)
  const selectedMission = selected
    ? backend.missions
      .filter((mission) => String(mission.drone?.id) === selected.id)
      .sort((a, b) => Number(b.id) - Number(a.id))[0]
    : null
  const availableDrones = backend.drones.filter(
    (drone) => drone.status?.toUpperCase() === 'AVAILABLE',
  )
  const count = (status) => drones.filter((drone) => drone.status === status).length

  const performAction = async (action) => {
    setBusy(true)
    setActionError('')
    try {
      await action()
      await refreshBackend()
    } catch (error) {
      setActionError(error instanceof Error ? error.message : 'The request failed')
    } finally {
      setBusy(false)
    }
  }

  const submitDrone = (event) => {
    event.preventDefault()
    performAction(async () => {
      await createDrone({ ...droneForm, status: 'AVAILABLE' })
      setDroneForm({ name: '', model: '' })
    })
  }

  const submitMission = (event) => {
    event.preventDefault()
    performAction(async () => {
      await createMission({
        droneId: Number(missionForm.droneId),
        missionName: missionForm.missionName,
        destination: missionForm.destination,
      })
      setMissionForm((current) => ({
        ...current,
        missionName: '',
        destination: '',
      }))
    })
  }

  return (
    <div className="app">
      <nav className="sidebar">
        <div className="brand">
          <Plane size={24} />
          <div>
            <div className="brand-name">Arrow Saga</div>
            <div className="brand-sub">Drone Fleet Control</div>
          </div>
        </div>

        {NAV.map(([label, Icon, target]) => (
          <button
            key={label}
            className={nav === label ? 'on' : ''}
            onClick={() => {
              setNav(label)
              if (target === 'map-view') {
                document.getElementById(target)?.scrollIntoView({ behavior: 'smooth' })
              } else {
                document.getElementById(target)?.scrollIntoView({ behavior: 'smooth' })
              }
            }}
          >
            <Icon size={20} />
            {label}
          </button>
        ))}

        <div className="side-foot">
          <div><i className={`dot ${backend.status === 'online' ? 'live' : 'offline'}`} />API {backend.status}</div>
          <div className="muted">Backend refreshes every 4 seconds</div>
        </div>
      </nav>

      <main className="map-area" id="map-view">
        <GlobeView drones={drones} selectedId={selectedId} onSelect={setSelectedId} />
      </main>

      <aside className="panel">
        <div className="panel-head">
          <span className="panel-icon"><Plane size={20} /></span>
          <h2>Fleet control</h2>
          <span className="live-text"><i className={`dot ${backend.status === 'online' ? 'live' : 'offline'}`} />{backend.status === 'online' ? 'API LIVE' : 'API OFFLINE'}</span>
        </div>

        {backend.status === 'offline' && (
          <section className="api-error" aria-live="polite">
            <strong>Backend unavailable</strong>
            <p>{backend.error}</p>
            <button className="inject" onClick={refreshBackend}>Retry connection</button>
          </section>
        )}
        {actionError && <section className="api-error" role="alert">{actionError}</section>}

        <section id="fleet-overview">
          <div className="row">
            <h3>Fleet overview</h3>
            <span className="tiny">{drones.length} REGISTERED</span>
          </div>
          <div className="stats">
            <div><span>Active</span><b className="teal">{count('active')}</b></div>
            <div><span>Available</span><b className="amber">{count('idle')}</b></div>
            <div><span>Offline</span><b>{count('offline')}</b></div>
          </div>
        </section>

        <section id="backend-overview" aria-live="polite">
          <div className="row">
            <h3>Backend records</h3>
            <span className="tiny">{backend.status.toUpperCase()}</span>
          </div>
          <div className="stats">
            <div><span>Drones</span><b>{backend.drones.length}</b></div>
            <div><span>Missions</span><b>{backend.missions.length}</b></div>
            <div><span>Telemetry</span><b>{backend.telemetry.length}</b></div>
          </div>
        </section>

        <section id="drone-form">
          <h3>Register drone</h3>
          <form className="api-form" onSubmit={submitDrone}>
            <input
              aria-label="Drone name"
              placeholder="Drone name"
              required
              value={droneForm.name}
              onChange={(event) => setDroneForm({ ...droneForm, name: event.target.value })}
            />
            <input
              aria-label="Drone model"
              placeholder="Model"
              required
              value={droneForm.model}
              onChange={(event) => setDroneForm({ ...droneForm, model: event.target.value })}
            />
            <button className="inject" disabled={busy || backend.status !== 'online'}>Register drone</button>
          </form>
        </section>

        <section id="mission-form">
          <h3>Create mission</h3>
          <form className="api-form" onSubmit={submitMission}>
            <select
              aria-label="Available drone"
              required
              value={missionForm.droneId}
              onChange={(event) => setMissionForm({ ...missionForm, droneId: event.target.value })}
            >
              <option value="">Choose available drone</option>
              {availableDrones.map((drone) => (
                <option key={drone.id} value={drone.id}>{drone.name} · {drone.model}</option>
              ))}
            </select>
            <input
              aria-label="Mission name"
              placeholder="Mission name"
              required
              value={missionForm.missionName}
              onChange={(event) => setMissionForm({ ...missionForm, missionName: event.target.value })}
            />
            <input
              aria-label="Destination"
              placeholder="Destination"
              required
              value={missionForm.destination}
              onChange={(event) => setMissionForm({ ...missionForm, destination: event.target.value })}
            />
            <button className="inject" disabled={busy || !availableDrones.length || backend.status !== 'online'}>
              Create mission
            </button>
          </form>
          {!availableDrones.length && <p className="tiny">Register or free a drone before creating a mission.</p>}
        </section>

        {selected ? (
          <>
            <section id="mission-list">
              <div className="row">
                <h3>Selected aircraft</h3>
                <span className={`badge ${selected.status}`}>{selected.status.toUpperCase()}</span>
              </div>
              <div className="drone-name">{selected.name}</div>
              <div className="tiny">{selected.model} · {selected.telemetryIsLive ? 'LIVE TELEMETRY' : 'SIMULATED POSITION'}</div>
              <div className="grid2">
                <div><span>Altitude</span><b>{selected.alt} m</b></div>
                <div><span>Speed</span><b>{selected.speed} km/h*</b></div>
                <div><span>Heading</span><b>{selected.heading}°*</b></div>
                <div><span>Battery</span><b>{Math.round(selected.battery)}%</b></div>
              </div>
              <div className="mission">
                <div><span>Current position</span><b>{coord(selected.lat, selected.lon)}</b></div>
                <div className="right"><span>Current mission</span><b>{selected.mission}</b></div>
              </div>
              <p className="tiny">* Speed and heading remain simulated until provided by telemetry.</p>
            </section>

            <MissionDag
              mission={selectedMission}
              busy={busy}
              onExecute={(missionId) => performAction(() => executeMission(missionId))}
            />
          </>
        ) : (
          <section>
            <h3>No registered drones</h3>
            <p className="tiny">Register a drone to see it on the map and create missions.</p>
          </section>
        )}

        <section>
          <div className="row">
            <h3>Missions</h3>
            <span className="tiny">{backend.missions.length}</span>
          </div>
          <ul className="mission-list">
            {backend.missions.map((mission) => (
              <li key={mission.id}>
                <div>
                  <strong>{mission.missionName}</strong>
                  <span className="tiny">{mission.drone?.name || 'Unassigned'} · {mission.destination}</span>
                </div>
                <div className="mission-actions">
                  <span className={`wf-badge ${mission.status}`}>{mission.status}</span>
                  {mission.status?.toUpperCase() === 'PENDING' && (
                    <>
                      <button className="tiny-button" disabled={busy} onClick={() => performAction(() => executeMission(mission.id))}>Start</button>
                      <button className="tiny-button" disabled={busy} onClick={() => performAction(() => deleteMission(mission.id))}>Delete</button>
                    </>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </section>

        {selected && (
          <section>
            <div className="row">
              <h3>Workflow activity</h3>
              <span className="tiny">{backend.workflowSignals.filter((signal) => String(signal.droneId) === selected.id).length}</span>
            </div>
            <ul className="signal-list">
              {backend.workflowSignals
                .filter((signal) => String(signal.droneId) === selected.id)
                .slice(0, 5)
                .map((signal) => (
                  <li key={signal.id}>
                    <span>{signal.signalType} · {signal.signalStatus}</span>
                    <span className="tiny">{signal.message}</span>
                  </li>
                ))}
            </ul>
          </section>
        )}

        <section className="roster-section" id="drone-roster">
          <div className="row">
            <h3>Aircraft roster</h3>
            <span className="tiny">{drones.length}</span>
          </div>
          <ul className="roster">
            {drones.map((drone) => (
              <li key={drone.id}>
                <button className={drone.id === selectedId ? 'on' : ''} onClick={() => setSelectedId(drone.id)}>
                  <span className="rid">{drone.name}</span>
                  <span className="rstat"><i className={`dot ${drone.status}`} />{drone.status}</span>
                  <span className="rbat">{Math.round(drone.battery)}%</span>
                </button>
              </li>
            ))}
          </ul>
        </section>
      </aside>
    </div>
  )
}
