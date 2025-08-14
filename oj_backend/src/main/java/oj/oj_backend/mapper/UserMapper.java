package oj.oj_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import oj.oj_backend.model.response.MyRecentWrongSubmissionResponse;
import oj.oj_backend.model.response.MySubmissionDataResponse;
import oj.oj_backend.model.response.SubmissionResponse;
import org.apache.ibatis.annotations.Param;
import oj.oj_backend.model.User;
import oj.oj_backend.model.response.MyRecentSubmissionResponse;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {
    int insert(User user);
    String getUUId(@Param("email") String email);

    List<MyRecentSubmissionResponse> getMyRecentSubmission(@Param("uuid")String uuid, @Param("page")int page);
    int getMyRecentSubmissionCount(@Param("uuid")String uuid);
    List<MyRecentWrongSubmissionResponse> getMyRecentWrongSubmission(@Param("uuid")String uuid);
    List<MySubmissionDataResponse> getMySubmissionData(@Param("uuid")String uuid);
    List<SubmissionResponse> getCommits(@Param("problemName") String problemName,
                                        @Param("language") String language,
                                        @Param("status") String status,
                                        @Param("userType") String userType,
                                        @Param("uuid") String uuid,
                                        @Param("page") int page);
    int getCommitsCount(@Param("problemName") String problemName,
                        @Param("language") String language,
                        @Param("status") String status,
                        @Param("userType") String userType,
                        @Param("uuid") String uuid);
}
