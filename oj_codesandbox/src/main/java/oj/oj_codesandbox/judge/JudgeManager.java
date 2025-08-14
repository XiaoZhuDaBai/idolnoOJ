package oj.oj_codesandbox.judge;


import oj.oj_codesandbox.judge.entity.JudgeContext;
import oj.oj_codesandbox.judge.entity.JudgeInfo;
import oj.oj_codesandbox.judge.strategy.JudgeStrategy;
import oj.oj_codesandbox.judge.strategy.impl.DefaultJudgeStrategy;
import oj.oj_codesandbox.judge.strategy.impl.JavaJudgeStrategy;
import oj.oj_codesandbox.model.dto.UserCommit;
import org.springframework.stereotype.Service;

@Service
public class JudgeManager {
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        UserCommit userCommit = judgeContext.getUserCommit();
        String language = userCommit.getLanguage();
        JudgeStrategy judgeStrategy = getStrategyByLanguage(language);
        return judgeStrategy.doJudge(judgeContext);
    }

    //todo
    public JudgeInfo doTest(JudgeContext judgeContext) {
        String language = judgeContext.getLanguage();
        JudgeStrategy judgeStrategy = getStrategyByLanguage(language);
        return judgeStrategy.doTest(judgeContext);
    }

    //todo 补充语言种类的
    private JudgeStrategy getStrategyByLanguage(String language) {
        JudgeStrategy judgeStrategy;
        if ("java".equals(language)) {
            judgeStrategy = new JavaJudgeStrategy();
        } else {
            judgeStrategy = new DefaultJudgeStrategy();
        }
        return judgeStrategy;
    }
}
