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

package org.apache.shardingsphere.spring.namespace.orchestration;

import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Orchestration ShardingSphere datasource for spring namespace.
 */
public final class OrchestrationSpringShardingSphereDataSource extends OrchestrationShardingSphereDataSource {
    
    public OrchestrationSpringShardingSphereDataSource(final DataSource dataSource, final OrchestrationConfiguration orchestrationConfig) {
        super((ShardingSphereDataSource) dataSource, orchestrationConfig);
    }
    
    public OrchestrationSpringShardingSphereDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(orchestrationConfig);
    }
    
    public OrchestrationSpringShardingSphereDataSource(final DataSource dataSource,
                                                       final OrchestrationConfiguration orchestrationConfig, final ClusterConfiguration clusterConfiguration) {
        super((ShardingSphereDataSource) dataSource, orchestrationConfig, clusterConfiguration);
    }
    
    public OrchestrationSpringShardingSphereDataSource(final OrchestrationConfiguration orchestrationConfig, final ClusterConfiguration clusterConfiguration) throws SQLException {
        super(orchestrationConfig, clusterConfiguration);
    }
}
