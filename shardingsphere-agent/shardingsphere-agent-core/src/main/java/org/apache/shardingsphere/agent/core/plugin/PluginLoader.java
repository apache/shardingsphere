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

package org.apache.shardingsphere.agent.core.plugin;

import net.bytebuddy.description.type.TypeDescription;
import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint;

public interface PluginLoader {
    
    /**
     * To detect the type whether or not exists.
     *
     * @param typeDescription type description
     * @return contains when it is true
     */
    boolean containsType(TypeDescription typeDescription);
    
    /**
     * Load plugin interceptor point by type description.
     *
     * @param typeDescription type description
     * @return plugin interceptor point
     */
    PluginInterceptorPoint loadPluginInterceptorPoint(TypeDescription typeDescription);
    
    /**
     * To get or create instance of the advice class. Create new one and caching when it is not exist.
     *
     * @param adviceClassName class name of advice
     * @param <T> advice type
     * @return instance of advice
     */
    <T> T getOrCreateInstance(String adviceClassName);
}
