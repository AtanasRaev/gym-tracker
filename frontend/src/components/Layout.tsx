import { useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext'

export default function Layout() {
  const { user, loading, signOut } = useAuth()
  const navigate = useNavigate()
  const [signingOut, setSigningOut] = useState(false)

  async function handleSignOut() {
    setSigningOut(true)
    try {
      await signOut()
      navigate('/')
    } catch {
      // The session may already be gone; the next /auth/me call shows the real state
    } finally {
      setSigningOut(false)
    }
  }

  return (
    <>
      <header className="site-header">
        <div className="site-header-inner">
          <Link to="/" className="brand">
            Gym Tracker
          </Link>
          <nav className="site-nav">
            <NavLink to="/" end>
              Exercises
            </NavLink>
          </nav>
          {/* Show nothing until /auth/me answers, so the header does not flicker */}
          {!loading && (
            <div className="site-account">
              {user ? (
                <>
                  <span className="site-user">{user.username}</span>
                  <button type="button" className="button button--ghost" onClick={handleSignOut} disabled={signingOut}>
                    Sign out
                  </button>
                </>
              ) : (
                <>
                  <Link to="/sign-in" className="button button--ghost">
                    Sign in
                  </Link>
                  <Link to="/sign-up" className="button button--primary button--small">
                    Sign up
                  </Link>
                </>
              )}
            </div>
          )}
        </div>
      </header>
      <Outlet />
    </>
  )
}
