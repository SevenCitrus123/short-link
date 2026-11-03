package project.common.enums;

import project.common.convention.errorcode.IErrorCode;

public enum ShortLinkErrorCodeEnum implements IErrorCode {
    SHORT_LINK_NULL("B000200", "短链接不存在"),

    SHORT_LINK_ORIGIN_URL_NULL("B000201", "短链接的原链接为空"),

    SHORT_LINK_DOMAIN_NULL("B000202", "短链接的域名为空"),

    SHORT_LINK_VALIDATE_ERROR("B000203", "短链接时间不合法"),

    SHORT_LINK_DESCRIBE_TOO_LONG("B000204", "短链接的描述过长"),

    SHORT_LINK_ALREADY_EXIST("B000205", "短链接已存在"),

    SHORT_LINK_TRYS_UP_TO_5("B000206", "尝试次数到达5次"),

    SHORT_LINK_ARGS_INVALID("B000207", "短链接不合法"),

    SHORT_LINK_OPERATION_FAIL("B000208", "操作失败");

    private final String code;

    private final String message;

    ShortLinkErrorCodeEnum(String code, String message) {
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
