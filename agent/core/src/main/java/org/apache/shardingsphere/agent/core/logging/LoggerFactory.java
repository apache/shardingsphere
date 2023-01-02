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

package org.apache.shardingsphere.agent.core.logging;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.core.classloader.AgentClassLoader;
import org.apache.shardingsphere.agent.core.plugin.PluginJar;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Collections;
import java.util.Objects;
import java.util.jar.JarFile;

/**
 * Logger factory.
 */
public final class LoggerFactory {
    
    private static AgentClassLoader classLoader;
    
    /**
     * Get logger.
     * 
     * @param clazz Class
     * @return logger
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static Logger getLogger(final Class<?> clazz) {
        Class<?> factoryClazz = getClassLoader().loadClass("org.slf4j.LoggerFactory");
        Method method = factoryClazz.getMethod("getLogger", Class.class);
        Object log = method.invoke(null, clazz);
        return new Logger(log);
    }
    
    private static AgentClassLoader getClassLoader() {
        if (Objects.nonNull(classLoader)) {
            return classLoader;
        }
        CodeSource codeSource = LoggerFactory.class.getProtectionDomain().getCodeSource();
        try {
            File agentFle = new File(codeSource.getLocation().toURI());
            if (agentFle.isFile() && agentFle.getName().endsWith(".jar")) {
                PluginJar pluginJar = new PluginJar(new JarFile(agentFle, true), agentFle);
                classLoader = new AgentClassLoader(LoggerFactory.class.getClassLoader().getParent(), Collections.singleton(pluginJar));
                return classLoader;
            }
            classLoader = new AgentClassLoader(LoggerFactory.class.getClassLoader(), Collections.emptyList());
        } catch (final URISyntaxException | IOException ignored) {
        }
        return classLoader;
    }
    
    /**
     * Logger.
     */
    @RequiredArgsConstructor
    public static final class Logger {
        
        private final Object logger;
        
        /**
         * Info.
         * 
         * @param msg message
         */
        public void info(final String msg) {
            invokeMethod("info", msg);
        }
        
        /**
         * Info.
         * 
         * @param format format
         * @param arguments arguments
         */
        public void info(final String format, final Object... arguments) {
            invokeMethod("info", format, arguments);
        }
        
        /**
         * Debug.
         * 
         * @param format format
         * @param arguments arguments
         */
        public void debug(final String format, final Object... arguments) {
            invokeMethod("debug", format, arguments);
        }
        
        /**
         * Debug.
         * 
         * @param msg message
         */
        public void debug(final String msg) {
            invokeMethod("debug", msg);
        }
        
        /**
         * Error.
         * 
         * @param format format
         * @param arguments arguments
         */
        public void error(final String format, final Object... arguments) {
            invokeMethod("error", format, arguments);
        }
        
        /**
         * Error.
         * 
         * @param msg message
         */
        public void error(final String msg) {
            invokeMethod("error", msg);
        }
        
        @SneakyThrows(ReflectiveOperationException.class)
        private void invokeMethod(final String methodName, final String msg) {
            Class<?> actualLogger = LoggerFactory.getClassLoader().loadClass("org.slf4j.Logger");
            Method method = actualLogger.getMethod(methodName, String.class);
            method.invoke(logger, msg);
        }
        
        @SneakyThrows(ReflectiveOperationException.class)
        private void invokeMethod(final String methodName, final String msg, final Object... arguments) {
            Class<?> actualLogger = LoggerFactory.getClassLoader().loadClass("org.slf4j.Logger");
            Method method = actualLogger.getMethod(methodName, String.class, Object[].class);
            method.invoke(logger, msg, arguments);
        }
    }
}
