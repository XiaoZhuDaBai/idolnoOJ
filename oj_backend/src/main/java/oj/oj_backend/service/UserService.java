package oj.oj_backend.service;


import com.baomidou.mybatisplus.extension.service.IService;
import oj.oj_backend.comm.ResponseResult;
import oj.oj_backend.model.User;
import oj.oj_backend.model.response.SubmissionResponse;
import oj.oj_backend.model.vo.LoginVo;
import oj.oj_backend.model.response.MyRecentSubmissionResponse;
import oj.oj_backend.model.response.MyRecentWrongSubmissionResponse;
import oj.oj_backend.model.response.MySubmissionDataResponse;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface UserService extends IService<User> {
    boolean isExistInTable(String email);
    ResponseResult<String> toLoginOrRegister(LoginVo loginVo);
    String getJWT(String uuid);
    String sendVerificationCode(String email);
    List<MyRecentWrongSubmissionResponse> getMyRecentWrongSubmission(String uuid);
    List<MyRecentSubmissionResponse> getMyRecentSubmission(String uuid, int page);
    int getMyRecentSubmissionCount(String uuid);
    List<MySubmissionDataResponse> getMySubmissionData(String uuid);
    List<SubmissionResponse> getCommits(String problemName, String language, String status, String userType, String uuid, int page);
    int getCommitsCount(String problemName, String language, String status, String userType, String uuid);
}
