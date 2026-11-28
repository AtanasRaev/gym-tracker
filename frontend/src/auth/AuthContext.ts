import { createContext, useContext } from 'react'
import type { User } from '../api/auth'

export interface AuthState {
  /** The signed-in user, or null when signed out. */
  user: User | null
  /** True until the first /auth/me call finishes. */
  loading: boolean
  signIn: (email: string, password: string) => Promise<User>
  signOut: () => Promise<void>
}

export const AuthContext = createContext<AuthState | null>(null)

export function useAuth(): AuthState {
  const auth = useContext(AuthContext)
  if (!auth) throw new Error('useAuth must be used inside <AuthProvider>')
  return auth
}
