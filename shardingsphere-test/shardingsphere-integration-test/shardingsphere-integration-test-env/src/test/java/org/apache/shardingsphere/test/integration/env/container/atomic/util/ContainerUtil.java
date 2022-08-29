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

package org.apache.shardingsphere.test.integration.env.container.atomic.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Container util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContainerUtil {
    
    private static final AtomicInteger ATOMIC_MYSQL_SERVER_ID = new AtomicInteger(1);
    
    private static final AtomicInteger ATOMIC_ADAPTOR_CONTAINER_ID = new AtomicInteger(1);
    
    private static final AtomicInteger ATOMIC_STORAGE_CONTAINER_ID = new AtomicInteger(1);
    
    /**
     * Generate a unique MySQL server id.
     * 
     * @return unique MySQL server id
     */
    public static int generateMySQLServerId() {
        return ATOMIC_MYSQL_SERVER_ID.getAndIncrement();
    }
    
    /**
     * Generate a unique adapter container id.
     *
     * @return unique adapter container id
     */
    public static int generateAdaptorContainerId() {
        return ATOMIC_ADAPTOR_CONTAINER_ID.getAndIncrement();
    }
    
    /**
     * Generate a unique storage container id.
     *
     * @return unique storage container id
     */
    public static int generateStorageContainerId() {
        return ATOMIC_STORAGE_CONTAINER_ID.getAndIncrement();
    }
}
