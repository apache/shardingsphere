/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.orchestration.spring.datasource;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.orchestration.api.config.OrchestrationConfiguration;
import io.shardingjdbc.orchestration.internal.OrchestrationFacade;
import io.shardingjdbc.orchestration.reg.base.CoordinatorRegistryCenter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration sharding datasource for spring namespace.
 *
 * @author caohao
 */
public class OrchestrationSpringShardingDataSource extends ShardingDataSource {
    
    private final OrchestrationConfiguration config;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRuleConfiguration shardingRuleConfig;
    
    private final Properties props;
    
    public OrchestrationSpringShardingDataSource(final String name, final boolean overwrite, final CoordinatorRegistryCenter regCenter, final Map<String, DataSource> dataSourceMap, 
                                                 final ShardingRuleConfiguration shardingRuleConfig, final Properties props) throws SQLException {
        super(shardingRuleConfig.build(dataSourceMap), props);
        this.dataSourceMap = dataSourceMap;
        this.shardingRuleConfig = shardingRuleConfig;
        this.props = props;
        config = new OrchestrationConfiguration(name, regCenter, overwrite);
    }
    
    /**
     * Initial all orchestration actions for sharding data source.
     */
    public void init() {
        new OrchestrationFacade(config).initShardingOrchestration(dataSourceMap, shardingRuleConfig, props, this);
    }
}
