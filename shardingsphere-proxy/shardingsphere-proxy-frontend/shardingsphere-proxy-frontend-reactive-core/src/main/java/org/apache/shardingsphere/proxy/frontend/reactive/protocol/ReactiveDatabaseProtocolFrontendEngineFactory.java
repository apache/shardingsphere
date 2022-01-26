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

package org.apache.shardingsphere.proxy.frontend.reactive.protocol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.proxy.frontend.reactive.spi.ReactiveDatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

/**
 * Reactive database protocol frontend engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReactiveDatabaseProtocolFrontendEngineFactory {
    
    static {
        ShardingSphereServiceLoader.register(ReactiveDatabaseProtocolFrontendEngine.class);
    }
    
    /**
     * Create new instance of reactive database protocol frontend engine.
     *
     * @param databaseType database type
     * @return new instance of reactive database protocol frontend engine
     */
    public static ReactiveDatabaseProtocolFrontendEngine newInstance(final String databaseType) {
        for (ReactiveDatabaseProtocolFrontendEngine each : ShardingSphereServiceLoader.newServiceInstances(ReactiveDatabaseProtocolFrontendEngine.class)) {
            if (DatabaseTypeRegistry.getActualDatabaseType(each.getDatabaseType()).getName().equals(databaseType)) {
                return each;
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot support database type '%s' in reactive", databaseType));
    }
}
