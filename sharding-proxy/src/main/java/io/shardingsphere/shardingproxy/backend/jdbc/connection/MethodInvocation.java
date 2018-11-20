/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.jdbc.connection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

/**
 * Reflective method invocation.
 * 
 * @author zhaojun
 */
@RequiredArgsConstructor
class MethodInvocation {
    
    @Getter
    private final Method method;
    
    @Getter
    private final Object[] arguments;
    
    /**
     * Invoke method.
     * 
     * @param target target object
     */
    @SneakyThrows
    void invoke(final Object target) {
        method.invoke(target, arguments);
    }
}
