package oj.oj_codesandbox.judge;


import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import oj.oj_codesandbox.CodeSandbox;
import oj.oj_codesandbox.CodeSandboxFactor;
import oj.oj_codesandbox.CodeSandboxProxy;
import oj.oj_codesandbox.comm.ResponseResult;
import oj.oj_codesandbox.judge.entity.JudgeContext;
import oj.oj_codesandbox.judge.entity.JudgeInfo;
import oj.oj_codesandbox.judge.myenum.CommitStatusEnum;
import oj.oj_codesandbox.model.ExecuteCodeRequest;
import oj.oj_codesandbox.model.ExecuteCodeResponse;
import oj.oj_codesandbox.model.dto.CommitCase;
import oj.oj_codesandbox.model.dto.Question;
import oj.oj_codesandbox.model.dto.UserCommit;
import oj.oj_codesandbox.model.vo.CommitResultVo;
import oj.oj_codesandbox.rabbitmq.RabbitMQConfig;
import oj.oj_codesandbox.service.CommitCaseService;
import oj.oj_codesandbox.service.QuestionService;
import oj.oj_codesandbox.service.UserCommitService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 *  实现判题逻辑
 */
@Service
public class JudgeServiceImpl implements JudgeService {
    @Resource
    private UserCommitService userCommitService;
    @Resource
    private QuestionService questionService;
    @Resource
    private CommitCaseService commitCaseService;
    @Value("${codesandbox.type}")
    private String type;
    @Resource
    private JudgeManager judgeManager;
    @Resource
    private RabbitTemplate rabbitTemplate;
    
    // 添加沙箱连接池，避免重复创建
    private final CodeSandbox codeSandbox;
    private final CodeSandbox codeSandboxProxy;
    
    public JudgeServiceImpl() {
        // 预创建沙箱实例，避免每次判题都创建新实例
        this.codeSandbox = CodeSandboxFactor.newInstance("remote");
        this.codeSandboxProxy = new CodeSandboxProxy(this.codeSandbox);
    }
    
    private static final Logger log = LoggerFactory.getLogger(JudgeServiceImpl.class);

    /**
     * 在进入队列前发生错误允许回滚
     * @param executeCodeRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<CommitResultVo> doJudge(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        String problemId = executeCodeRequest.getProblemId();
        String uuid = executeCodeRequest.getUuid();

        UserCommit commit = new UserCommit();
        String commitId = UUID.randomUUID().toString();
        commit.setUid(uuid);
        commit.setQid(problemId);
        commit.setCommitId(commitId);
        commit.setCode(code);
        commit.setLanguage(language);
        commit.setCreateTime(new Date());
        int save = userCommitService.insert(commit);
        if (save <= 0) {
            throw new RuntimeException("提交保存失败");
        }

        // 题目保存为等待中的状态
        CommitCase commitCase = new CommitCase();
        commitCase.setCommitId(commitId);
        commitCase.setCnName(CommitStatusEnum.WAITING.getCnMessage());
        commitCase.setEnglishName(CommitStatusEnum.WAITING.getEnMessage());
        int insert = commitCaseService.insert(commitCase);
        if (insert <= 0) {
            throw new RuntimeException("提交状态保存失败");
        }

        executeCodeRequest.setCommitId(commitId);
        rabbitTemplate.convertAndSend(RabbitMQConfig.JUDGE_EXCHANGE, RabbitMQConfig.JUDGE_ROUTING_KEY, executeCodeRequest);

        // 返回提交成功的响应，包含commitId供前端查询判题结果
        CommitResultVo resultVo = new CommitResultVo();
        resultVo.setMessage("提交成功，正在判题中");
        resultVo.setCommitId(commitId);
        return ResponseResult.success(resultVo);
    }

    /**
     * 消费消息队列中的判题任务
     */
    @RabbitListener(queues = RabbitMQConfig.JUDGE_QUEUE)
    public void processJudgeTask(ExecuteCodeRequest executeCodeRequest) {
        String commitId = executeCodeRequest.getCommitId();
        
        // 异步处理判题任务，提高并发性能
        CompletableFuture.runAsync(() -> {
            processJudgeTaskAsync(executeCodeRequest, commitId);
        });
    }
    
