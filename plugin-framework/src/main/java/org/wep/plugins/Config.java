package org.wep.plugins;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Config {
    /**
     * 配置文件名称, 缺省为"{插件id}.yml"
     *
     * @return
     */
    String value() default "";
}
