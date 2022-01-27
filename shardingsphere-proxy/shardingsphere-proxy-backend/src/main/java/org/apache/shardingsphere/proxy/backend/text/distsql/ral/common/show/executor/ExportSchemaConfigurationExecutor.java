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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.ExportSchemaConfigurationStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Export schema configuration executor.
 */
@RequiredArgsConstructor
public final class ExportSchemaConfigurationExecutor extends AbstractShowExecutor {
    
    private static final String CONFIG = "config";
    
    private static final String COLON = ":";
    
    private static final String SPACE = " ";
    
    private static final String NEWLINE = "\n";
    
    private static final String COLON_SPACE = COLON + SPACE;
    
    private static final String COLON_NEWLINE = COLON + NEWLINE;
    
    private static final String INDENT = SPACE + SPACE;
    
    private static final String STANDARD = "standard";
    
    private static final String COMPLEX = "complex";
    
    private static final String HINT = "hint";
    
    private static final String NONE = "none";
    
    private static final String SHARDING_COLUMN = "shardingColumn";
    
    private static final String SHARDING_ALGORITHM_NAME = "shardingAlgorithmName";
    
    private static final String TYPE = "type";
    
    private static final String PROPS = "props";
    
    private static final int ZERO = 0;
    
    private static final int ONE = 1;
    
    private static final int TWO = 2;
    
    private static final int THREE = 3;
    
    private static final int FOUR = 4;
    
    private static final int FIVE = 5;
    
    private static final int SIX = 6;
    
    private final ExportSchemaConfigurationStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        return Arrays.asList(new QueryHeader("", "", CONFIG, CONFIG, Types.VARCHAR, "VARCHAR", 128, 0, false, false, false, false));
    }
    
    @Override
    protected MergedResult createMergedResult() {
        String schemaName = getSchemaName();
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getMetaData(schemaName);
        StringBuilder result = new StringBuilder();
        configItem(ZERO, "schemaName", schemaName, result);
        getDataSourcesConfig(metaData, result);
        Optional<ShardingRuleConfiguration> ruleConfig = metaData.getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ShardingRuleConfiguration).map(each -> (ShardingRuleConfiguration) each).findAny();
        ruleConfig.ifPresent(rule -> getRulesConfig(rule, result));
        if (!sqlStatement.getFilePath().isPresent()) {
            return new MultipleLocalDataMergedResult(Collections.singleton(Collections.singletonList(result.toString())));
        }
        
        File outFile = new File(sqlStatement.getFilePath().get());
        try (FileOutputStream stream = new FileOutputStream(outFile)) {
            stream.write(result.toString().getBytes());
            stream.flush();
        } catch (final IOException ex) {
            throw new ShardingSphereException(ex);
        }
        return null;
    }
    
    private void getDataSourcesConfig(final ShardingSphereMetaData metaData, final StringBuilder result) {
        if (null == metaData.getResource().getDataSources() || metaData.getResource().getDataSources().isEmpty()) {
            return;
        }
        configItem(ZERO, "dataSources", result);
        for (Map.Entry<String, DataSource> each : metaData.getResource().getDataSources().entrySet()) {
            configItem(ONE, each.getKey(), result);
            DataSourceProperties dataSourceProps = DataSourcePropertiesCreator.create(each.getValue());
            dataSourceProps.getAllLocalProperties().entrySet().forEach(standard -> {
                configItem(TWO, standard.getKey(), standard.getValue(), result);
            });
        }
    }
    
    private void getRulesConfig(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        configItem(ZERO, "rules", result);
        result.append("- !SHARDING").append(NEWLINE);
        getTablesConfig(ruleConfig, result);
        getBindingTablesConfig(ruleConfig, result);
        getDefaultDatabaseStrategyConfig(ruleConfig, result);
        getDefaultTableStrategyConfig(ruleConfig, result);
        getShardingAlgorithmsConfig(ruleConfig, result);
        getKeyGeneratorsConfig(ruleConfig, result);
        getScalingConfig(ruleConfig, result);
    }
    
    private void getTablesConfig(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getTables().isEmpty() && ruleConfig.getAutoTables().isEmpty()) {
            return;
        }
        configItem(ONE, "tables", result);
        getTableRulesConfig(ruleConfig, result);
        getAutoTableRulesConfig(ruleConfig, result);
    }
    
    private void getTableRulesConfig(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        for (ShardingTableRuleConfiguration each : ruleConfig.getTables()) {
            getTableRulesItemConfig(each.getLogicTable(), each.getActualDataNodes(), each.getTableShardingStrategy(), each.getKeyGenerateStrategy(), result);
        }
    }
    
    private void getAutoTableRulesConfig(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        for (ShardingAutoTableRuleConfiguration each : ruleConfig.getAutoTables()) {
            getTableRulesItemConfig(each.getLogicTable(), each.getActualDataSources(), each.getShardingStrategy(), each.getKeyGenerateStrategy(), result);
        }
    }
    
    private void getTableRulesItemConfig(final String logicTable, final String actualDataNodes, final ShardingStrategyConfiguration tableShardingStrategy,
                                         final KeyGenerateStrategyConfiguration keyGenerateStrategy, final StringBuilder result) {
        configItem(TWO, logicTable, result);
        configItem(THREE, "actualDataNodes", actualDataNodes, result);
        configItem(THREE, "tableStrategy", result);
        String algorithmName = "";
        String shardingColumn = "";
        String shardingAlgorithmName = "";
        if (tableShardingStrategy instanceof StandardShardingStrategyConfiguration) {
            StandardShardingStrategyConfiguration strategyConfig = (StandardShardingStrategyConfiguration) tableShardingStrategy;
            algorithmName = STANDARD;
            shardingColumn = strategyConfig.getShardingColumn();
            shardingAlgorithmName = strategyConfig.getShardingAlgorithmName();
        } else if (tableShardingStrategy instanceof ComplexShardingStrategyConfiguration) {
            ComplexShardingStrategyConfiguration strategyConfig = (ComplexShardingStrategyConfiguration) tableShardingStrategy;
            algorithmName = COMPLEX;
            shardingColumn = strategyConfig.getShardingColumns();
            shardingAlgorithmName = strategyConfig.getShardingAlgorithmName();
        } else if (tableShardingStrategy instanceof HintShardingStrategyConfiguration) {
            HintShardingStrategyConfiguration strategyConfig = (HintShardingStrategyConfiguration) tableShardingStrategy;
            algorithmName = HINT;
            shardingAlgorithmName = strategyConfig.getShardingAlgorithmName();
        } else if (tableShardingStrategy instanceof NoneShardingStrategyConfiguration) {
            algorithmName = NONE;
        }
        if (!Strings.isNullOrEmpty(algorithmName)) {
            configItem(FOUR, algorithmName, result);
        }
        if (!Strings.isNullOrEmpty(shardingColumn)) {
            configItem(FIVE, SHARDING_COLUMN, shardingColumn, result);
        }
        if (!Strings.isNullOrEmpty(shardingAlgorithmName)) {
            configItem(FIVE, SHARDING_ALGORITHM_NAME, shardingAlgorithmName, result);
        }
        if (null == keyGenerateStrategy) {
            return;
        }
        configItem(THREE, "keyGenerateStrategy", result);
        configItem(FOUR, "column", keyGenerateStrategy.getColumn(), result);
        configItem(FOUR, "keyGeneratorName", keyGenerateStrategy.getKeyGeneratorName(), result);
    }
    
    private void getBindingTablesConfig(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getBindingTableGroups().isEmpty()) {
            return;
        }
        configItem(ONE, "bindingTables", result);
        result.append(indent(TWO)).append("-").append(SPACE).append(String.join(",", ruleConfig.getBindingTableGroups())).append(NEWLINE);
    }
    
    private void getDefaultDatabaseStrategyConfig(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (null == ruleConfig.getDefaultDatabaseShardingStrategy()) {
            return;
        }
        configItem(ONE, "defaultDatabaseStrategy", result);
        getCommonDefaultStrategyConfig(ruleConfig.getDefaultDatabaseShardingStrategy(), result);
    }
    
    private void getDefaultTableStrategyConfig(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (null == ruleConfig.getDefaultTableShardingStrategy()) {
            return;
        }
        configItem(ONE, "defaultTableStrategy", result);
        getCommonDefaultStrategyConfig(ruleConfig.getDefaultTableShardingStrategy(), result);
    }
    
    private void getShardingAlgorithmsConfig(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getShardingAlgorithms().isEmpty()) {
            return;
        }
        configItem(ONE, "shardingAlgorithms", result);
        getAlgorithmConfig(ruleConfig.getShardingAlgorithms(), result);
    }
    
    private void getKeyGeneratorsConfig(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig.getKeyGenerators().isEmpty()) {
            return;
        }
        configItem(ONE, "keyGenerators", result);
        getAlgorithmConfig(ruleConfig.getKeyGenerators(), result);
    }
    
    private void getScalingConfig(final ShardingRuleConfiguration ruleConfig, final StringBuilder result) {
        if (Strings.isNullOrEmpty(ruleConfig.getScalingName())) {
            return;
        }
        configItem(ONE, "scalingName", ruleConfig.getScalingName(), result);
        if (ruleConfig.getScaling().isEmpty()) {
            return;
        }
        configItem(ONE, "scaling", result);
        for (Map.Entry<String, OnRuleAlteredActionConfiguration> each : ruleConfig.getScaling().entrySet()) {
            OnRuleAlteredActionConfiguration current = each.getValue();
            configItem(TWO, each.getKey(), result);
            if (null != current) {
                getScalingInputOrOutputConfig("input", current.getInput().getWorkerThread(), current.getInput().getBatchSize(), current.getInput().getRateLimiter(), result);
                getScalingInputOrOutputConfig("output", current.getOutput().getWorkerThread(), current.getOutput().getBatchSize(), current.getOutput().getRateLimiter(), result);
                getCommonScalingItemConfig("streamChannel", current.getStreamChannel(), result);
                getCommonScalingItemConfig("completionDetector", current.getCompletionDetector(), result);
                getCommonScalingItemConfig("dataConsistencyChecker", current.getDataConsistencyChecker(), result);
            }
        }
    }
    
    private void getScalingInputOrOutputConfig(final String itemName, final int workerThread, final int batchSize,
                                               final ShardingSphereAlgorithmConfiguration rateLimiterConfig, final StringBuilder result) {
        configItem(THREE, itemName, result);
        configItem(FOUR, "workerThread", workerThread, result);
        configItem(FOUR, "batchSize", batchSize, result);
        configItem(FOUR, "rateLimiter", result);
        configItem(FIVE, TYPE, rateLimiterConfig.getType(), result);
        if (null != rateLimiterConfig.getProps() && !rateLimiterConfig.getProps().isEmpty()) {
            configItem(FIVE, PROPS, result);
            for (Map.Entry<Object, Object> each : rateLimiterConfig.getProps().entrySet()) {
                configItem(SIX, each.getKey(), each.getValue(), result);
            }
        }
    }
    
    private void getCommonScalingItemConfig(final String itemName, final ShardingSphereAlgorithmConfiguration itemConfig, final StringBuilder result) {
        configItem(THREE, itemName, result);
        configItem(FOUR, TYPE, itemConfig.getType(), result);
        if (null != itemConfig.getProps() && !itemConfig.getProps().isEmpty()) {
            configItem(FOUR, PROPS, result);
            for (Map.Entry<Object, Object> each : itemConfig.getProps().entrySet()) {
                configItem(FIVE, each.getKey(), each.getValue(), result);
            }
        }
    }
    
    private void getAlgorithmConfig(final Map<String, ShardingSphereAlgorithmConfiguration> algorithmMap, final StringBuilder result) {
        for (Map.Entry<String, ShardingSphereAlgorithmConfiguration> each : algorithmMap.entrySet()) {
            configItem(TWO, each.getKey(), result);
            configItem(THREE, TYPE, each.getValue().getType(), result);
            if (null != each.getValue().getProps() && !each.getValue().getProps().isEmpty()) {
                configItem(THREE, PROPS, result);
                for (Map.Entry<Object, Object> each1 : each.getValue().getProps().entrySet()) {
                    configItem(FOUR, each1.getKey(), each1.getValue(), result);
                }
            }
        }
    }
    
    private void getCommonDefaultStrategyConfig(final ShardingStrategyConfiguration ruleConfig, final StringBuilder result) {
        if (ruleConfig instanceof StandardShardingStrategyConfiguration) {
            StandardShardingStrategyConfiguration strategyConfig = (StandardShardingStrategyConfiguration) ruleConfig;
            configItem(TWO, STANDARD, result);
            configItem(THREE, SHARDING_COLUMN, strategyConfig.getShardingColumn(), result);
            configItem(THREE, SHARDING_ALGORITHM_NAME, strategyConfig.getShardingAlgorithmName(), result);
        } else if (ruleConfig instanceof ComplexShardingStrategyConfiguration) {
            ComplexShardingStrategyConfiguration strategyConfig = (ComplexShardingStrategyConfiguration) ruleConfig;
            configItem(TWO, COMPLEX, result);
            configItem(THREE, SHARDING_COLUMN, strategyConfig.getShardingColumns(), result);
            configItem(THREE, SHARDING_ALGORITHM_NAME, strategyConfig.getShardingAlgorithmName(), result);
        } else if (ruleConfig instanceof HintShardingStrategyConfiguration) {
            HintShardingStrategyConfiguration strategyConfig = (HintShardingStrategyConfiguration) ruleConfig;
            configItem(TWO, HINT, result);
            configItem(THREE, SHARDING_ALGORITHM_NAME, strategyConfig.getShardingAlgorithmName(), result);
        } else if (ruleConfig instanceof NoneShardingStrategyConfiguration) {
            configItem(TWO, NONE, result);
        }
    }
    
    private void configItem(final int indent, final Object key, final StringBuilder result) {
        result.append(indent(indent)).append(key).append(COLON_NEWLINE);
    }
    
    private void configItem(final int indent, final Object key, final Object value, final StringBuilder result) {
        result.append(indent(indent)).append(key).append(COLON_SPACE).append(value).append(NEWLINE);
    }
    
    private String indent(final int count) {
        if (count <= 0) {
            return "";
        }
        if (1 == count) {
            return INDENT;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(INDENT);
        }
        return result.toString();
    }
    
    private String getSchemaName() {
        String result = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : connectionSession.getSchemaName();
        if (null == result) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(result)) {
            throw new SchemaNotExistedException(result);
        }
        return result;
    }
}
