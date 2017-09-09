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

package com.dangdang.ddframe.rdb.sharding.api.rule;

import com.dangdang.ddframe.rdb.sharding.api.config.TableRuleConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ComplexShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.HintShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.InlineShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.ShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.api.config.strategy.StandardShardingStrategyConfig;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGenerator;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.complex.ComplexKeysShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.complex.ComplexShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.hint.HintShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.hint.HintShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.inline.InlineShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.none.NoneShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.standard.PreciseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.standard.RangeShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.standard.StandardShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.util.InlineExpressionParser;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Table rule configuration.
 * 
 * @author zhangliang
 */
@Getter
@ToString
public final class TableRule {
    
    private final String logicTable;
    
    private final boolean dynamic;
    
    private final List<DataNode> actualTables;
    
    private final ShardingStrategy databaseShardingStrategy;
    
    private final ShardingStrategy tableShardingStrategy;
    
    private final String generateKeyColumn;
    
    private final KeyGenerator keyGenerator;
    
    public TableRule(final TableRuleConfig config, final DataSourceRule dataSourceRule) {
        logicTable = config.getLogicTable();
        dynamic = config.isDynamic();
        List<String> dataSourceNames = new InlineExpressionParser(config.getDataSourceNames()).evaluate();
        List<String> actualTables = new InlineExpressionParser(config.getActualTables()).evaluate();
        databaseShardingStrategy = getShardingStrategy(config.getDatabaseShardingStrategy());
        tableShardingStrategy = getShardingStrategy(config.getTableShardingStrategy());
        if (dynamic) {
            Preconditions.checkNotNull(dataSourceRule);
            this.actualTables = generateDataNodes(dataSourceRule);
        } else if (null == actualTables || actualTables.isEmpty()) {
            Preconditions.checkNotNull(dataSourceRule);
            this.actualTables = generateDataNodes(Collections.singletonList(logicTable), dataSourceRule, dataSourceNames);
        } else {
            this.actualTables = generateDataNodes(actualTables, dataSourceRule, dataSourceNames);
        }
        if (null == config.getGenerateKeyStrategy()) {
            generateKeyColumn = null;
            keyGenerator = null;
        } else {
            generateKeyColumn = config.getGenerateKeyStrategy().getColumnName();
            keyGenerator = newInstance(config.getGenerateKeyStrategy().getKeyGeneratorClass(), KeyGenerator.class);
        }
    }
    
    private ShardingStrategy getShardingStrategy(final ShardingStrategyConfig config) {
        if (null == config) {
            return null;
        }
        if (config instanceof StandardShardingStrategyConfig) {
            StandardShardingStrategyConfig standardShardingStrategyConfig = (StandardShardingStrategyConfig) config;
            Preconditions.checkNotNull(standardShardingStrategyConfig.getShardingColumn(), "Sharding column cannot be null.");
            Preconditions.checkNotNull(standardShardingStrategyConfig.getPreciseAlgorithmClassName(), "Precise sharding algorithm cannot be null.");
            return new StandardShardingStrategy(standardShardingStrategyConfig.getShardingColumn(), 
                    newInstance(standardShardingStrategyConfig.getPreciseAlgorithmClassName(), PreciseShardingAlgorithm.class), 
                    newInstance(standardShardingStrategyConfig.getRangeAlgorithmClassName(), RangeShardingAlgorithm.class));
        }
        if (config instanceof ComplexShardingStrategyConfig) {
            ComplexShardingStrategyConfig complexShardingStrategyConfig = (ComplexShardingStrategyConfig) config;
            Preconditions.checkNotNull(complexShardingStrategyConfig.getShardingColumns(), "Sharding columns cannot be null.");
            Preconditions.checkNotNull(complexShardingStrategyConfig.getAlgorithmClassName(), "Sharding algorithm cannot be null.");
            return new ComplexShardingStrategy(split(complexShardingStrategyConfig.getShardingColumns()),
                    newInstance(complexShardingStrategyConfig.getAlgorithmClassName(), ComplexKeysShardingAlgorithm.class));
        }
        if (config instanceof HintShardingStrategyConfig) {
            HintShardingStrategyConfig hintShardingStrategyConfig = (HintShardingStrategyConfig) config;
            Preconditions.checkNotNull(hintShardingStrategyConfig.getAlgorithmClassName(), "Sharding algorithm cannot be null.");
            return new HintShardingStrategy(newInstance(hintShardingStrategyConfig.getAlgorithmClassName(), HintShardingAlgorithm.class));
        }
        if (config instanceof InlineShardingStrategyConfig) {
            InlineShardingStrategyConfig inlineShardingStrategyConfig = (InlineShardingStrategyConfig) config;
            Preconditions.checkNotNull(inlineShardingStrategyConfig.getShardingColumn(), "Sharding column cannot be null.");
            Preconditions.checkNotNull(inlineShardingStrategyConfig.getAlgorithmInlineExpression(), "Inline expression cannot be null.");
            return new InlineShardingStrategy(inlineShardingStrategyConfig.getShardingColumn(), inlineShardingStrategyConfig.getAlgorithmInlineExpression());
        }
        return new NoneShardingStrategy();
    }
    
