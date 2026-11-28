import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { ApiError } from '../api/auth'
import { useAuth } from '../auth/AuthContext'
import FormField from '../components/FormField'
import { redirectTarget } from './redirectTarget'

export default function SignInPage() {
  const { user, signIn } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (user && !submitting) {
    return <Navigate to={redirectTarget(location.state)} replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    if (!email.trim() || !password) {
      setError('Enter your email and password.')
      return
    }
    setSubmitting(true)
    try {
      await signIn(email.trim(), password)
      navigate(redirectTarget(location.state), { replace: true })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Sign in failed. Try again.')
      setSubmitting(false)
    }
  }

  return (
    <main className="container auth-page">
      <form className="auth-card" onSubmit={handleSubmit} noValidate>
        <h1>Sign in</h1>

        {error && (
          <p className="form-error" role="alert">
            {error}
          </p>
        )}

        <FormField
          label="Email"
          type="email"
          name="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <FormField
          label="Password"
          type="password"
          name="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <button type="submit" className="button button--primary" disabled={submitting}>
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>

        <p className="auth-switch">
          Don't have an account?{' '}
          <Link to="/sign-up" state={location.state}>
            Sign up
          </Link>
        </p>
      </form>
    </main>
  )
}
