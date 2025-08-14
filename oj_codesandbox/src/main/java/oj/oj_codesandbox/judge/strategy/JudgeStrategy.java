package oj.oj_codesandbox.judge.strategy;


import oj.oj_codesandbox.judge.entity.JudgeContext;
import oj.oj_codesandbox.judge.entity.JudgeInfo;

public interface JudgeStrategy {
    /**
     * 执行判题
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) ;
    JudgeInfo doTest(JudgeContext judgeContext) ;
}
