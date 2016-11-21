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
import com.dangdang.ddframe.rdb.sharding.id.generator.IdGenerator;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * 表规则配置对象.
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
    
    @Getter(AccessLevel.PACKAGE)
    private final Map<String, IdGenerator> autoIncrementColumnMap = new LinkedHashMap<>();
    
    /**
     * 全属性构造器.
     *
     * <p>用于Spring非命名空间的配置.</p>
     *
     * <p>未来将改为private权限, 不在对外公开, 不建议使用非Spring命名空间的配置.</p>
     *
     * @deprecated 未来将改为private权限, 不在对外公开, 不建议使用非Spring命名空间的配置.
     */
    @Deprecated
    public TableRule(final String logicTable, final boolean dynamic, final List<String> actualTables, final DataSourceRule dataSourceRule, final Collection<String> dataSourceNames,
                     final DatabaseShardingStrategy databaseShardingStrategy, final TableShardingStrategy tableShardingStrategy) {
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
    }
    
    /**
     * 获取表规则配置对象构建器.
     *
     * @param logicTable 逻辑表名称 
     * @return 表规则配置对象构建器
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
     * 根据数据源名称过滤获取真实数据单元.
     *
     * @param targetDataSources 数据源名称集合
     * @param targetTables 真实表名称集合
     * @return 真实数据单元
     */
    public Collection<DataNode> getActualDataNodes(final Collection<String> targetDataSources, final Collection<String> targetTables) {
        return dynamic ? getDynamicDataNodes(targetDataSources, targetTables) : getStaticDataNodes(targetDataSources, targetTables);
    }
    
    private Collection<DataNode> getDynamicDataNodes(final Collection<String> targetDataSources, final Collection<String> targetTables) {
        Collection<DataNode> result = new LinkedHashSet<>(targetDataSources.size() * targetTables.size());
        for (String targetDataSource : targetDataSources) {
            for (String targetTable : targetTables) {
                result.add(new DataNode(targetDataSource, targetTable));
            }
        }
        return result;
    }
    
    private Collection<DataNode> getStaticDataNodes(final Collection<String> targetDataSources, final Collection<String> targetTables) {
        Collection<DataNode> result = new LinkedHashSet<>(actualTables.size());
        for (DataNode each : actualTables) {
            if (targetDataSources.contains(each.getDataSourceName()) && targetTables.contains(each.getTableName())) {
                result.add(each);
            }
        }
        return result;
    }
    
    
    /**
     * 获取真实数据源.
     *
     * @return 真实表名称
     */
    public Collection<String> getActualDatasourceNames() {
        Collection<String> result = new LinkedHashSet<>(actualTables.size());
        for (DataNode each : actualTables) {
            result.add(each.getDataSourceName());
        }
        return result;
    }
    
    /**
     * 根据数据源名称过滤获取真实表名称.
     *
     * @param targetDataSources 数据源名称
     * @return 真实表名称
     */
    public Collection<String> getActualTableNames(final Collection<String> targetDataSources) {
        Collection<String> result = new LinkedHashSet<>(actualTables.size());
        for (DataNode each : actualTables) {
            if (targetDataSources.contains(each.getDataSourceName())) {
                result.add(each.getTableName());
            }
        }
        return result;
    }
    
    int findActualTableIndex(final String dataSourceName, final String actualTableName) {
        int result = 0;
        for (DataNode each : actualTables) {
            if (each.getDataSourceName().equals(dataSourceName) && each.getTableName().equals(actualTableName)) {
                return result;
            }
            result++;
        }
        return -1;
    }
    
    void fillIdGenerator(final Class<? extends IdGenerator> idGeneratorClass) {
        for (Map.Entry<String, IdGenerator> each : autoIncrementColumnMap.entrySet()) {
            if (null == each.getValue()) {
                IdGenerator idGenerator = TableRuleBuilder.instanceIdGenerator(idGeneratorClass);
                each.setValue(idGenerator);
            }
        }
    }
    
    /**
     * 生成Id.
     * 
     * @param columnName 列名称
     * @return 生成的id
     */
    public Object generateId(final String columnName) {
        Number result = autoIncrementColumnMap.get(columnName).generateId();
        Preconditions.checkNotNull(result);
        return result;
    }
    
    /**
     * 表规则配置对象构建器.
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
    
        private final Map<String, IdGenerator> autoIncrementColumnMap = new LinkedHashMap<>();
    
        private Class<? extends IdGenerator> tableIdGeneratorClass;
        
        
        static IdGenerator instanceIdGenerator(final Class<? extends IdGenerator> idGeneratorClass) {
            Preconditions.checkNotNull(idGeneratorClass);
            try {
                return idGeneratorClass.newInstance();
            } catch (final InstantiationException | IllegalAccessException e) {
                throw new IllegalArgumentException(String.format("Class %s should have public privilege and no argument constructor", idGeneratorClass.getName()));
            }
        }
    
        /**
         * 构建是否为动态表.
         *
         * @param dynamic 是否为动态表
         * @return 真实表集合
         */
        public TableRuleBuilder dynamic(final boolean dynamic) {
            this.dynamic = dynamic;
            return this;
        }
        
        /**
         * 构建真实表集合.
         *
         * @param actualTables 真实表集合
         * @return 真实表集合
         */
        public TableRuleBuilder actualTables(final List<String> actualTables) {
            this.actualTables = actualTables;
            return this;
        }
        
        /**
         * 构建数据源分片规则.
         *
         * @param dataSourceRule 数据源分片规则
         * @return 规则配置对象构建器
         */
        public TableRuleBuilder dataSourceRule(final DataSourceRule dataSourceRule) {
            this.dataSourceRule = dataSourceRule;
            return this;
        }
        
        /**
         * 构建数据源分片规则.
         *
         * @param dataSourceNames 数据源名称集合
         * @return 规则配置对象构建器
         */
        public TableRuleBuilder dataSourceNames(final Collection<String> dataSourceNames) {
            this.dataSourceNames = dataSourceNames;
            return this;
        }
        
        /**
         * 构建数据库分片策略.
         *
         * @param databaseShardingStrategy 数据库分片策略
         * @return 规则配置对象构建器
         */
        public TableRuleBuilder databaseShardingStrategy(final DatabaseShardingStrategy databaseShardingStrategy) {
            this.databaseShardingStrategy = databaseShardingStrategy;
            return this;
        }
        
        /**
         * 构建表分片策略.
         *
         * @param tableShardingStrategy 表分片策略
         * @return 规则配置对象构建器
         */
        public TableRuleBuilder tableShardingStrategy(final TableShardingStrategy tableShardingStrategy) {
            this.tableShardingStrategy = tableShardingStrategy;
            return this;
        }
    
        /**
         * 自增列.
         * 
         * @param autoIncrementColumn 自增列名称
         * @return 规则配置对象构建器
         */
        public TableRuleBuilder autoIncrementColumns(final String autoIncrementColumn) {
            this.autoIncrementColumnMap.put(autoIncrementColumn, null);
            return this;
        }
    
        /**
         * 自增列.
         *
         * @param autoIncrementColumn 自增列名称
         * @param columnIdGeneratorClass 列Id生成器的类
         * @return 规则配置对象构建器
         */
        public TableRuleBuilder autoIncrementColumns(final String autoIncrementColumn, final Class<? extends IdGenerator> columnIdGeneratorClass) {
            this.autoIncrementColumnMap.put(autoIncrementColumn, instanceIdGenerator(columnIdGeneratorClass));
            return this;
        }
    
        /**
         * 整个表的Id生成器.
         * 
         * @param tableIdGeneratorClass Id生成器
         * @return 规则配置对象构建器
         */
        public TableRuleBuilder tableIdGenerator(final Class<? extends IdGenerator> tableIdGeneratorClass) {
            this.tableIdGeneratorClass = tableIdGeneratorClass;
            return this;
        }
        
        /**
         * 构建表规则配置对象.
         *
         * @return 表规则配置对象
         */
        public TableRule build() {
            TableRule result = new TableRule(logicTable, dynamic, actualTables, dataSourceRule, dataSourceNames, databaseShardingStrategy, tableShardingStrategy);
            result.autoIncrementColumnMap.putAll(autoIncrementColumnMap);
            if (null == tableIdGeneratorClass) {
                return result;
            }
            result.fillIdGenerator(tableIdGeneratorClass);
            return result;
        }
        
    }
}
