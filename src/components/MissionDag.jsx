import { useEffect, useState } from 'react'
import { getMission, nowSec } from '../services/mockMissions'

const ICON = { pending: '○', active: '◉', completed: '✓', failed: '✕', compensating: '↺', skipped: '–' }

// Har 500 ms par dobara draw karo, taaki DAG "live" lage
function useTick(ms = 500) {
  const [, set] = useState(0)
  useEffect(() => {
    const id = setInterval(() => set((x) => x + 1), ms)
    return () => clearInterval(id)
  }, [ms])
}

function Node({ step }) {
  return (
    <div className={`dag-node ${step.status}`}>
      <span className="dag-ic">{ICON[step.status]}</span>
      <span>
        {step.label}
        {step.id === 'rtb' && <span className="dag-tag">compensation</span>}
      </span>
    </div>
  )
}

export default function MissionDag({ drone, forceFail, onToggleFail }) {
  useTick()
  const m = getMission(drone, nowSec(), forceFail)
  const s = Object.fromEntries(m.steps.map((x) => [x.id, x]))

  return (
    <section>
      <div className="row">
        <h3>Mission workflow</h3>
        <span className={`wf-badge ${m.state}`}>{m.state}</span>
      </div>
      <div className="tiny wf-sub">{drone.mission}</div>

      <div className="dag">
        <Node step={s.preflight} /><div className="dag-link" />
        <Node step={s.takeoff} /><div className="dag-link" />
        <Node step={s.navigate} /><div className="dag-link" />
        <Node step={s.drop} />
        <div className="dag-link center" />
        <div className="dag-branch">
          <Node step={s.confirm} />
          <Node step={s.rtb} />
        </div>
        <div className="dag-merge" />
        <Node step={s.land} />
      </div>

      <div className="dag-legend tiny">
        <span>○ Pending</span><span>◉ Active</span><span>✓ Completed</span><span>✕ Failed</span><span>↺ Compensating</span>
      </div>

      <button
        className={`inject ${forceFail ? 'on' : ''}`}
        onClick={onToggleFail}
        disabled={drone.status !== 'active'}
      >
        {forceFail ? 'Failure injected (click to undo)' : 'Inject drop failure'}
      </button>
    </section>
  )
}