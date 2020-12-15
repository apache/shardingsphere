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

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

import java.util.Optional;
import java.util.Properties;

/**
 * Database admin backend handler engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseAdminBackendHandlerEngineFactory {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseAdminBackendHandlerEngine.class);
    }
    
    /**
     * Create new instance of database admin backend handler engine.
     *
     * @param databaseType database type
     * @return new instance of database admin backend handler engine
     */
    public static Optional<DatabaseAdminBackendHandlerEngine> newInstance(final DatabaseType databaseType) {
        return TypedSPIRegistry.findRegisteredService(DatabaseAdminBackendHandlerEngine.class, databaseType.getName(), new Properties());
    }
}
