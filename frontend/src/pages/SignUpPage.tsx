import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { ApiError, type FieldErrors } from '../api/auth'
import { useAuth } from '../auth/AuthContext'
import FormField from '../components/FormField'
import { redirectTarget } from './redirectTarget'

interface SignUpForm {
  username: string
  email: string
  password: string
  confirmPassword: string
  firstName: string
  lastName: string
}

const EMPTY_FORM: SignUpForm = {
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  firstName: '',
  lastName: '',
}

/** The same rules as the backend, so that most mistakes show before the request. */
function validate(form: SignUpForm): FieldErrors {
  const errors: FieldErrors = {}
  const username = form.username.trim()
  if (!username) errors.username = 'Username cannot be empty'
  else if (username.length < 3 || username.length > 30) errors.username = 'Username must be between 3 and 30 characters'

  if (!form.email.trim()) errors.email = 'Email cannot be empty'

  if (!form.password) errors.password = 'Password cannot be empty'
  else if (form.password.length < 8 || form.password.length > 72)
    errors.password = 'Password must be between 8 and 72 characters'

  if (form.confirmPassword !== form.password) errors.confirmPassword = 'Passwords do not match'

  if (form.firstName.length > 100) errors.firstName = 'First name must be at most 100 characters'
  if (form.lastName.length > 100) errors.lastName = 'Last name must be at most 100 characters'
  return errors
}

export default function SignUpPage() {
  const { user, signUp } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState<SignUpForm>(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (user && !submitting) {
    return <Navigate to={redirectTarget(location.state)} replace />
  }

  function update(field: keyof SignUpForm, value: string) {
    setForm((current) => ({ ...current, [field]: value }))
    // Clear the message for a field as soon as the user changes it
    setFieldErrors((current) => ({ ...current, [field]: undefined }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)

    const errors = validate(form)
    setFieldErrors(errors)
    if (Object.values(errors).some(Boolean)) return

    setSubmitting(true)
    try {
      await signUp({
        username: form.username.trim(),
        email: form.email.trim(),
        password: form.password,
        firstName: form.firstName.trim() || undefined,
        lastName: form.lastName.trim() || undefined,
      })
      navigate(redirectTarget(location.state), { replace: true })
    } catch (err) {
      if (err instanceof ApiError) {
        setFieldErrors(err.fieldErrors)
        // A field message already explains the problem, so show the general message only without one
        setError(Object.keys(err.fieldErrors).length > 0 ? null : err.message)
      } else {
        setError('Sign up failed. Try again.')
      }
      setSubmitting(false)
    }
  }

  return (
    <main className="container auth-page">
      <form className="auth-card" onSubmit={handleSubmit} noValidate>
        <h1>Sign up</h1>

        {error && (
          <p className="form-error" role="alert">
            {error}
          </p>
        )}

        <FormField
          label="Username"
          name="username"
          autoComplete="username"
          value={form.username}
          onChange={(e) => update('username', e.target.value)}
          error={fieldErrors.username}
        />
        <FormField
          label="Email"
          type="email"
          name="email"
          autoComplete="email"
          value={form.email}
          onChange={(e) => update('email', e.target.value)}
          error={fieldErrors.email}
        />
        <FormField
          label="Password"
          type="password"
          name="password"
          autoComplete="new-password"
          value={form.password}
          onChange={(e) => update('password', e.target.value)}
          error={fieldErrors.password}
          hint="8 to 72 characters"
        />
        <FormField
          label="Confirm password"
          type="password"
          name="confirmPassword"
          autoComplete="new-password"
          value={form.confirmPassword}
          onChange={(e) => update('confirmPassword', e.target.value)}
          error={fieldErrors.confirmPassword}
        />

        <div className="field-row">
          <FormField
            label="First name (optional)"
            name="firstName"
            autoComplete="given-name"
            value={form.firstName}
            onChange={(e) => update('firstName', e.target.value)}
            error={fieldErrors.firstName}
          />
          <FormField
            label="Last name (optional)"
            name="lastName"
            autoComplete="family-name"
            value={form.lastName}
            onChange={(e) => update('lastName', e.target.value)}
            error={fieldErrors.lastName}
          />
        </div>

        <button type="submit" className="button button--primary" disabled={submitting}>
          {submitting ? 'Signing up…' : 'Sign up'}
        </button>

        <p className="auth-switch">
          Already have an account?{' '}
          <Link to="/sign-in" state={location.state}>
            Sign in
          </Link>
        </p>
      </form>
    </main>
  )
}
