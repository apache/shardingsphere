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

import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGenerator;
import com.dangdang.ddframe.rdb.sharding.keygen.KeyGeneratorFactory;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
    
    private final DatabaseShardingStrategy databaseShardingStrategy;
    
    private final TableShardingStrategy tableShardingStrategy;
    
    private final String generateKeyColumn;
    
    private final KeyGenerator keyGenerator;
    
    /**
     * Constructs a full properties table rule.
     *
     * <p>Should not use for spring namespace.</p>
     *
     * @deprecated should be private
     * @param logicTable logic table name
     * @param dynamic is dynamic table
     * @param actualTables names of actual tables
     * @param dataSourceRule data source rule
     * @param dataSourceNames names of data sources
     * @param databaseShardingStrategy database sharding strategy
     * @param tableShardingStrategy table sharding strategy
     * @param generateKeyColumn generate key column name
     * @param keyGenerator key generator
     */
    @Deprecated
    public TableRule(final String logicTable, final boolean dynamic, final List<String> actualTables, final DataSourceRule dataSourceRule, final Collection<String> dataSourceNames,
                     final DatabaseShardingStrategy databaseShardingStrategy, final TableShardingStrategy tableShardingStrategy,
                     final String generateKeyColumn, final KeyGenerator keyGenerator) {
        Preconditions.checkNotNull(logicTable);
        this.logicTable = logicTable;
        this.dynamic = dynamic;
        this.databaseShardingStrategy = databaseShardingStrategy;
        this.tableShardingStrategy = tableShardingStrategy;
        if (dynamic) {
            Preconditions.checkNotNull(dataSourceRule);
            this.actualTables = generateDataNodes(dataSourceRule);
        } else if (null == actualTables || actualTables.isEmpty()) {
            Preconditions.checkNotNull(dataSourceRule);
            this.actualTables = generateDataNodes(Collections.singletonList(logicTable), dataSourceRule, dataSourceNames);
        } else {
            this.actualTables = generateDataNodes(actualTables, dataSourceRule, dataSourceNames);
        }
        this.generateKeyColumn = generateKeyColumn;
        this.keyGenerator = keyGenerator;
    }
    
    /**
     * Get table rule builder.
     *
     * @param logicTable logic table name
     * @return table rule builder
     */
    public static TableRuleBuilder builder(final String logicTable) {
        return new TableRuleBuilder(logicTable);
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
    
    /**
     * Table rule builder..
     */
    @RequiredArgsConstructor
    public static class TableRuleBuilder {
        
        private final String logicTable;
        
        private boolean dynamic;
        
        private List<String> actualTables;
        
        private DataSourceRule dataSourceRule;
        
        private Collection<String> dataSourceNames;
        
        private DatabaseShardingStrategy databaseShardingStrategy;
        
        private TableShardingStrategy tableShardingStrategy;
        
        private String generateKeyColumn;
        
        private Class<? extends KeyGenerator> keyGeneratorClass;
        
        /**
         * Build is dynamic table.
         *
         * @param dynamic is dynamic table
         * @return this builder
         */
        public TableRuleBuilder dynamic(final boolean dynamic) {
            this.dynamic = dynamic;
            return this;
        }
        
        /**
         * Build actual tables.
         *
         * @param actualTables actual tables
         * @return this builder
         */
        public TableRuleBuilder actualTables(final List<String> actualTables) {
            this.actualTables = actualTables;
            return this;
        }
        
        /**
         * Build data source rule.
         *
         * @param dataSourceRule data source rule
         * @return this builder
         */
        public TableRuleBuilder dataSourceRule(final DataSourceRule dataSourceRule) {
            this.dataSourceRule = dataSourceRule;
            return this;
        }
        
        /**
         * Build data sources's names.
         *
         * @param dataSourceNames data sources's names
         * @return this builder
         */
        public TableRuleBuilder dataSourceNames(final Collection<String> dataSourceNames) {
            this.dataSourceNames = dataSourceNames;
            return this;
        }
        
        /**
         * Build database sharding strategy.
         *
         * @param databaseShardingStrategy database sharding strategy
         * @return this builder
         */
        public TableRuleBuilder databaseShardingStrategy(final DatabaseShardingStrategy databaseShardingStrategy) {
            this.databaseShardingStrategy = databaseShardingStrategy;
            return this;
        }
        
        /**
         * Build table sharding strategy.
         *
         * @param tableShardingStrategy table sharding strategy
         * @return this builder
         */
        public TableRuleBuilder tableShardingStrategy(final TableShardingStrategy tableShardingStrategy) {
            this.tableShardingStrategy = tableShardingStrategy;
            return this;
        }
        
        /**
         * Build generate key column.
         * 
         * @param generateKeyColumn generate key column
         * @return this builder
         */
        public TableRuleBuilder generateKeyColumn(final String generateKeyColumn) {
            this.generateKeyColumn = generateKeyColumn;
            return this;
        }
        
        /**
         * Build generate key column.
         *
         * @param generateKeyColumn generate key column
         * @param keyGeneratorClass key generator class
         * @return this builder
         */
        public TableRuleBuilder generateKeyColumn(final String generateKeyColumn, final Class<? extends KeyGenerator> keyGeneratorClass) {
            this.generateKeyColumn = generateKeyColumn;
            this.keyGeneratorClass = keyGeneratorClass;
            return this;
        }
        
        /**
         * Build table rule.
         *
         * @return built table rule
         */
        public TableRule build() {
            KeyGenerator keyGenerator = null;
            if (null != generateKeyColumn && null != keyGeneratorClass) {
                keyGenerator = KeyGeneratorFactory.createKeyGenerator(keyGeneratorClass);
            }
            return new TableRule(logicTable, dynamic, actualTables, dataSourceRule, dataSourceNames, databaseShardingStrategy, tableShardingStrategy, generateKeyColumn, keyGenerator);
        }
    }
}
