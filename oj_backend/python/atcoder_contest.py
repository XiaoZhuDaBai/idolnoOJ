# -*- coding:utf-8 -*-
import json,requests,time
from bs4 import BeautifulSoup


def start_time_change(Time):
    s = ''
    for i in range(0,19):
        s += Time[i]
    cur_time = time.mktime(time.strptime(s,'%Y-%m-%d %H:%M:%S'))
    cur_time -= 3600
    return cur_time


def get_seconds(duration):
    h,m=duration.strip().split(':')
    times=int(h)*3600+int(m)*60
    return times

url = 'https://atcoder.jp/contests/'
req = requests.get(url)
soup=BeautifulSoup(req.text,'lxml')
data=soup.select('#contest-table-upcoming>div>div>table>tbody>tr')

contests = []
current_time = time.time()

for d in data:
    contests_start_time=d.find('time').get_text()
    contests_start_time=start_time_change(contests_start_time)
    # 只保存未开始的比赛
    if contests_start_time > current_time:
        contest = {}
        contests_url='https://atcoder.jp'+d.find_all('a')[1]['href']
        contests_name=d.find_all('a')[1].get_text()
        contests_length=d.find_all('td')[2].get_text()
        contest['name'] = contests_name
        contest['contest_url'] = contests_url
        contest['start_time'] = contests_start_time
        contest['durationSeconds'] = get_seconds(contests_length)
        contest['end_time'] = contests_start_time+contest['durationSeconds']
        contest['platform'] = 'AtCoder'
        contests.append(contest)

print(json.dumps(contests, ensure_ascii=False))