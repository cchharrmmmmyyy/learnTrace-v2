package org.example.learntrace;

//错误常量
public class ErrorCode {
    private ErrorCode(){}


    // ==========业务错误码==========
    /** 用户名已存在 */
    public static final int USER_NAME_DUPLICATE = 1001;
    /** 用户名或密码错误 */
    public static final int USER_LOGIN_FAIL = 1002;
    /** 无权限，只能操作自己账号 */
    public static final int USER_NO_PERMISSION = 1003;
    /** 用户不存在 / 操作失败 */
    public static final int USER_OPERATE_FAIL = 1004;
    /** 1005 请求JSON格式解析错误 */
    public static final int JSON_FORMAT_ERROR = 1005;
    /** 1006 未登录或token无效 */
    public static final int TOKEN_INVALID = 1006;
    /** 1007 权限不足，禁止访问 */
    public static final int PERMISSION_DENIED = 1007;

    /** 服务器内部异常 */
    public static final int SERVER_ERROR = 5000;
}
