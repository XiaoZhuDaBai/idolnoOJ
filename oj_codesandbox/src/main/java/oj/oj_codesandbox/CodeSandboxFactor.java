package oj.oj_codesandbox;


import oj.oj_codesandbox.codesandbox.RemoteCodeSandbox;
import oj.oj_codesandbox.codesandbox.ThirdCodeSandbox;

/**
 * 通过静态工厂方法获取不同类型的CodeSandbox实例
 */
public interface CodeSandboxFactor {
    public static CodeSandbox newInstance(String type) {
        switch (type) {
            case "remote":
                return new RemoteCodeSandbox();
            case "third":
                return new ThirdCodeSandbox();
            default:
                throw new RuntimeException("没有实现这个沙箱");
        }
    }
}
