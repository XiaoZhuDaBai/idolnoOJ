import request from "@/utils/request.js";


export function getVerifyCode(email) {
  return request.post('/api/user/send-verify-code', null, { params: { email } })
}
export function LoginOrRegister(userData) {
  return request.post('/api/user/login-or-register', userData)
}

export function GetJwt(uuid) {
  return request.get('/api/user/jwt', { params: { uuid } })
}

export function RecentCommit(uuid, page) {
  return request.get('/api/user/search-recent-commit', { params: { uuid, page } })
}
export function RecentCommitCount(uuid) {
  return request.get('/api/user/search-recent-commit-count', { params: { uuid} })
}

export function WrongCommit(uuid) {
  return request.get('/api/user/search-wrong-commit', { params: { uuid } })
}

export function MyCommitsData(uuid) {
  return request.get('/api/user/search-my-commits-data', { params: { uuid } })
}

export function SearchCommits(params) {
  return request.post('/api/user/search-commits', params)
}
export function SearchCommitsCount(params) {
  return request.post('/api/user/search-commits-count', params)
}

export function SendFeedback(email) {
  return request.post('/api/feedback/email', email)
}


