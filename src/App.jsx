


import { useEffect, useState } from 'react'
import { House, Target, Plane, Globe, ChartColumn, Settings } from 'lucide-react'
import GlobeView from './components/GlobeView'
import MissionDag from './components/MissionDag'
import { DRONES } from './data/drones'
import { subscribe } from './services/telemetryClient'
import './App.css'

const NAV = [
  ['Dashboard', House],
  ['Missions', Target],
  ['Drones', Plane],
  ['Map View', Globe],
  ['Analytics', ChartColumn],
  ['Settings', Settings],
]

const coord = (lat, lon) =>
  `${Math.abs(lat).toFixed(4)}° ${lat >= 0 ? 'N' : 'S'}, ${Math.abs(lon).toFixed(4)}° ${lon >= 0 ? 'E' : 'W'}`

export default function App() {
  const [nav, setNav] = useState('Dashboard')
  const [selectedId, setSelectedId] = useState('DRONE-002')
  const [drones, setDrones] = useState(DRONES)
  const [failIds, setFailIds] = useState(() => new Set()) // jin drones par failure inject kiya

  useEffect(() => subscribe(setDrones), [])

  const toggleFail = (id) =>
    setFailIds((prev) => {
      const next = new Set(prev)
      next.has(id) ? next.delete(id) : next.add(id)
      return next
    })

  const selected = drones.find((d) => d.id === selectedId)
  const count = (status) => drones.filter((d) => d.status === status).length

  return (
    <div className="app">
      <nav className="sidebar">
        <div className="brand">
          <Plane size={24} />
          <div>
            <div className="brand-name">Arrow Saga</div>
            <div className="brand-sub">Drone Fleet Simulation</div>
          </div>
        </div>

        {NAV.map(([label, Icon]) => (
          <button key={label} className={nav === label ? 'on' : ''} onClick={() => setNav(label)}>
            <Icon size={20} />
            {label}
          </button>
        ))}

        <div className="side-foot">
          <div><i className="dot live" />System Online</div>
          <div className="muted">v1.0.0 (Week 3)</div>
        </div>
      </nav>

      <main className="map-area">
        <GlobeView drones={drones} selectedId={selectedId} onSelect={setSelectedId} />
      </main>

      <aside className="panel">
        <div className="panel-head">
          <span className="panel-icon"><Plane size={20} /></span>
          <h2>Fleet control</h2>
          <span className="live-text"><i className="dot live" />LIVE SIM</span>
        </div>

        <section>
          <div className="row">
            <h3>Fleet overview</h3>
            <span className="tiny">{drones.length} AIRCRAFT</span>
          </div>
          <div className="stats">
            <div><span>Active</span><b className="teal">{count('active')}</b></div>
            <div><span>Idle</span><b className="amber">{count('idle')}</b></div>
            <div><span>Offline</span><b>{count('offline')}</b></div>
          </div>
        </section>

        <section>
          <div className="row">
            <h3>Selected aircraft</h3>
            <span className={`badge ${selected.status}`}>{selected.status.toUpperCase()}</span>
          </div>
          <div className="drone-name">{selected.id}</div>
          <div className="grid2">
            <div><span>Altitude</span><b>{selected.alt} m</b></div>
            <div><span>Speed</span><b>{selected.speed} km/h</b></div>
            <div><span>Heading</span><b>{selected.heading}°</b></div>
            <div><span>Battery</span><b>{Math.round(selected.battery)}%</b></div>
          </div>
          <div className="mission">
            <div><span>Current position</span><b>{coord(selected.lat, selected.lon)}</b></div>
            <div className="right"><span>{selected.mission}</span><b>{coord(...selected.dest)}</b></div>
          </div>
        </section>

        <MissionDag
          drone={selected}
          forceFail={failIds.has(selected.id)}
          onToggleFail={() => toggleFail(selected.id)}
        />

        <section className="roster-section">
          <div className="row">
            <h3>Aircraft roster</h3>
            <span className="tiny">{drones.length}</span>
          </div>
          <ul className="roster">
            {drones.map((d) => (
              <li key={d.id}>
                <button className={d.id === selectedId ? 'on' : ''} onClick={() => setSelectedId(d.id)}>
                  <span className="rid">{d.id}</span>
                  <span className="rstat"><i className={`dot ${d.status}`} />{d.status}</span>
                  <span className="rbat">{Math.round(d.battery)}%</span>
                </button>
              </li>
            ))}
          </ul>
        </section>
      </aside>
    </div>
  )
}