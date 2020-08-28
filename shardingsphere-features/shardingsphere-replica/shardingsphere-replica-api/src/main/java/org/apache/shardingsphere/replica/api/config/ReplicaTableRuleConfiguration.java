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

package org.apache.shardingsphere.replica.api.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;

/**
 * Replica routing configuration.
 */
@Getter
public final class ReplicaTableRuleConfiguration implements RuleConfiguration {
    
    private String physicsTable;
    
    private String replicaGroupId;
    
    private String replicaPeers;
    
    private String dataSourceName;
    
    public ReplicaTableRuleConfiguration() {
    }
    
    /**
     * Set physics table.
     *
     * @param physicsTable physics table
     */
    public void setPhysicsTable(final String physicsTable) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(physicsTable), "physicsTable is required.");
        this.physicsTable = physicsTable;
    }
    
    /**
     * Set replica group id.
     *
     * @param replicaGroupId replica group id
     */
    public void setReplicaGroupId(final String replicaGroupId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(replicaGroupId), "replicaGroupId is required.");
        this.replicaGroupId = replicaGroupId;
    }
    
    /**
     * Set replica peers.
     *
     * @param replicaPeers replica peers
     */
    public void setReplicaPeers(final String replicaPeers) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(replicaPeers), "replicaPeers is required.");
        this.replicaPeers = replicaPeers;
    }
    
    /**
     * Set data source name.
     *
     * @param dataSourceName data source name
     */
    public void setDataSourceName(final String dataSourceName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dataSourceName), "dataSourceName is required.");
        this.dataSourceName = dataSourceName;
    }
}
