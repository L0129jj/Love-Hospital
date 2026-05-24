package cn.edu.scnu.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    String key() default "";
    int maxAttempts() default 10;
    int timeWindow() default 60;
    String message() default "请求过于频繁，请稍后再试";
}
