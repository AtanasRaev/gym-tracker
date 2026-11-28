export interface Exercise {
  id: number
  name: string
  category: string | null
  level: string | null
  equipment: string | null
  force: string | null
  mechanic: string | null
  primaryMuscles: string[]
  secondaryMuscles: string[]
  imageUrls: string[]
  instructions: string | null
}

/** Shape of Spring Data's PagedModel JSON. */
export interface Page<T> {
  content: T[]
  page: {
    size: number
    number: number
    totalElements: number
    totalPages: number
  }
}

export async function fetchExercises(
  page: number,
  size: number,
  signal?: AbortSignal,
): Promise<Page<Exercise>> {
  const params = new URLSearchParams({ page: String(page), size: String(size), sort: 'name,asc' })
  const response = await fetch(`/api/exercises?${params}`, { signal, credentials: 'include' })
  if (!response.ok) {
    throw new Error(`Failed to load exercises (HTTP ${response.status})`)
  }
  return response.json()
}