    /**
     * 异步处理判题任务
     */
    @Async("judgeTaskExecutor")
    public void processJudgeTaskAsync(ExecuteCodeRequest executeCodeRequest, String commitId) {
        try {
            // 更改状态为"判题中"
            CommitCase commitCaseUpdate = new CommitCase();
            commitCaseUpdate.setCommitId(commitId);
            commitCaseUpdate.setCnName(CommitStatusEnum.RUNNING.getCnMessage());
            commitCaseUpdate.setEnglishName(CommitStatusEnum.RUNNING.getEnMessage());
            boolean update = commitCaseService.updateById(commitCaseUpdate);
            if (!update) {
                throw new RuntimeException("更新CommitCase表失败");
            }

            // 获取题目信息
            String problemId = executeCodeRequest.getProblemId();
            Question question = questionService.getOneQuestion(problemId);
            if (question == null) {
                throw new RuntimeException("题目找不到");
            }

            // 获取用户提交信息
            UserCommit userCommit = userCommitService.getByCommitId(commitId);
            if (userCommit == null) {
                throw new RuntimeException("提交记录不存在");
            }

            // 调用沙箱 - 使用预创建的实例，避免重复创建
            ExecuteCodeResponse executeCodeResponse = codeSandboxProxy.executeCode(executeCodeRequest);
            List<String> outputList = executeCodeResponse.getOutputList();
            JudgeInfo exeJudgeInfo = executeCodeResponse.getJudgeInfo();
            // 沙箱执行代码获取返回对象
            JudgeContext judgeContext = JudgeContext.builder()
                    .exitCode(executeCodeResponse.getExitCode())
                    .question(question)
                    .outputList(outputList)
                    .judgeInfo(exeJudgeInfo)
                    .userCommit(userCommit)
                    .build();

            // 获取判题结果
            JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

            // 更新状态
            commitCaseUpdate = new CommitCase();
            commitCaseUpdate.setCommitId(commitId);
            commitCaseUpdate.setCnName(judgeInfo.getCnMessage());
            commitCaseUpdate.setEnglishName(judgeInfo.getEnMessage());
            commitCaseUpdate.setTime(judgeInfo.getTime());
            commitCaseUpdate.setMemory(judgeInfo.getMemory());
            long exitCode = judgeInfo.getExitCode();
            if (exitCode == 0) {
                commitCaseUpdate.setOutput(Arrays.toString(exeJudgeInfo.getCorrect()));
            }

            commitCaseService.updateById(commitCaseUpdate);

            // 优化：使用事务确保数据一致性
            try {
                // 先更新提交总数（无论成功失败都要更新）
                int commitResult = questionService.updateCommitCountById(problemId);
                if (commitResult <= 0) {
                    log.warn("更新提交总数失败，problemId: {}", problemId);
                }
                
                // 再更新通过数（只有成功才更新）
                if (judgeInfo.getCnMessage().equals(CommitStatusEnum.ACCEPTED.getCnMessage())) {
                    int acResult = questionService.updateAcCountById(problemId);
                    if (acResult <= 0) {
                        log.warn("更新通过数失败，problemId: {}", problemId);
                    }
                }
            } catch (Exception e) {
                log.error("更新题目统计信息失败，problemId: {}, commitId: {}", problemId, commitId, e);
                // 不影响主流程，只记录日志
            }

            log.info("判题任务处理成功，commitId: {}", commitId);

        } catch (Exception e) {
            log.error("判题过程发生异常，commitId: {}, 错误信息: {}", commitId, e.getMessage(), e);
            
            // 处理异常，更新提交状态为错误
            CommitCase commitCaseError = new CommitCase();
            commitCaseError.setCommitId(commitId);
            commitCaseError.setCnName(CommitStatusEnum.FAIL.getCnMessage());
            commitCaseError.setEnglishName(CommitStatusEnum.FAIL.getEnMessage());
            commitCaseService.updateById(commitCaseError);
            
            // 抛出异常，让Spring AMQP处理重试逻辑
            throw new RuntimeException("判题失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据提交ID查询判题结果
     */
    public ResponseResult<CommitResultVo> getJudgeResult(String commitId) {
        // 查询判题结果
        CommitCase commitCase = commitCaseService.getByCommitId(commitId);
        if (commitCase == null) {
            return ResponseResult.fail("提交记录不存在");
        }

        CommitResultVo resultVo = new CommitResultVo();
        String judgeResult = commitCase.getCnName();
        resultVo.setCommitId(commitId);
        resultVo.setMessage(judgeResult);
        resultVo.setTime(commitCase.getTime());
        resultVo.setMemory(commitCase.getMemory());
        resultVo.setOutput(Collections.singletonList(commitCase.getOutput()));
        //做错误信息返回
        if (judgeResult.equals(CommitStatusEnum.COMPILE_ERROR.getCnMessage()) ||
                judgeResult.equals(CommitStatusEnum.RUNTIME_ERROR.getCnMessage()) ||
                judgeResult.equals(CommitStatusEnum.NON_ZERO_ERROR.getCnMessage())) {

            resultVo.setErrorMessages(commitCase.getEnglishName());
        }
        // 如果状态是等待中或运行中，前端可以继续轮询
        resultVo.setProcessing(CommitStatusEnum.WAITING.getCnMessage().equals(commitCase.getCnName()) ||
                CommitStatusEnum.RUNNING.getCnMessage().equals(commitCase.getCnName()));

        return ResponseResult.success(resultVo);
    }

    @Override
    public ResponseResult<CommitResultVo> userTest(ExecuteCodeRequest executeCodeRequest) {
        String input = executeCodeRequest.getUserInput();
        String language = executeCodeRequest.getLanguage();
        String problemId = executeCodeRequest.getProblemId();

        if (StrUtil.isEmptyIfStr(input)) {
            // 无输入，提示输入
            CommitResultVo resultVo = new CommitResultVo();
            resultVo.setOutput(null);
            resultVo.setTime(0L);
            resultVo.setMemory(0L);
            resultVo.setErrorMessages("请在输入行完成输入!");
            return ResponseResult.fail(resultVo);
        }

        Question question = questionService.getOneQuestion(problemId);
        if (question == null){
            throw new RuntimeException("题目找不到");
        }

        // 调用沙箱
        CodeSandbox codeSandbox = CodeSandboxFactor.newInstance(type);
        // 代理对象 打印日志
        codeSandbox = new CodeSandboxProxy(codeSandbox);

        ExecuteCodeResponse executeCodeResponse = codeSandbox.userTestCode(executeCodeRequest);

        List<String> outputList = executeCodeResponse.getOutputList();
        // 沙箱执行代码获取返回对象
        JudgeContext judgeContext = JudgeContext.builder()
                .exitCode(executeCodeResponse.getExitCode())
                .question(question)
                .language(language)
                .outputList(outputList)
                .judgeInfo(executeCodeResponse.getJudgeInfo())
                .build();
        // 获取判题结果
        JudgeInfo judgeInfo = judgeManager.doTest(judgeContext);

        // 返回结果对象
        long exitCode = judgeInfo.getExitCode();
        CommitResultVo resultVo = new CommitResultVo();
        if (exitCode != 0) {
            resultVo.setErrorMessages(judgeInfo.getCnMessage() + " : " + judgeInfo.getEnMessage());
        } else {
            resultVo.setMessage(judgeInfo.getCnMessage());
        }
        resultVo.setOutput(outputList);
        resultVo.setTime(judgeInfo.getTime());
        resultVo.setMemory(judgeInfo.getMemory());

        return ResponseResult.success(resultVo);
    }
}
