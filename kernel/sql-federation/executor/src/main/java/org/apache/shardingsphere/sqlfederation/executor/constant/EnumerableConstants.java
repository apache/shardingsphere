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

package org.apache.shardingsphere.sqlfederation.executor.constant;

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * Enumerable constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EnumerableConstants {
    
    public static final Collection<String> SYSTEM_CATALOG_TABLES = new CaseInsensitiveSet<>(3, 1F);
    
    public static final String DAT_COMPATIBILITY = "PG";
    
    public static final String PG_DATABASE = "pg_database";
    
    public static final String PG_TABLES = "pg_tables";
    
    public static final String PG_ROLES = "pg_roles";
    
    static {
        SYSTEM_CATALOG_TABLES.add(PG_DATABASE);
        SYSTEM_CATALOG_TABLES.add(PG_TABLES);
        SYSTEM_CATALOG_TABLES.add(PG_ROLES);
    }
}
