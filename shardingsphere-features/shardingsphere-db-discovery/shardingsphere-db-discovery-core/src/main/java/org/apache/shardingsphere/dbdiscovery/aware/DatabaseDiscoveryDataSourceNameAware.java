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

package org.apache.shardingsphere.dbdiscovery.aware;

import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.infra.aware.DataSourceNameAware;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;

/**
 * Database discovery data source name aware.
 */
public final class DatabaseDiscoveryDataSourceNameAware implements DataSourceNameAware {
    
    private DatabaseDiscoveryRule rule;
    
    @Override
    public void setRule(final ShardingSphereRule rule) {
        this.rule = (DatabaseDiscoveryRule) rule;
    }
    
    @Override
    public String getPrimaryDataSourceName(final String dataSourceName) {
        return rule.getDataSourceRules().get(dataSourceName).getPrimaryDataSourceName();
    }
    
    @Override
    public Collection<String> getReplicaDataSourceNames(final String dataSourceName) {
        return rule.getDataSourceRules().get(dataSourceName).getReplicaDataSourceNames();
    }
    
    @Override
    public String getType() {
        return "database-discovery";
    }
}
