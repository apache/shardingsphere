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

package org.apache.shardingsphere.proxy.frontend;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Connection ID generator.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class ConnectionIdGenerator {
    
    private static final ConnectionIdGenerator INSTANCE = new ConnectionIdGenerator();
    
    private int currentId;
    
    /**
     * Get instance.
     * 
     * @return instance
     */
    public static ConnectionIdGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get next connection ID.
     * 
     * @return next connection ID
     */
    public synchronized int nextId() {
        if (currentId >= Integer.MAX_VALUE) {
            currentId = 0;
        }
        return ++currentId;
    }
}
