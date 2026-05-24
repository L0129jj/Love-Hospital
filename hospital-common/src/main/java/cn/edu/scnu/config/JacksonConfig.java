package cn.edu.scnu.config;

import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置。
 * 脱敏序列化器（PhoneMask / IdCardMask / NameMask）通过
 * {@code @JsonSerialize(using = ...)} 在 VO 字段上主动声明使用，
 * 无需在此处全局注册。
 */
@Configuration
public class JacksonConfig {
}
