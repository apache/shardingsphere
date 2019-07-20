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

package org.apache.shardingsphere.shardingproxy.backend.schema;

import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.config.encrypt.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.util.ConfigurationLogger;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.EncryptRuleChangedEvent;
import org.apache.shardingsphere.shardingproxy.config.yaml.YamlDataSourceParameter;

import java.util.Map;

/**
 * Encrypt schema.
 *
 * @author panjuan
 * @author sunbufu
 */
@Getter
public final class EncryptSchema extends LogicSchema {
    
    private final ShardingMetaData metaData;
    
    private final ShardingRule shardingRule;
    
    private EncryptRule encryptRule;
    
    public EncryptSchema(final String name, final Map<String, YamlDataSourceParameter> dataSources, final EncryptRuleConfiguration encryptRuleConfiguration) {
        super(name, dataSources);
        encryptRule = new EncryptRule(encryptRuleConfiguration);
        shardingRule = new ShardingRule(new ShardingRuleConfiguration(), getDataSources().keySet());
        metaData = createShardingMetaData();
    }
    
    private ShardingMetaData createShardingMetaData() {
        ShardingDataSourceMetaData shardingDataSourceMetaData = new ShardingDataSourceMetaData(getDataSourceURLs(getDataSources()), shardingRule, LogicSchemas.getInstance().getDatabaseType());
        ShardingTableMetaData shardingTableMetaData = new ShardingTableMetaData(getTableMetaDataInitializer(shardingDataSourceMetaData).load(shardingRule));
        return new ShardingMetaData(shardingDataSourceMetaData, shardingTableMetaData);
    }
    
    /**
     * Renew encrypt rule.
     *
     * @param encryptRuleChangedEvent encrypt configuration changed event
     */
    @Subscribe
    @SneakyThrows
    public synchronized void renew(final EncryptRuleChangedEvent encryptRuleChangedEvent) {
        ConfigurationLogger.log(encryptRuleChangedEvent.getEncryptRuleConfiguration());
        encryptRule = new EncryptRule(encryptRuleChangedEvent.getEncryptRuleConfiguration());
    }
}
