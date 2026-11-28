/** Where to go after sign in or sign up: the page that sent the user here, or the home page. */
export function redirectTarget(state: unknown): string {
  const from = (state as { from?: unknown } | null)?.from
  // Only same-app paths. "//host" would leave the site.
  return typeof from === 'string' && from.startsWith('/') && !from.startsWith('//') ? from : '/'
}
