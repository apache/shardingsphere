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

package org.apache.shardingsphere.example.governance.raw.jdbc.config.cloud;

import org.apache.shardingsphere.driver.governance.api.GovernanceShardingSphereDataSourceFactory;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class CloudReplicaQueryConfiguration implements ExampleConfiguration {
    
    private final GovernanceConfiguration governanceConfig;
    
    public CloudReplicaQueryConfiguration(final GovernanceConfiguration governanceConfig) {
        this.governanceConfig = governanceConfig;
    }
    
    @Override
    public DataSource getDataSource() throws SQLException {
        return GovernanceShardingSphereDataSourceFactory.createDataSource(governanceConfig);
    }
}
