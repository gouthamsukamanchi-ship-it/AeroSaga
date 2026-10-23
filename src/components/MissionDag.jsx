const ICON = {
  pending: '○',
  active: '◉',
  completed: '✓',
  failed: '✕',
  skipped: '–',
}

const STEPS = [
  ['preflight', 'Pre-flight check'],
  ['takeoff', 'Takeoff'],
  ['navigate', 'Navigate to destination'],
  ['delivery', 'Complete delivery'],
  ['return', 'Return to base'],
  ['land', 'Land'],
]

function statusForStep(missionStatus, index) {
  if (missionStatus === 'COMPLETED') return 'completed'
  if (missionStatus === 'FAILED') {
    if (index < 2) return 'completed'
    if (index === 2) return 'failed'
    return 'skipped'
  }
  if (missionStatus === 'RUNNING') {
    if (index < 2) return 'completed'
    if (index === 2) return 'active'
  }
  return 'pending'
}

function Node({ label, status }) {
  return (
    <div className={`dag-node ${status}`}>
      <span className="dag-ic">{ICON[status]}</span>
      <span>{label}</span>
    </div>
  )
}

export default function MissionDag({ mission, onExecute, busy }) {
  const status = mission?.status?.toUpperCase() || 'NO MISSION'

  return (
    <section>
      <div className="row">
        <h3>Mission workflow</h3>
        <span className={`wf-badge ${status}`}>{status}</span>
      </div>
      <div className="tiny wf-sub">
        {mission ? `${mission.missionName} · ${mission.destination}` : 'No mission assigned to this drone'}
      </div>

      {mission && (
        <>
          <div className="dag">
            {STEPS.map(([id, label], index) => (
              <div key={id}>
                <Node label={label} status={statusForStep(status, index)} />
                {index < STEPS.length - 1 && <div className="dag-link" />}
              </div>
            ))}
          </div>

          {status === 'PENDING' && (
            <button className="inject" onClick={() => onExecute(mission.id)} disabled={busy}>
              {busy ? 'Starting mission…' : 'Start Temporal mission'}
            </button>
          )}
        </>
      )}
      <p className="tiny">Workflow progress is read from the backend mission record.</p>
    </section>
  )
}
