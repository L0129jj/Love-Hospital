const TOKEN_KEY = 'hospital_token'
const ROLE_KEY = 'hospital_role'
const USER_ID_KEY = 'hospital_user_id'
const EXPIRES_KEY = 'hospital_expires'
const NAME_KEY = 'hospital_name'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getName(): string | null {
  return localStorage.getItem(NAME_KEY)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ROLE_KEY)
  localStorage.removeItem(USER_ID_KEY)
  localStorage.removeItem(EXPIRES_KEY)
  localStorage.removeItem(NAME_KEY)
}

export function setUserInfo(role: string, userId: number, expiresAt: number, name?: string) {
  localStorage.setItem(ROLE_KEY, role)
  localStorage.setItem(USER_ID_KEY, String(userId))
  localStorage.setItem(EXPIRES_KEY, String(expiresAt))
  if (name) {
    localStorage.setItem(NAME_KEY, name)
  }
}

export function getRole(): string | null {
  return localStorage.getItem(ROLE_KEY)
}

export function getUserId(): number | null {
  const id = localStorage.getItem(USER_ID_KEY)
  return id ? Number(id) : null
}

export function isTokenExpired(): boolean {
  const exp = localStorage.getItem(EXPIRES_KEY)
  if (!exp) return true
  return Date.now() > Number(exp)
}
