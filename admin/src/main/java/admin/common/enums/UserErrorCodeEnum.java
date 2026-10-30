package admin.common.enums;

import admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {
    USER_NULL("B000200", "用户记录不存在"),

    USER_NAME_EXIST("B000201", "用户名已存在"),

    USER_NAME_NOT_EXIST("B000202", "用户名不存在"),

    USER_SAVE_ERROR("B000203", "用户记录保存失败"),

    USER_ALREADY_LOGIN("B000204", "用户已登录"),

    USER_NOT_LOG("B000204" , "用户未登录");

    private final String code;

    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
