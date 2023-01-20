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

package org.apache.shardingsphere.agent.core.log;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.agent.core.path.AgentPath;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.JarFile;

/**
 * Agent logger factory.
 */
public final class AgentLoggerFactory {
    
    private static AgentLoggerClassLoader classLoader;
    
    /**
     * Get logger.
     * 
     * @param clazz class
     * @return logger
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static Logger getLogger(final Class<?> clazz) {
        Class<?> factoryClazz = getClassLoader().loadClass("org.slf4j.LoggerFactory");
        Method method = factoryClazz.getMethod("getLogger", Class.class);
        return new Logger(method.invoke(null, clazz));
    }
    
    private static AgentLoggerClassLoader getClassLoader() {
        if (null != classLoader) {
            return classLoader;
        }
        synchronized (AgentLoggerFactory.class) {
            if (null == classLoader) {
                classLoader = getAgentLoggerClassLoader();
            }
        }
        return classLoader;
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private static AgentLoggerClassLoader getAgentLoggerClassLoader() {
        File agentFle = new File(AgentLoggerFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (agentFle.isFile() && agentFle.getName().endsWith(".jar")) {
            Collection<JarFile> pluginJars = getPluginJars(getJarFiles(new File(String.join(File.separator, AgentPath.getRootPath().getPath(), "lib"))));
            File resourcePath = new File(String.join(File.separator, AgentPath.getRootPath().getPath(), "conf"));
            return new AgentLoggerClassLoader(pluginJars, resourcePath);
        }
        return new AgentLoggerClassLoader();
    }
    
    private static Collection<JarFile> getPluginJars(final Collection<File> jarFiles) throws IOException {
        Collection<JarFile> result = new LinkedList<>();
        for (File each : jarFiles) {
            result.add(new JarFile(each, true));
        }
        return result;
    }
    
    @SneakyThrows(IOException.class)
    private static Collection<File> getJarFiles(final File file) {
        Collection<File> result = new LinkedList<>();
        Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(final Path path, final BasicFileAttributes attributes) {
                if (path.toFile().isFile() && path.toFile().getName().endsWith(".jar")) {
                    result.add(path.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
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
            Class<?> actualLogger = AgentLoggerFactory.getClassLoader().loadClass("org.slf4j.Logger");
            Method method = actualLogger.getMethod(methodName, String.class);
            method.invoke(logger, msg);
        }
        
        @SneakyThrows(ReflectiveOperationException.class)
        private void invokeMethod(final String methodName, final String msg, final Object... arguments) {
            Class<?> actualLogger = AgentLoggerFactory.getClassLoader().loadClass("org.slf4j.Logger");
            Method method = actualLogger.getMethod(methodName, String.class, Object[].class);
            method.invoke(logger, msg, arguments);
        }
    }
}
