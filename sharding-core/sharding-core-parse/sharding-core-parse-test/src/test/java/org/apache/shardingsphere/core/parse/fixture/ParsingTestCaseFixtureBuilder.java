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

package org.apache.shardingsphere.core.parse.fixture;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.metadata.table.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.config.encrypt.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlRootShardingConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParsingTestCaseFixtureBuilder {
    
    /**
     * Build sharding rule.
     * 
     * @return sharding rule
     */
    @SneakyThrows
    public static ShardingRule buildShardingRule() {
        URL url = ParsingTestCaseFixtureBuilder.class.getClassLoader().getResource("yaml/sharding-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot find parse rule yaml configuration.");
        YamlRootShardingConfiguration yamlShardingConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlRootShardingConfiguration.class);
        return new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(yamlShardingConfig.getShardingRule()), yamlShardingConfig.getDataSources().keySet());
    }
    
    /**
     * Build encrypt rule.
     *
     * @return encrypt rule
     */
    @SneakyThrows
    public static EncryptRule buildEncryptRule() {
        URL url = ParsingTestCaseFixtureBuilder.class.getClassLoader().getResource("yaml/encrypt-rule.yaml");
        Preconditions.checkNotNull(url, "Cannot find parse rule yaml configuration.");
        YamlEncryptRuleConfiguration encryptConfig = YamlEngine.unmarshal(new File(url.getFile()), YamlEncryptRuleConfiguration.class);
        return new EncryptRule(new EncryptRuleConfigurationYamlSwapper().swap(encryptConfig));
    }
    
    /**
     * Build sharding table meta data.
     * 
     * @return sharding table meta data
     */
    public static ShardingTableMetaData buildShardingTableMetaData() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("t_order",
                new TableMetaData(Arrays.asList(new ColumnMetaData("order_id", "int", true), new ColumnMetaData("user_id", "int", false), 
                        new ColumnMetaData("status", "int", false)), Collections.<String>emptySet()));
        tableMetaDataMap.put("t_order_item", new TableMetaData(Arrays.asList(new ColumnMetaData("item_id", "int", true), new ColumnMetaData("order_id", "int", false),
                new ColumnMetaData("user_id", "int", false), new ColumnMetaData("status", "varchar", false), 
                new ColumnMetaData("c_date", "timestamp", false)), Collections.<String>emptySet()));
        tableMetaDataMap.put("t_encrypt", new TableMetaData(Arrays.asList(new ColumnMetaData("id", "int", true), new ColumnMetaData("name", "varchar", false),
                new ColumnMetaData("mobile", "varchar", false), new ColumnMetaData("status", "int", false)), Collections.<String>emptySet()));
        return new ShardingTableMetaData(tableMetaDataMap);
    }
}
