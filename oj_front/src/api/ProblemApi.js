import request from "@/utils/request.js";

export function GetRecentWA() {
  return request.get('/api/question/rcwrong')
}

export function GetOneProblemDataById(problemId) {
  return request.get('/api/question/data', { params: { problemId } })
}

export function UserTest(requestData) {
  return request.post('/codesandbox/oj/test', requestData)
}

export function Submit(requestData) {
  return request.post('/codesandbox/oj/submit', requestData)
}

export function GetJudgeResult(commitId) {
  return request.get(`/codesandbox/oj/result/${commitId}`)
}

export function Search(Tags) {
  return request.post('/api/question/search', Tags)
}

export function GetProblemCounts(Tags) {
  return request.post('/api/question/counts', Tags)
}
