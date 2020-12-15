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

package org.apache.shardingsphere.agent.metrics.bootstrap;

/**
 * The enum Elapsed time thread local.
 */
public enum ElapsedTimeThreadLocal {
    
    /**
     * Instance elapsed time thread local.
     */
    INSTANCE;
    
    private static final ThreadLocal<Long> CURRENT_LOCAL = new ThreadLocal<>();
    
    /**
     * Set.
     *
     * @param time the time
     */
    public void set(final long time) {
        CURRENT_LOCAL.set(time);
    }
    
    /**
     * Get long.
     *
     * @return the long
     */
    public Long get() {
        return CURRENT_LOCAL.get();
    }
    
    /**
     * Remove.
     */
    public void remove() {
        CURRENT_LOCAL.remove();
    }
}
