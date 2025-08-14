import request from "@/utils/request.js";

export function GetContestTables() {
  return request.get('/api/fetch/contests')
}
