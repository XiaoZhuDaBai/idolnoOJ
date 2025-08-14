# -*- coding:utf-8 -*-
import json,requests,time,html
from bs4 import BeautifulSoup

def get_nowcoder_contests():
    url = 'https://ac.nowcoder.com/acm/contest/vip-index'
    req = requests.get(url)
    soup = BeautifulSoup(req.text,'lxml')
    data = soup.select('body > div.nk-container.acm-container > div.nk-main.with-banner-page.clearfix.js-container > div.nk-content > div.platform-mod > div.platform-item.js-item ')
    
    contests = []
    current_time = time.time()

    for d in data:
        solved = d['data-json']
        solved = html.unescape(solved).encode('utf-8')
        solved = json.loads(solved)
        
        # 只保存未开始的比赛
        start_time = int(solved['contestStartTime']/1000)
        if start_time > current_time:
            contest = {}
            contest['start_time'] = start_time
            contest['end_time'] = int(solved['contestEndTime']/1000)
            contest['durationSeconds'] = int(solved['contestDuration']/1000)
            contest['name'] = solved['contestName']
            contest['platform'] = '牛客'
            contest['contest_url'] = 'https://ac.nowcoder.com'+d.find('a')['href']
            contests.append(contest)

    return contests


contests = get_nowcoder_contests()
formatContest = json.dumps(contests, ensure_ascii=False)
print(formatContest)