    private List<String> split(final String value) {
        return Splitter.on(",").trimResults().splitToList(value);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T newInstance(final String className, final Class<T> classType) {
        if (null == className) {
            return null;
        }
        try {
            return (T) classType.getClassLoader().loadClass(className).newInstance();
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
    
    private List<DataNode> generateDataNodes(final DataSourceRule dataSourceRule) {
        Collection<String> dataSourceNames = dataSourceRule.getDataSourceNames();
        List<DataNode> result = new ArrayList<>(dataSourceNames.size());
        for (String each : dataSourceNames) {
            result.add(new DynamicDataNode(each));
        }
        return result;
    }
    
    private List<DataNode> generateDataNodes(final List<String> actualTables, final DataSourceRule dataSourceRule, final Collection<String> actualDataSourceNames) {
        Collection<String> dataSourceNames = getDataSourceNames(dataSourceRule, actualDataSourceNames);
        List<DataNode> result = new ArrayList<>(actualTables.size() * (dataSourceNames.isEmpty() ? 1 : dataSourceNames.size()));
        for (String actualTable : actualTables) {
            if (DataNode.isValidDataNode(actualTable)) {
                result.add(new DataNode(actualTable));
            } else {
                for (String dataSourceName : dataSourceNames) {
                    result.add(new DataNode(dataSourceName, actualTable));
                }
            }
        }
        return result;
    }
    
    private Collection<String> getDataSourceNames(final DataSourceRule dataSourceRule, final Collection<String> actualDataSourceNames) {
        if (null == dataSourceRule) {
            return Collections.emptyList();
        }
        if (null == actualDataSourceNames || actualDataSourceNames.isEmpty()) {
            return dataSourceRule.getDataSourceNames();
        }
        return actualDataSourceNames;
    }
    
    /**
     * Get actual data nodes via target data source and actual tables.
     *
     * @param targetDataSource target data source name
     * @param targetTables target actual tables.
     * @return actual data nodes
     */
    public Collection<DataNode> getActualDataNodes(final String targetDataSource, final Collection<String> targetTables) {
        return dynamic ? getDynamicDataNodes(targetDataSource, targetTables) : getStaticDataNodes(targetDataSource, targetTables);
    }
    
    private Collection<DataNode> getDynamicDataNodes(final String targetDataSource, final Collection<String> targetTables) {
        Collection<DataNode> result = new LinkedHashSet<>(targetTables.size());
        for (String each : targetTables) {
            result.add(new DataNode(targetDataSource, each));
        }
        return result;
    }
    
    private Collection<DataNode> getStaticDataNodes(final String targetDataSource, final Collection<String> targetTables) {
        Collection<DataNode> result = new LinkedHashSet<>(actualTables.size());
        for (DataNode each : actualTables) {
            if (targetDataSource.equals(each.getDataSourceName()) && targetTables.contains(each.getTableName())) {
                result.add(each);
            }
        }
        return result;
    }
    
    /**
     * Get actual data source names.
     *
     * @return actual data source names
     */
    public Collection<String> getActualDatasourceNames() {
        Collection<String> result = new LinkedHashSet<>(actualTables.size());
        for (DataNode each : actualTables) {
            result.add(each.getDataSourceName());
        }
        return result;
    }
    
    /**
     * Get actual table names via target data source name.
     *
     * @param targetDataSource target data source name
     * @return names of actual tables
     */
    public Collection<String> getActualTableNames(final String targetDataSource) {
        Collection<String> result = new LinkedHashSet<>(actualTables.size());
        for (DataNode each : actualTables) {
            if (targetDataSource.equals(each.getDataSourceName())) {
                result.add(each.getTableName());
            }
        }
        return result;
    }
    
    int findActualTableIndex(final String dataSourceName, final String actualTableName) {
        int result = 0;
        for (DataNode each : actualTables) {
            if (each.getDataSourceName().equalsIgnoreCase(dataSourceName) && each.getTableName().equalsIgnoreCase(actualTableName)) {
                return result;
            }
            result++;
        }
        return -1;
    }
}
