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

package org.apache.shardingsphere.database.connector.core.metadata.detector;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

/**
 * System table engine.
 */
public final class SystemTableEngine {
    
    /**
     * Judge whether the table is system table or not.
     *
     * @param databaseType database type
     * @param tableName table name
     * @return whether the table is system table or not
     */
    public static boolean isSystemTable(final DatabaseType databaseType, final String tableName) {
        return DatabaseTypedSPILoader.findService(DialectSystemTableRule.class, databaseType)
                .map(rule -> rule.isSystemTable(tableName))
                .orElseGet(() -> SystemTableDetector.hasSystemTables(databaseType.getType())
                        ? SystemTableDetector.isSystemTable(databaseType.getType(), null, tableName)
                        : tableName.contains("$") || tableName.contains("/") || tableName.contains("##"));
    }
}
