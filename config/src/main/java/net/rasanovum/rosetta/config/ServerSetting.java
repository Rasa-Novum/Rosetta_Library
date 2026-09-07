package net.rasanovum.rosetta.config;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ServerSetting {
    int permissionLevel() default 2;
    boolean requireCheats() default false;
    String disabledDescription() default "";
}
