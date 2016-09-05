package com.lucky;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yuchao on 8/26/16.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface PreferenceField {

  String defaultValue() default "";

//  String returnType() default "String";
//  String returnPreferenceName() default "";
}
