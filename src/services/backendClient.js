async function getJson(path) {
  const response = await fetch(path)
  if (!response.ok) {
    throw new Error(`Backend request failed (${response.status})`)
  }
  return response.json()
}

export async function getBackendOverview() {
  const [drones, missions] = await Promise.all([
    getJson('/api/drones'),
    getJson('/api/missions'),
  ])

  if (!Array.isArray(drones) || !Array.isArray(missions)) {
    throw new Error('Backend returned an invalid drone or mission list')
  }

  return { drones, missions }
}
