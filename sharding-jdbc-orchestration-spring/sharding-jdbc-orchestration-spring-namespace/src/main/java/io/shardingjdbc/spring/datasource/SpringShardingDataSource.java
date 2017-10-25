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

package io.shardingjdbc.spring.datasource;

import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.ShardingRule;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Sharding datasource for spring namespace.
 *
 * @author caohao
 * @author zhanglaing
 */
public class SpringShardingDataSource extends ShardingDataSource implements ApplicationContextAware {
    
    @Setter
    private ApplicationContext applicationContext;
    
    public SpringShardingDataSource(final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig, final Properties props) throws SQLException {
        super(shardingRuleConfig.build(dataSourceMap), props);
    }
    
    @Override
    public void renew(final ShardingRule newShardingRule, final Properties newProps) throws SQLException {
        for (Entry<String, DataSource> entry : newShardingRule.getDataSourceMap().entrySet()) {
            if (entry.getValue() instanceof MasterSlaveDataSource) {
                for (Entry<String, DataSource> masterSlaveEntry : ((MasterSlaveDataSource) entry.getValue()).getAllDataSources().entrySet()) {
                    DataSourceBeanUtil.createDataSourceBean(applicationContext, masterSlaveEntry.getKey(), masterSlaveEntry.getValue());
                }
            } else {
                DataSourceBeanUtil.createDataSourceBean(applicationContext, entry.getKey(), entry.getValue());
            }
        }
        super.renew(newShardingRule, newProps);
    }
}
