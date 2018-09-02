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

package io.shardingsphere.jdbc.orchestration.api.datasource;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.jdbc.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationFacade;
import io.shardingsphere.jdbc.orchestration.internal.config.ConfigurationService;
import io.shardingsphere.jdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Orchestration sharding data source factory.
 *
 * @author zhangliang
 * @author caohao
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrchestrationShardingDataSourceFactory {
    
    /**
     * Create sharding data source.
     *
     * @param shardingDataSource sharding data source
     * @param orchestrationConfig orchestration configuration
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final ShardingDataSource shardingDataSource, final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        return new OrchestrationShardingDataSource(shardingDataSource, new OrchestrationFacade(orchestrationConfig));
    }
    
    /**
     * Create sharding data source.
     *
     * @param orchestrationConfig orchestration configuration
     * @return sharding data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final OrchestrationConfiguration orchestrationConfig) throws SQLException {
        OrchestrationFacade orchestrationFacade = new OrchestrationFacade(orchestrationConfig);
        ConfigurationService configService = orchestrationFacade.getConfigService();
        ShardingRuleConfiguration shardingRuleConfig = configService.loadShardingRuleConfiguration();
        Preconditions.checkNotNull(shardingRuleConfig, "Missing the sharding rule configuration on register center");
        ShardingDataSource shardingDataSource = new ShardingDataSource(configService.loadDataSourceMap(), 
                new ShardingRule(shardingRuleConfig, configService.loadDataSourceMap().keySet()), configService.loadShardingConfigMap(), configService.loadShardingProperties());
        return new OrchestrationShardingDataSource(shardingDataSource, orchestrationFacade);
    }
}
