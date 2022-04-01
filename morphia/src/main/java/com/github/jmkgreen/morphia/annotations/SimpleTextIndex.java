package com.github.jmkgreen.morphia.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: jamesgreen
 * Date: 25/03/2013
 * Time: 22:10
 * To change this template use File | Settings | File Templates.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SimpleTextIndex {

    /**
     * The name of the index to create; default is to let the mongodb create a name (in the form of key1_1/-1_key2_1/-1...
     */
    String name() default "";

    /**
     * The default language is english. Supply only MongoDB supported languages please.
     */
    String defaultLanguage() default "english";
}
