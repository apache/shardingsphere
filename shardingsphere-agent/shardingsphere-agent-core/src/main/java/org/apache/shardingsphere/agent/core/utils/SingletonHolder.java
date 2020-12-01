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

package org.apache.shardingsphere.agent.core.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton holder.
 */
public enum SingletonHolder {
    
    /**
     * Instance singleton.
     */
    INSTANCE;
    
    private static final Map<String, Object> SINGLES = new ConcurrentHashMap<>();
    
    /**
     * Put entity object.
     *
     * @param entity entity object
     */
    public void put(final Object entity) {
        SINGLES.put(entity.getClass().getName(), entity);
    }
    
    /**
     * Get object.
     *
     * @param <T> type parameter
     * @param clazz clazz
     * @return object
     */
    public <T> T get(final Class<T> clazz) {
        return (T) SINGLES.get(clazz.getName());
    }
}
