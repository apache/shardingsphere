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

package io.shardingsphere.shardingproxy.runtime;

import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.orchestration.internal.rule.OrchestrationShardingRule;
import io.shardingsphere.shardingproxy.backend.BackendExecutorContext;
import io.shardingsphere.shardingproxy.runtime.metadata.ProxyTableMetaDataConnectionManager;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding schema.
 *
 * @author zhangliang
 * @author zhangyonglun
 * @author panjuan
 * @author zhaojun
 * @author wangkai
 */
@Getter
public final class ShardingSchema extends LogicSchema {
    
    private final ShardingRule shardingRule;
    
    private final ShardingMetaData metaData;
    
    public ShardingSchema(final String name, final Map<String, DataSourceParameter> dataSources, final ShardingRuleConfiguration shardingRuleConfig, final boolean isUsingRegistry) {
        super(name, dataSources);
        shardingRule = getShardingRule(shardingRuleConfig, isUsingRegistry);
        metaData = getShardingMetaData(BackendExecutorContext.getInstance().getExecuteEngine());
    }
    
    private ShardingRule getShardingRule(final ShardingRuleConfiguration shardingRule, final boolean isUsingRegistry) {
        return isUsingRegistry ? new OrchestrationShardingRule(shardingRule, getDataSources().keySet()) : new ShardingRule(shardingRule, getDataSources().keySet());
    }
    
    private ShardingMetaData getShardingMetaData(final ShardingExecuteEngine executeEngine) {
        return new ShardingMetaData(getDataSourceURLs(getDataSources()), shardingRule,
                DatabaseType.MySQL, executeEngine, new ProxyTableMetaDataConnectionManager(getBackendDataSource()), GlobalRegistry.getInstance().getMaxConnectionsSizePerQuery());
    }
    
    private Map<String, String> getDataSourceURLs(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, String> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getUrl());
        }
        return result;
    }
}
