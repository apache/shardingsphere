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

package org.apache.shardingsphere.sqlfederation.compiler.sql.function.postgresql.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PostgreSQL system function.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLSystemFunction {
    
    /**
     * Mock pg_table_is_visible function.
     *
     * @param oid oid
     * @return true
     */
    @SuppressWarnings("unused")
    public static boolean pgTableIsVisible(final Long oid) {
        return true;
    }
    
    /**
     * Mock pg_get_userbyid function.
     *
     * @param oid oid
     * @return user name
     */
    @SuppressWarnings("unused")
    public static String pgGetUserById(final Long oid) {
        return "mock user";
    }
}
