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

package org.apache.shardingsphere.proxy.backend.communication;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Hold SQL statement database for current thread.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementDatabaseHolder {
    
    private static final ThreadLocal<String> SQL_STATEMENT_DATABASE = new ThreadLocal<>();
    
    /**
     * Set SQL statement database.
     *
     * @param database SQL statement database
     */
    public static void set(final String database) {
        SQL_STATEMENT_DATABASE.set(database);
    }
    
    /**
     * Get SQL statement database.
     *
     * @return SQL statement database
     */
    public static String get() {
        return SQL_STATEMENT_DATABASE.get();
    }
    
    /**
     * Remove SQL statement database.
     */
    public static void remove() {
        SQL_STATEMENT_DATABASE.remove();
    }
}
