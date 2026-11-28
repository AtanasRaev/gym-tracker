export interface User {
  id: string
  username: string
  email: string
  firstName: string | null
  lastName: string | null
  roles: string[]
}

/** An HTTP error from the backend. `message` is safe to show to the user. */
export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

// The CSRF token lives only in memory. The backend replaces it on sign in, sign up and sign out.
let csrfToken: string | null = null

async function getCsrfToken(): Promise<string> {
  if (csrfToken) return csrfToken
  const response = await fetch('/auth/csrf', { credentials: 'include' })
  if (!response.ok) throw new ApiError(response.status, 'Could not start a secure session. Try again.')
  const body: { token: string } = await response.json()
  csrfToken = body.token
  return csrfToken
}

/** Forget the token so that the next write request fetches the new one. */
function resetCsrfToken() {
  csrfToken = null
}

/**
 * Sends a write request with the CSRF header. A 403 usually means the token is old,
 * so the request is sent one more time with a new token.
 */
async function sendWithCsrf(path: string, init: RequestInit, retry = true): Promise<Response> {
  const token = await getCsrfToken()
  let response: Response
  try {
    response = await fetch(path, {
      ...init,
      credentials: 'include',
      headers: { ...init.headers, 'X-CSRF-TOKEN': token },
    })
  } catch {
    throw new ApiError(0, 'Could not reach the server. Check your connection.')
  }
  if (response.status === 403 && retry) {
    resetCsrfToken()
    return sendWithCsrf(path, init, false)
  }
  return response
}

/** The signed-in user, or null when there is no session. */
export async function fetchCurrentUser(): Promise<User | null> {
  const response = await fetch('/auth/me', { credentials: 'include' })
  if (response.status === 401) return null
  if (!response.ok) throw new ApiError(response.status, 'Could not load your account.')
  return response.json()
}

export async function signIn(email: string, password: string): Promise<User> {
  // Spring Security's form login reads form-encoded fields, not JSON
  const response = await sendWithCsrf('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({ email, password }),
  })
  if (response.status === 401) throw new ApiError(401, 'Wrong email or password.')
  if (!response.ok) throw new ApiError(response.status, 'Sign in failed. Try again.')
  resetCsrfToken()
  return response.json()
}

export async function signOut(): Promise<void> {
  const response = await sendWithCsrf('/auth/logout', { method: 'POST' })
  resetCsrfToken()
  if (!response.ok) throw new ApiError(response.status, 'Sign out failed. Try again.')
}
