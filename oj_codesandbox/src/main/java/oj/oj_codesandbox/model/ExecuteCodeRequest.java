package oj.oj_codesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeRequest implements Serializable {
    private static final long serialVersionUID = 13579L;
    private String code;
    private String userInput;
    private String language;
    private String problemId;
    private String uuid;
    private String commitId;
}
