package com.dl.server.core;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)

/**
 * 注解<br>
 * 为包扫描做准备；
 * 
 * @author dl
 *
 */
public @interface ProxyAnntotation {
	Class<?>[] interfaces();
}
