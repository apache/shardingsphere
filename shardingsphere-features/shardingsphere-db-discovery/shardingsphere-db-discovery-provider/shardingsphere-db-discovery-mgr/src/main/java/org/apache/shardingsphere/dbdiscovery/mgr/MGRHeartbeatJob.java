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

package org.apache.shardingsphere.dbdiscovery.mgr;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * MGR heartbeat job.
 */
@RequiredArgsConstructor
public final class MGRHeartbeatJob implements SimpleJob {
    
    private final DatabaseDiscoveryType databaseDiscoveryType;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final String schemaName;
    
    private final Collection<String> disabledDataSourceNames;
    
    private final String groupName;
    
    private final String primaryDataSourceName;
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        databaseDiscoveryType.updatePrimaryDataSource(dataSourceMap, schemaName, disabledDataSourceNames, groupName, primaryDataSourceName);
        databaseDiscoveryType.updateMemberState(dataSourceMap, schemaName, disabledDataSourceNames);
    }
}
