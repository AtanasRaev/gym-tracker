import { useEffect, useState } from 'react'
import { fetchExercises, type Exercise, type Page } from '../api/exercises'

const PAGE_SIZE = 20

/** Turns enum values like OLYMPIC_WEIGHTLIFTING into "Olympic weightlifting". */
function humanize(value: string | null): string | null {
  if (!value) return null
  const text = value.replaceAll('_', ' ').toLowerCase()
  return text.charAt(0).toUpperCase() + text.slice(1)
}

function ExerciseCard({ exercise }: { exercise: Exercise }) {
  const tags = [exercise.category, exercise.level, exercise.equipment, exercise.mechanic]
    .map(humanize)
    .filter((tag): tag is string => tag !== null)

  return (
    <article className="card">
      {exercise.imageUrls[0] ? (
        <img className="card-image" src={exercise.imageUrls[0]} alt={exercise.name} loading="lazy" />
      ) : (
        <div className="card-image card-image--empty">No image</div>
      )}
      <div className="card-body">
        <h2>{exercise.name}</h2>
        {exercise.primaryMuscles.length > 0 && (
          <p className="muscles">{exercise.primaryMuscles.join(', ')}</p>
        )}
        <ul className="tags">
          {tags.map((tag) => (
            <li key={tag}>{tag}</li>
          ))}
        </ul>
      </div>
    </article>
  )
}

export default function ExercisesPage() {
  const [pageNumber, setPageNumber] = useState(0)
  const [data, setData] = useState<Page<Exercise> | null>(null)
  // Which page the current data/error belongs to; anything else means a request is in flight
  const [settledPage, setSettledPage] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const loading = settledPage !== pageNumber

  useEffect(() => {
    const controller = new AbortController()
    fetchExercises(pageNumber, PAGE_SIZE, controller.signal)
      .then((result) => {
        setData(result)
        setError(null)
        setSettledPage(pageNumber)
      })
      .catch((err: unknown) => {
        if (controller.signal.aborted) return
        setError(err instanceof Error ? err.message : String(err))
        setSettledPage(pageNumber)
      })
    return () => controller.abort()
  }, [pageNumber])

  const totalPages = data?.page.totalPages ?? 0

  return (
    <main className="container">
      <header className="page-header">
        <h1>Exercises</h1>
        {data && <span className="count">{data.page.totalElements} total</span>}
      </header>

      {error && <p className="error">{error}</p>}
      {!error && data?.content.length === 0 && <p>No exercises yet.</p>}

      <section className={`grid${loading ? ' grid--loading' : ''}`}>
        {data?.content.map((exercise) => (
          <ExerciseCard key={exercise.id} exercise={exercise} />
        ))}
      </section>

      {totalPages > 1 && (
        <nav className="pagination">
          <button onClick={() => setPageNumber((p) => p - 1)} disabled={loading || pageNumber === 0}>
            Previous
          </button>
          <span>
            Page {pageNumber + 1} of {totalPages}
          </span>
          <button
            onClick={() => setPageNumber((p) => p + 1)}
            disabled={loading || pageNumber + 1 >= totalPages}
          >
            Next
          </button>
        </nav>
      )}
    </main>
  )
}
