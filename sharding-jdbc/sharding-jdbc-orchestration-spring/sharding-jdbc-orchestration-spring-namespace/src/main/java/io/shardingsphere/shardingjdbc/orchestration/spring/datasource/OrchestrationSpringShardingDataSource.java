/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.orchestration.spring.datasource;

import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Orchestration sharding datasource for spring namespace.
 *
 * @author panjuan
 */
public final class OrchestrationSpringShardingDataSource extends OrchestrationShardingDataSource {
    
    public OrchestrationSpringShardingDataSource(final DataSource dataSource, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super((ShardingDataSource) dataSource, orchestrationConfig);
    }
    
    public OrchestrationSpringShardingDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        super(orchestrationConfig);
    }
}
