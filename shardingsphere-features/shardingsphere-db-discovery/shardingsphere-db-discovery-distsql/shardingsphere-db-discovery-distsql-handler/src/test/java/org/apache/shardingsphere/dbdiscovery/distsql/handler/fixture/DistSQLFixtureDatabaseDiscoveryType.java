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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.fixture;

import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.dbdiscovery.spi.status.HighlyAvailableStatus;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;

public final class DistSQLFixtureDatabaseDiscoveryType implements DatabaseDiscoveryType {
    
    @Override
    public HighlyAvailableStatus loadHighlyAvailableStatus(final DataSource dataSource) {
        return mock(HighlyAvailableStatus.class);
    }
    
    @Override
    public Optional<String> determinePrimaryDataSource(final Map<String, DataSource> dataSourceMap) {
        return Optional.empty();
    }
    
    @Override
    public void updateMemberState(final String databaseName, final Map<String, DataSource> dataSourceMap, final String groupName) {
    }
    
    @Override
    public String getPrimaryDataSource() {
        return null;
    }
    
    @Override
    public String getOldPrimaryDataSource() {
        return null;
    }
    
    @Override
    public void setOldPrimaryDataSource(final String oldPrimaryDataSource) {
    }
    
    @Override
    public String getType() {
        return "DISTSQL.FIXTURE";
    }
}
