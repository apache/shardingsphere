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

package org.apache.shardingsphere.data.pipeline.api;

import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPIRegistry;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

/**
 * Pipeline job public API factory.
 */
public final class PipelineJobPublicAPIFactory {
    
    static {
        ShardingSphereServiceLoader.register(InventoryIncrementalJobPublicAPI.class);
        ShardingSphereServiceLoader.register(MigrationJobPublicAPI.class);
        ShardingSphereServiceLoader.register(ConsistencyCheckJobPublicAPI.class);
    }
    
    /**
     * Get instance of inventory incremental job public API.
     *
     * @param jobTypeName job type name
     * @return got instance
     */
    public static InventoryIncrementalJobPublicAPI getInventoryIncrementalJobPublicAPI(final String jobTypeName) {
        return TypedSPIRegistry.getRegisteredService(InventoryIncrementalJobPublicAPI.class, jobTypeName);
    }
    
    /**
     * Get instance of migration job public API.
     *
     * @return got instance
     */
    public static MigrationJobPublicAPI getMigrationJobPublicAPI() {
        return RequiredSPIRegistry.getRegisteredService(MigrationJobPublicAPI.class);
    }
    
    /**
     * Get instance of consistency check job public API.
     *
     * @return got instance
     */
    public static ConsistencyCheckJobPublicAPI getConsistencyCheckJobPublicAPI() {
        return RequiredSPIRegistry.getRegisteredService(ConsistencyCheckJobPublicAPI.class);
    }
}
