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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPIRegistry;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

/**
 * Dialect query result data row loader factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DialectQueryResultDataRowLoaderFactory {
    
    static {
        ShardingSphereServiceLoader.register(DialectQueryResultDataRowLoader.class);
    }
    
    /**
     * Find instance of dialect query result data row loader factory.
     *
     * @param databaseType database type
     * @return found instance
     */
    public static DialectQueryResultDataRowLoader getInstance(final DatabaseType databaseType) {
        return TypedSPIRegistry.findRegisteredService(DialectQueryResultDataRowLoader.class, databaseType.getType())
                .orElseGet(() -> RequiredSPIRegistry.getRegisteredService(DialectQueryResultDataRowLoader.class));
    }
}
