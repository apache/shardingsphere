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

package org.apache.shardingsphere.infra.rule.identifier.type;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.event.DataSourceStatusChangedEvent;

import java.util.Collection;

/**
 * Dynamic data source contained rule.
 */
public interface DynamicDataSourceContainedRule extends ShardingSphereRule {
    
    /**
     * Get primary data source name.
     *
     * @param dataSourceName data source name
     * @return primary data source name
     */
    String getPrimaryDataSourceName(String dataSourceName);
    
    /**
     * Get replica data source names.
     *
     * @param dataSourceName data source name
     * @return replica data source names
     */
    Collection<String> getReplicaDataSourceNames(String dataSourceName);
    
    /**
     * Update data source status.
     *
     * @param event data source status changed event
     */
    void updateStatus(DataSourceStatusChangedEvent event);
    
    /**
     * Restart heart beat job.
     *
     * @param event data source status changed event
     */
    void restartHeartBeatJob(DataSourceStatusChangedEvent event);
    
    /**
     * Close heart beat.
     */
    void closeHeartBeatJob();
}
