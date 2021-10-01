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

package org.apache.shardingsphere.driver.state;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Driver state context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DriverStateContext {
    
    private static final Map<String, DriverState> STATES;
    
    static {
        // TODO add singleton cache with TypedSPI init
        ShardingSphereServiceLoader.register(DriverState.class);
        Collection<DriverState> driverStates = ShardingSphereServiceLoader.getSingletonServiceInstances(DriverState.class);
        STATES = new HashMap<>();
        for (DriverState each : driverStates) {
            STATES.put(each.getType(), each);
        }
    }
    
    /**
     * Get connection.
     *
     * @param schemaName schema name
     * @param contextManager context manager
     * @return connection
     */
    public static Connection getConnection(final String schemaName, final ContextManager contextManager) {
        return STATES.get(contextManager.getStateContext().getCurrentState().name()).getConnection(schemaName, contextManager);
    }
}
