package oj.oj_backend.controller;

import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * pom 配置依赖项
 * <dependency>
 *   <groupId>com.alibaba</groupId>
 *   <artifactId>dashscope-sdk-java</artifactId>
 *   <!-- 请将 'the-latest-version' 替换为查询到的最新版本号：https://mvnrepository.com/artifact/com.alibaba/dashscope-sdk-java -->
 *   <version>the-latest-version</version>
 * </dependency>
 *
 * 将API Key配置到环境变量 https://help.aliyun.com/zh/model-studio/developer-reference/configure-api-key-through-environment-variables
 */
@RestController
@RequestMapping("/ai")
public class AiController {
    @Value("${dashscope.appId}")
    private String appId;

    /**
     * 创建线程池
     **/
    ThreadPoolExecutor executor = new ThreadPoolExecutor(2,4,60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    /**
     * 实现 chat 接口，支持流式返回数据
     *
     * @param query
     * @return
     */

    // todo 返回数据编码有问题
    @RequestMapping(value = "/chat", method = RequestMethod.POST)
    public ResponseBodyEmitter streamData(@RequestBody String query) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(180000L);
        executor.execute(() -> {
            try {
                JsonObject jsonObject = new JsonParser().parse(query).getAsJsonObject();
                streamCall(emitter, jsonObject.get("prompt").getAsString());
            } catch (NoApiKeyException | InputRequiredException e) {
                e.printStackTrace();
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    /**
     * 调用百炼应用，封装流式返回数据
     * 返回数据格式
     * id:1
     * event:result
     * :HTTP_STATUS/200
     * data:{"output":{"session_id":"xxx","finish_reason":"null","text":"相关的问题"}}
     *
     * @param emitter
     * @param query
     * @throws NoApiKeyException
     * @throws InputRequiredException
     */
    public void streamCall(ResponseBodyEmitter emitter, String query) throws NoApiKeyException, InputRequiredException {
        // appId 填入百炼应用 ID
        ApplicationParam param = ApplicationParam.builder()
                .appId(appId)
                .prompt(query)
                .incrementalOutput(true)
                .build();

        Application application = new Application();
        Flowable<ApplicationResult> result = application.streamCall(param);
        AtomicInteger counter = new AtomicInteger(0);
        result.blockingForEach(data -> {
            int newValue = counter.incrementAndGet();
            String resData = "id:" + newValue + "\nevent:result\n:HTTP_STATUS/200\ndata:" + new Gson().toJson(data) + "\n\n";
            MediaType mediaType = MediaType.TEXT_EVENT_STREAM;
            mediaType = new MediaType(mediaType.getType(), mediaType.getSubtype(), StandardCharsets.UTF_8);
            emitter.send(resData, mediaType);

            if ("stop".equals(data.getOutput().getFinishReason())) {
                emitter.complete();
            }
        });
    }
}