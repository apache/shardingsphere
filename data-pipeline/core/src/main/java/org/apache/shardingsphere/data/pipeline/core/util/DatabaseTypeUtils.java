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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Database type utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class DatabaseTypeUtils {
    
    /**
     * Get type and branch types.
     *
     * @param databaseType database type
     * @return type and branch types
     */
    public static Collection<DatabaseType> getTypeAndBranchTypes(final String databaseType) {
        Collection<DatabaseType> result = new LinkedList<>();
        DatabaseType supportedDatabaseType = TypedSPILoader.getService(DatabaseType.class, databaseType);
        result.add(supportedDatabaseType);
        result.addAll(new DatabaseTypeRegistry(supportedDatabaseType).getAllBranchDatabaseTypes());
        return result;
    }
}
