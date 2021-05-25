package cc.winfo.oauth2.server.constant;

/**
 * @Description: 认证模式枚举类
 * @ProjectName: com.uufund.auth.master
 * @Version: 1.0
 */


public enum GrantTypeEnum {
    IMPLICIT("implicit", "简化模式"),
    PASSWORD("password", "密码模式"),
    REFRESH_TOKEN("refresh_token", "刷新token"),
    CLIENT_CREDENTIALS("client_credentials", "客户端模式"),
    AUTHORIZATION_CODE("authorization_code", "授权码模式");

    private final String grant_type;
    private final String grant_name;

    GrantTypeEnum(String grant_type, String grant_name) {
        this.grant_type = grant_type;
        this.grant_name = grant_name;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public static String[] getGrantTypes() {
        String[] grants = new String[5];
        GrantTypeEnum[] grantTypeEnums = GrantTypeEnum.values();
        for (int i = 0; i < grantTypeEnums.length; i++) {
            grants[i] = grantTypeEnums[i].getGrant_type();
        }
        return grants;
    }
}


