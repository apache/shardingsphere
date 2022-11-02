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

package org.apache.shardingsphere.proxy.backend.handler.admin.postgresql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

/**
 * Factory for {@link PostgreSQLSessionVariableHandler}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLSessionVariableHandlerFactory {
    
    static {
        ShardingSphereServiceLoader.register(PostgreSQLSessionVariableHandler.class);
    }
    
    /**
     * Get {@link PostgreSQLSessionVariableHandler} for specific variable.
     *
     * @param variableName variable name
     * @return {@link PostgreSQLSessionVariableHandler} for variable
     */
    public static PostgreSQLSessionVariableHandler getHandler(final String variableName) {
        return TypedSPIRegistry.findRegisteredService(PostgreSQLSessionVariableHandler.class, variableName).orElseGet(DefaultPostgreSQLSessionVariableHandler::new);
    }
}
