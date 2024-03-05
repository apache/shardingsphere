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

/**
 * Reflect Context.
 * Avoid using JDK21 bytecode during compilation. Refer to `org.graalvm.polyglot.Context`.
 */
public final class ReflectContext implements AutoCloseable {
    
    private static final String CONTEXT_CLASS_NAME = "org.graalvm.polyglot.Context";
    
    private final Object contextInstance;
    
    /**
     * This method is a simulation of the following operation.
     * // CHECKSTYLE:OFF
     * <pre class="code">
     * private final Context context = Context.newBuilder()
     *             .allowAllAccess(true)
     *             .option("java.Classpath", JAVA_CLASSPATH)
     *             .build();
     * </pre>
     * // CHECKSTYLE:ON
     * TODO <a href="https://github.com/oracle/graal/issues/4555">oracle/graal#4555</a> not yet closed.
     * Maybe sometimes shardingsphere need `.option("java.Properties.org.graalvm.home", System.getenv("JAVA_HOME")).
     *
     * @param javaClassPath java class path
     */
    @SneakyThrows
    public ReflectContext(final String javaClassPath) {
        Object builderInstance = Class.forName(CONTEXT_CLASS_NAME)
                .getMethod("newBuilder", String[].class)
                .invoke(null, (Object) new String[0]);
        builderInstance = builderInstance.getClass()
                .getMethod("allowAllAccess", boolean.class)
                .invoke(builderInstance, true);
        builderInstance = builderInstance.getClass()
                .getMethod("option", String.class, String.class)
                .invoke(builderInstance, "java.Classpath", javaClassPath);
        contextInstance = builderInstance.getClass()
                .getMethod("build")
                .invoke(builderInstance);
    }
    
    /**
     * Returns a value that represents the top-most bindings of a language.
     *
     * @param languageId languageId
     * @return {@link org.apache.shardingsphere.infra.expr.espresso.ReflectValue}
     */
    @SneakyThrows
    public ReflectValue getBindings(final String languageId) {
        Object valueInstance = Class.forName(CONTEXT_CLASS_NAME)
                .getMethod("getBindings", String.class)
                .invoke(contextInstance, languageId);
        return new ReflectValue(valueInstance);
    }
    
    @Override
    @SneakyThrows
    public void close() {
        Class.forName(CONTEXT_CLASS_NAME).getMethod("close").invoke(contextInstance);
    }
}
