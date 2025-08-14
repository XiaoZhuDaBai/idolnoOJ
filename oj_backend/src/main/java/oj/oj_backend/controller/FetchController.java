package oj.oj_backend.controller;

import cn.hutool.json.JSONUtil;
import oj.oj_backend.model.response.ContestResponse;
import oj.oj_backend.service.FetchService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;



/**
 * @author XiaoZhuDaBai
 * @version 1.0
 * @date 2025/6/15 14:24
 */

@RestController
@RequestMapping("/fetch")
public class FetchController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 从redis中抓取比赛信息
     * @return Json
     */
    @GetMapping("/contests")
    public String fetchContests() {
        Set<String> range = stringRedisTemplate.opsForZSet().range("oj:contest:all", 0, -1);

        if (range == null || range.isEmpty()) {
            return JSONUtil.toJsonStr(Collections.emptyList());
        }

        // 将字符串反序列化为 ContestVo 对象列表
        List<ContestResponse> contests = new ArrayList<>();
        for (String json : range) {
            ContestResponse contest = JSONUtil.toBean(json, ContestResponse.class);
            contests.add(contest);
        }

        return JSONUtil.toJsonStr(contests);
    }
}
