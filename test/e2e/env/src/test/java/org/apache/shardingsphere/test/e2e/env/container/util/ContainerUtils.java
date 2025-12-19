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

package org.apache.shardingsphere.test.e2e.env.container.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Container utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContainerUtils {
    
    private static final AtomicInteger ATOMIC_MYSQL_SERVER_ID = new AtomicInteger(1);
    
    /**
     * Generate a unique MySQL server id.
     *
     * @return unique MySQL server id
     */
    public static int generateMySQLServerId() {
        return ATOMIC_MYSQL_SERVER_ID.getAndIncrement();
    }
}
