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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql;

import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for {@link MySQLSessionVariableHandler}.
 */
public final class MySQLSessionVariableHandlerFactory {
    
    static {
        ShardingSphereServiceLoader.register(MySQLSessionVariableHandler.class);
    }
    
    /**
     * Get list of {@link MySQLSessionVariableHandler} for variables.
     *
     * @param variableNames variable names
     * @return {@link MySQLSessionVariableHandler} for variables
     */
    public static List<MySQLSessionVariableHandler> getHandlers(final List<String> variableNames) {
        List<MySQLSessionVariableHandler> result = new ArrayList<>(variableNames.size());
        for (String each : variableNames) {
            result.add(TypedSPIRegistry.findRegisteredService(MySQLSessionVariableHandler.class, each).orElseGet(DefaultMySQLSessionVariableHandler::new));
        }
        return result;
    }
}
