import { create } from 'zustand'

const STORAGE_KEY = 'pch.auth.v1'

function readStored(): { accessToken: string | null; refreshToken: string | null } {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return { accessToken: null, refreshToken: null }
    const parsed = JSON.parse(raw) as {
      accessToken?: string
      refreshToken?: string
    }
    return {
      accessToken: parsed.accessToken ?? null,
      refreshToken: parsed.refreshToken ?? null,
    }
  } catch {
    return { accessToken: null, refreshToken: null }
  }
}

const initial = readStored()

type AuthState = {
  accessToken: string | null
  refreshToken: string | null
  setTokens: (access: string | null, refresh: string | null) => void
  clear: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: initial.accessToken,
  refreshToken: initial.refreshToken,
  setTokens: (access, refresh) => {
    if (access && refresh) {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({ accessToken: access, refreshToken: refresh }),
      )
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
    set({ accessToken: access, refreshToken: refresh })
  },
  clear: () => {
    localStorage.removeItem(STORAGE_KEY)
    set({ accessToken: null, refreshToken: null })
  },
}))
