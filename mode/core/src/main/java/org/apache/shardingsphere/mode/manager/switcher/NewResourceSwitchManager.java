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

package org.apache.shardingsphere.mode.manager.switcher;

import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;

import java.util.Collections;
import java.util.Map;

/**
 * TODO Rename ResourceSwitchManager when metadata structure adjustment completed. #25485
 * Resource switch manager.
 */
public final class NewResourceSwitchManager {
    
    /**
     * Register storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param dataSourceProps data source properties
     * @return created switching resource
     */
    public SwitchingResource registerStorageUnit(final ShardingSphereResourceMetaData resourceMetaData, final Map<String, DataSourceProperties> dataSourceProps) {
        return new SwitchingResource(resourceMetaData, DataSourcePoolCreator.create(dataSourceProps), Collections.emptyMap());
    }
    
    /**
     * Alter storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param storageUnitName storage unit name
     * @param dataSourceProps data source properties
     * @return created switching resource
     */
    public SwitchingResource alterStorageUnit(final ShardingSphereResourceMetaData resourceMetaData, final String storageUnitName, final Map<String, DataSourceProperties> dataSourceProps) {
        return new SwitchingResource(resourceMetaData, DataSourcePoolCreator.create(dataSourceProps),
                Collections.singletonMap(storageUnitName, resourceMetaData.getDataSources().remove(storageUnitName)));
    }
    
    /**
     * Unregister storage unit.
     *
     * @param resourceMetaData resource meta data
     * @param storageUnitName storage unit name
     * @return created switching resource
     */
    public SwitchingResource unregisterStorageUnit(final ShardingSphereResourceMetaData resourceMetaData, final String storageUnitName) {
        return new SwitchingResource(resourceMetaData, Collections.emptyMap(), Collections.singletonMap(storageUnitName, resourceMetaData.getDataSources().remove(storageUnitName)));
    }
}
