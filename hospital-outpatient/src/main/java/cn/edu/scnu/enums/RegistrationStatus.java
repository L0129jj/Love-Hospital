package cn.edu.scnu.enums;

import lombok.Getter;

/**
 * 挂号状态枚举。
 * 对应 registration 表 status 字段的 ENUM 值。
 */
@Getter
public enum RegistrationStatus {

    PENDING("待就诊"),
    IN_PROGRESS("就诊中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String displayName;

    RegistrationStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 根据中文显示名查找枚举。
     */
    public static RegistrationStatus fromDisplayName(String displayName) {
        for (RegistrationStatus s : values()) {
            if (s.displayName.equals(displayName)) {
                return s;
            }
        }
        throw new IllegalArgumentException("未知的挂号状态: " + displayName);
    }
}
