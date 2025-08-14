#codeforces.py
# -*- coding:utf-8 -*-
#爬虫需要用到的库，json用于格式转换，requests用于请求网页数据，time库用于处理时间信息（其他ppy文件中会用到）
import json, requests, time

def get_codeforces_contests():
    # 获取contest api信息
    response = requests.get('https://codeforces.com/api/contest.list')
    data = json.loads(response.text)
    data = data['result']

    # 获取当前时间
    current_time = time.time()

    # 过滤并处理数据
    filtered_data = []
    for x in data:
        # 只保存未开始的比赛
        if x['startTimeSeconds'] > current_time:
            contest = {}
            contest['start_time'] = x['startTimeSeconds']
            contest['end_time'] = x['startTimeSeconds'] + x['durationSeconds']
            contest['platform'] = 'Codeforces'
            contest['contest_url'] = 'https://codeforces.com/contest/' + str(x['id'])
            contest['name'] = x['name']
            contest['durationSeconds'] = x['durationSeconds']
            filtered_data.append(contest)

    return filtered_data

if __name__ == "__main__":
    contests = get_codeforces_contests()
    print(json.dumps(contests, ensure_ascii=False))