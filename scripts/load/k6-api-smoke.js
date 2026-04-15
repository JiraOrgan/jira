/**
 * k6 스모크 부하 시나리오 (NFR 스팟 점검용).
 *
 * 사용:
 *   BASE_URL=http://localhost:8080 k6 run scripts/load/k6-api-smoke.js
 *
 * 로그인(선택): EMAIL, PASSWORD 환경 변수 — 없으면 공개 엔드포인트만 호출.
 */
import http from 'k6/http'
import { check, sleep } from 'k6'

const base = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

export const options = {
  vus: 10,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<500'],
  },
}

function health() {
  const res = http.get(`${base}/actuator/health`)
  check(res, { 'health 200': (r) => r.status === 200 })
}

function registerAndLogin() {
  const email = `k6_${__VU}_${Date.now()}@load.test`
  const password = 'password12'
  const reg = http.post(
    `${base}/api/auth/register`,
    JSON.stringify({
      email,
      password,
      name: 'k6',
    }),
    { headers: { 'Content-Type': 'application/json' } },
  )
  if (reg.status !== 201) return null
  const login = http.post(
    `${base}/api/auth/login`,
    JSON.stringify({ email, password }),
    { headers: { 'Content-Type': 'application/json' } },
  )
  if (login.status !== 200) return null
  try {
    const token = login.json('data.accessToken')
    return token ? String(token) : null
  } catch {
    return null
  }
}

export default function () {
  health()
  sleep(0.3)

  const email = __ENV.EMAIL
  const password = __ENV.PASSWORD
  let token = null
  if (email && password) {
    const login = http.post(
      `${base}/api/auth/login`,
      JSON.stringify({ email, password }),
      { headers: { 'Content-Type': 'application/json' } },
    )
    if (login.status === 200) {
      try {
        token = login.json('data.accessToken')
      } catch {
        token = null
      }
    }
  } else {
    token = registerAndLogin()
  }

  if (!token) {
    sleep(1)
    return
  }

  const headers = {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
  }

  const proj = http.post(
    `${base}/api/v1/projects`,
    JSON.stringify({
      key: `K${__VU}${String(Date.now()).slice(-4)}`,
      name: 'k6 project',
      boardType: 'SCRUM',
    }),
    { headers },
  )
  check(proj, { 'create project 201': (r) => r.status === 201 })
  sleep(0.2)

  const pid = proj.json('data.id')
  const pkey = proj.json('data.key')
  if (!pid || !pkey) return

  const jqlRes = http.post(
    `${base}/api/v1/projects/${pid}/jql/search`,
    JSON.stringify({ jql: `project = ${pkey}`, maxResults: 10 }),
    { headers },
  )
  check(jqlRes, { 'jql search 200': (r) => r.status === 200 })

  sleep(0.5)
}
