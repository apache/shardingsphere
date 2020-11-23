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
 *
 */

package org.apache.shardingsphere.agent.core.plugin;

import lombok.SneakyThrows;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Plugins loader.
 * TODO not-implemented yet
 */
public class PluginLoader extends ClassLoader {

    private final ConcurrentHashMap<String, Object> objectPool = new ConcurrentHashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * To find all intercepting target classes then to build TypeMatcher.
     *
     * @return TypeMatcher
     */
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return null;
    }

    /**
     * To detect the type whether or not exists.
     *
     * @param typeDescription TypeDescription
     * @return contains when it is true.
     */
    public boolean containsType(final TypeDescription typeDescription) {
        return false;
    }

    /**
     * Load the definition configuration by TypeDescription.
     *
     * @param typeDescription TypeDescription
     * @return the plugin definition configurations.
     */
    public PluginAdviceDefine loadPluginAdviceDefine(final TypeDescription typeDescription) {
        return null;
    }

    /**
     * To get or create instance of the advice class. Create new one and caching when it is not exist.
     *
     * @param classNameOfAdvice the class name of advice
     * @param <T> the advice type.
     * @return instance of advice
     */
    @SneakyThrows({ClassNotFoundException.class, IllegalAccessException.class, InstantiationException.class})
    public <T> T getInstance(final String classNameOfAdvice) {

        if (objectPool.containsKey(classNameOfAdvice)) {
            return (T) objectPool.get(classNameOfAdvice);
        }

        lock.lock();
        try {
            Object inst = objectPool.get(classNameOfAdvice);
            if (Objects.isNull(inst)) {
                inst = Class.forName(classNameOfAdvice, true, this)
                        .newInstance();
                objectPool.put(classNameOfAdvice, inst);
            }
            return (T) inst;
        } finally {
            lock.unlock();
        }
    }
}
