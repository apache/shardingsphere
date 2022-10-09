/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.spring.boot.datasource;

import lombok.SneakyThrows;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;

/**
 * Aop proxy utils.
 */
public final class AopProxyUtils {
    
    /**
     * Get target object.
     * 
     * @param proxy proxy object
     * @return target object
     */
    @SneakyThrows
    public static Object getTarget(final Object proxy) {
        if (AopUtils.isJdkDynamicProxy(proxy)) {
            return getJdkDynamicProxyTargetObject(proxy);
        }
        if (AopUtils.isCglibProxy(proxy)) {
            return getCglibProxyTargetObject(proxy);
        }
        return proxy;
    }
    
    private static Object getJdkDynamicProxyTargetObject(final Object proxy) throws Exception {
        AopProxy aopProxy = getJdkDynamicAopProxy(proxy);
        Field advisedField = aopProxy.getClass().getDeclaredField("advised");
        advisedField.setAccessible(true);
        return ((AdvisedSupport) advisedField.get(aopProxy)).getTargetSource().getTarget();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static AopProxy getJdkDynamicAopProxy(final Object proxy) {
        Field hField = proxy.getClass().getSuperclass().getDeclaredField("h");
        hField.setAccessible(true);
        return (AopProxy) hField.get(proxy);
    }
    
    private static Object getCglibProxyTargetObject(final Object proxy) throws Exception {
        Object advisedInterceptor = getCglibAdvisedInterceptor(proxy);
        Field advisedField = advisedInterceptor.getClass().getDeclaredField("advised");
        advisedField.setAccessible(true);
        return ((AdvisedSupport) advisedField.get(advisedInterceptor)).getTargetSource().getTarget();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static Object getCglibAdvisedInterceptor(final Object proxy) {
        Field callbackField = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        callbackField.setAccessible(true);
        return callbackField.get(proxy);
    }
}
