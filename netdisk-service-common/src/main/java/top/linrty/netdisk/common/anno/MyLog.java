package top.linrty.netdisk.common.anno;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyLog {
    String module() default "";

    String operation() default "";

    String type() default "operation";

    String level() default "0";
}

