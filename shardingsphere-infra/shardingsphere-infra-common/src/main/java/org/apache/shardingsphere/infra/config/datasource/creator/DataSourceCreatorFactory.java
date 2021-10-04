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

package org.apache.shardingsphere.infra.config.datasource.creator;

import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.required.RequiredSPIRegistry;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Properties;

/**
 * Data source creator factory.
 */
public final class DataSourceCreatorFactory {
    
    static {
        ShardingSphereServiceLoader.register(DataSourceCreator.class);
    }
    
    /**
     * Get data source creator.
     * 
     * @param dataSourceClassName data source class name
     * @return data source creator
     */
    public static DataSourceCreator getDataSourceCreator(final String dataSourceClassName) {
        return TypedSPIRegistry.findRegisteredService(DataSourceCreator.class, dataSourceClassName, new Properties()).orElse(RequiredSPIRegistry.getRegisteredService(DataSourceCreator.class));
    }
}
