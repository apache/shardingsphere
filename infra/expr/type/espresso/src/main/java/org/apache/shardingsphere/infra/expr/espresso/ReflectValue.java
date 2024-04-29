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

package org.apache.shardingsphere.infra.expr.espresso;

import lombok.SneakyThrows;

import java.util.stream.Stream;

/**
 * Reflect Value.
 * Avoid using JDK21 bytecode during compilation. Refer to `org.graalvm.polyglot.Value`.
 */
public class ReflectValue {
    
    private static final String VALUE_CLASS_NAME = "org.graalvm.polyglot.Value";
    
    private final Object valueInstance;
    
    public ReflectValue(final Object valueInstance) {
        this.valueInstance = valueInstance;
    }
    
    /**
     * Returns the member with a given identifier or null if the member does not exist.
     * @param identifier the member identifier
     * @return {@link org.apache.shardingsphere.infra.expr.espresso.ReflectValue}
     */
    @SneakyThrows
    public ReflectValue getMember(final String identifier) {
        Object resultValueInstance = Class.forName(VALUE_CLASS_NAME)
                .getMethod("getMember", String.class)
                .invoke(valueInstance, identifier);
        return new ReflectValue(resultValueInstance);
    }
    
    /**
     * Instantiates this value if it can be instantiated.
     * @param arguments the arguments
     * @return {@link org.apache.shardingsphere.infra.expr.espresso.ReflectValue}
     */
    @SneakyThrows
    public ReflectValue newInstance(final Object... arguments) {
        Object resultValueInstance = Class.forName(VALUE_CLASS_NAME)
                .getMethod("newInstance", Object[].class)
                .invoke(valueInstance, new Object[]{Stream.of(arguments).toArray()});
        return new ReflectValue(resultValueInstance);
    }
    
    /**
     * Invokes the given member of this value.
     * @param identifier the member identifier to invoke
     * @param arguments the invocation arguments
     * @return {@link org.apache.shardingsphere.infra.expr.espresso.ReflectValue}
     */
    @SneakyThrows
    public ReflectValue invokeMember(final String identifier, final Object... arguments) {
        Object resultValueInstance = Class.forName(VALUE_CLASS_NAME)
                .getMethod("invokeMember", String.class, Object[].class)
                .invoke(valueInstance, identifier, arguments);
        return new ReflectValue(resultValueInstance);
    }
    
    /**
     * Returns true if this value represents a string.
     * @return Returns true if this value represents a string.
     */
    @SneakyThrows
    public boolean isString() {
        return (boolean) Class.forName(VALUE_CLASS_NAME)
                .getMethod("isString")
                .invoke(valueInstance);
    }
    
    /**
     * Maps a polyglot value to a value with a given Java target type.
     * @param targetType the target Java type to map
     * @param <T> target type
     * @return target type
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public <T> T as(final Class<T> targetType) {
        return (T) Class.forName(VALUE_CLASS_NAME)
                .getMethod("as", Class.class)
                .invoke(valueInstance, targetType);
    }
}
