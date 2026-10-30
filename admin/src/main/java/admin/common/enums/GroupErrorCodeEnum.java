package admin.common.enums;

import admin.common.convention.errorcode.IErrorCode;

public enum GroupErrorCodeEnum implements IErrorCode {
    GROUP_NULL("B000300", "短链接组异常"),

    GROUP_NAME_EXIST("B000301", "短链接组名已存在"),

    GROUP_SAVE_FAIL("B000302", "短链接组保存失败");

    private final String code;

    private final String message;

    GroupErrorCodeEnum(String code, String message) {
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
