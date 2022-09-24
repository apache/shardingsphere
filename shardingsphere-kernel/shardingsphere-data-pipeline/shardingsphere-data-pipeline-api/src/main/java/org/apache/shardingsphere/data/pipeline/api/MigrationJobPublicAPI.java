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

import org.apache.shardingsphere.data.pipeline.api.pojo.CreateMigrationJobParameter;
import org.apache.shardingsphere.data.pipeline.api.pojo.MigrationJobInfo;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPI;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Migration job public API.
 */
@SingletonSPI
public interface MigrationJobPublicAPI extends InventoryIncrementalJobPublicAPI, RequiredSPI {
    
    /**
     * List all jobs.
     *
     * @return job infos
     */
    @Override
    List<MigrationJobInfo> list();
    
    /**
     * Add migration source resources.
     *
     * @param dataSourcePropsMap data source properties map
     */
    void addMigrationSourceResources(Map<String, DataSourceProperties> dataSourcePropsMap);
    
    /**
     * Drop migration source resources.
     *
     * @param resourceNames resource names
     */
    void dropMigrationSourceResources(Collection<String> resourceNames);
    
    /**
     * Query migration source resources list.
     *
     * @return migration source resources
     */
    Collection<Collection<Object>> listMigrationSourceResources();
    
    /**
     * Create job migration config and start.
     *
     * @param parameter create migration job parameter
     * @return job id
     */
    String createJobAndStart(CreateMigrationJobParameter parameter);
}
