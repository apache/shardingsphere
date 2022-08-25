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

package org.apache.shardingsphere.data.pipeline.core.prepare.datasource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

import java.util.Optional;

/**
 * Data source preparer factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourcePreparerFactory {
    
    static {
        ShardingSphereServiceLoader.register(DataSourcePreparer.class);
    }
    
    /**
     * Get Data source preparer instance.
     *
     * @param databaseType database type
     * @return data source preparer instance
     */
    public static Optional<DataSourcePreparer> getInstance(final String databaseType) {
        return TypedSPIRegistry.findRegisteredService(DataSourcePreparer.class, databaseType);
    }
}
