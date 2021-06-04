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

package org.apache.shardingsphere.sharding.converter;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.FunctionSegment;
import org.apache.shardingsphere.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ShardingBindingTableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.keygen.YamlKeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlHintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.strategy.sharding.YamlStandardShardingStrategyConfiguration;

import java.util.Collection;

/**
 * Sharding rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRuleStatementConverter {
    
    /**
     * Convert create sharding table rule statement context to YAML sharding rule configuration.
     * 
     * @param sqlStatement create sharding table rule statement
     * @return YAML sharding rule configuration
     */
    public static YamlShardingRuleConfiguration convert(final CreateShardingTableRuleStatement sqlStatement) {
        return convertTableRuleSegments(sqlStatement.getTables());
    }

    /**
     * Convert alter sharding table rule statement context to YAML sharding rule configuration.
     *
     * @param sqlStatement alter sharding table rule statement
     * @return YAML sharding rule configuration
     */
    public static YamlShardingRuleConfiguration convert(final AlterShardingTableRuleStatement sqlStatement) {
        return convertTableRuleSegments(sqlStatement.getTables());
    }
    
    /**
     * Convert create sharding binding table rule statement context to YAML sharding rule configuration.
     *
     * @param sqlStatement create sharding binding table rule statement
     * @return YAML sharding rule configuration
     */
    public static YamlShardingRuleConfiguration convert(final CreateShardingBindingTableRulesStatement sqlStatement) {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        for (ShardingBindingTableRuleSegment each : sqlStatement.getRules()) {
            result.getBindingTables().add(each.getTables());
        }
        return result;
    }

    /**
     * Convert alter sharding binding table rule statement context to YAML sharding rule configuration.
     *
     * @param sqlStatement alter sharding binding table rule statement
     * @return YAML sharding rule configuration
     */
    public static YamlShardingRuleConfiguration convert(final AlterShardingBindingTableRulesStatement sqlStatement) {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        for (ShardingBindingTableRuleSegment each : sqlStatement.getRules()) {
            result.getBindingTables().add(each.getTables());
        }
        return result;
    }
    
    private static YamlShardingRuleConfiguration convertTableRuleSegments(final Collection<TableRuleSegment> tableRuleSegments) {
        YamlShardingRuleConfiguration result = new YamlShardingRuleConfiguration();
        for (TableRuleSegment each : tableRuleSegments) {
            if (null != each.getTableStrategy()) {
                result.getShardingAlgorithms().put(getAlgorithmName(each.getLogicTable(), each.getTableStrategy().getAlgorithmName()), createAlgorithmConfiguration(each.getTableStrategy()));
                result.getAutoTables().put(each.getLogicTable(), createAutoTableRuleConfiguration(each));
            }
        }
        return result;
    }
    
    /**
     * Convert function segment to YAML algorithm configuration.
     *
     * @param segment function segment
     * @return YAML algorithm configuration
     */
    public static YamlShardingSphereAlgorithmConfiguration createAlgorithmConfiguration(final FunctionSegment segment) {
        YamlShardingSphereAlgorithmConfiguration result = new YamlShardingSphereAlgorithmConfiguration();
        result.setType(segment.getAlgorithmName());
        result.setProps(segment.getAlgorithmProps());
        return result;
    }
    
    private static YamlShardingAutoTableRuleConfiguration createAutoTableRuleConfiguration(final TableRuleSegment segment) {
        YamlShardingAutoTableRuleConfiguration result = new YamlShardingAutoTableRuleConfiguration();
        result.setLogicTable(segment.getLogicTable());
        result.setActualDataSources(Joiner.on(",").join(segment.getDataSources()));
        result.setShardingStrategy(createTableStrategyConfiguration(segment));
        return result;
    }
    
    /**
     * Convert table rule segment to YAML sharding strategy configuration.
     *
     * @param segment table rule segment
     * @return YAML sharding strategy configuration
     */
    public static YamlShardingStrategyConfiguration createTableStrategyConfiguration(final TableRuleSegment segment) {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        YamlStandardShardingStrategyConfiguration standard = new YamlStandardShardingStrategyConfiguration();
        standard.setShardingColumn(segment.getTableStrategyColumn());
        standard.setShardingAlgorithmName(getAlgorithmName(segment.getLogicTable(), segment.getTableStrategy().getAlgorithmName()));
        result.setStandard(standard);
        return result;
    }
    
    /**
     * Create YAML sharding strategy configuration.
     *
     * @param shardingColumn sharding column
     * @param segment function segment
     * @return YAML sharding strategy configuration
     */
    public static YamlShardingStrategyConfiguration createDefaultTableStrategyConfiguration(final String shardingColumn, final FunctionSegment segment) {
        YamlShardingStrategyConfiguration result = new YamlShardingStrategyConfiguration();
        if (null != shardingColumn) {
            YamlStandardShardingStrategyConfiguration standard = new YamlStandardShardingStrategyConfiguration();
            standard.setShardingColumn(shardingColumn);
            standard.setShardingAlgorithmName(segment.getAlgorithmName());
            result.setStandard(standard);
        } else {
            YamlHintShardingStrategyConfiguration hint = new YamlHintShardingStrategyConfiguration();
            hint.setShardingAlgorithmName(segment.getAlgorithmName());
            result.setHint(hint);
        }
        return result;
    }
    
    /**
     * Convert table rule segment to YAML key generate strategy configuration.
     *
     * @param segment table rule segment
     * @return YAML key generate strategy configuration
     */
    public static YamlKeyGenerateStrategyConfiguration createKeyGenerateStrategyConfiguration(final TableRuleSegment segment) {
        YamlKeyGenerateStrategyConfiguration result = new YamlKeyGenerateStrategyConfiguration();
        result.setColumn(segment.getKeyGenerateStrategyColumn());
        result.setKeyGeneratorName(getAlgorithmName(segment.getLogicTable(), segment.getKeyGenerateStrategy().getAlgorithmName()));
        return result;
    }
    
    /**
     * Generate real algorithm name.
     *
     * @param tableName table name
     * @param algorithmType algorithm name
     * @return real algorithm name
     */
    public static String getAlgorithmName(final String tableName, final String algorithmType) {
        return String.format("%s_%s", tableName, algorithmType);
    }
}
