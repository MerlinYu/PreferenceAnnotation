package com.lucky;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yuchao on 8/26/16.
 * use for class not include inner class
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PreferenceItem {
  String tableName() default "preference_annotation";
}
