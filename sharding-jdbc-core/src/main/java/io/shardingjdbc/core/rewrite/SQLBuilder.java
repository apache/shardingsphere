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

package io.shardingjdbc.core.rewrite;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL builder.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
public final class SQLBuilder {
    
    private final List<Object> segments;
    
    private StringBuilder currentSegment;
    
    public SQLBuilder() {
        segments = new LinkedList<>();
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Append literals.
     *
     * @param literals literals for SQL
     */
    public void appendLiterals(final String literals) {
        currentSegment.append(literals);
    }
    
    /**
     * Append table token.
     *
     * @param tableName table name
     */
    public void appendTable(final String tableName) {
        segments.add(new TableToken(tableName));
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Append schema token.
     *
     * @param schemaName schema name
     * @param tableName table name
     */
    public void appendSchema(final String schemaName, final String tableName) {
        segments.add(new SchemaToken(schemaName, tableName));
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Append index token.
     *
     * @param indexName index name
     * @param tableName table name
     */
    public void appendIndex(final String indexName, final String tableName) {
        segments.add(new IndexToken(indexName, tableName));
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Convert to SQL string.
     *
     * @param logicAndActualTableMap logic and actual map
     * @param shardingRule sharding rule
     * @return SQL string
     */
    public String toSQL(final Map<String, String> logicAndActualTableMap, final ShardingRule shardingRule) {
        StringBuilder result = new StringBuilder();
        for (Object each : segments) {
            if (each instanceof TableToken && logicAndActualTableMap.containsKey(((TableToken) each).tableName)) {
                result.append(logicAndActualTableMap.get(((TableToken) each).tableName));
            } else if (each instanceof SchemaToken) {
                SchemaToken schemaToken = (SchemaToken) each;
                String actualTableName = logicAndActualTableMap.get(schemaToken.tableName);
                Optional<TableRule> tableRule = shardingRule.tryFindTableRuleByActualTable(actualTableName);
                if (!tableRule.isPresent() && Strings.isNullOrEmpty(shardingRule.getDefaultDataSourceName())) {
                    throw new ShardingJdbcException("Cannot found schema name '%s' in sharding rule.", schemaToken.schemaName);
                }
                String actualDataSourceName = tableRule.get().getActualDatasourceNames().iterator().next();
                // TODO 目前只能找到真实数据源名称. 未来需要在初始化sharding rule时创建connnection,并验证连接是否正确,并获取出真实的schema的名字, 然后在这里替换actualDataSourceName为actualSchemaName
                // TODO 目前actualDataSourceName必须actualSchemaName一样,才能保证替换schema的场景不出错, 如: show columns xxx
                result.append(actualDataSourceName);
            } else if (each instanceof IndexToken) {
                IndexToken indexToken = (IndexToken) each;
                result.append(indexToken.indexName);
                String actualTableName = logicAndActualTableMap.get(indexToken.tableName);
                if (!Strings.isNullOrEmpty(actualTableName)) {
                    result.append("_");
                    result.append(actualTableName);
                }
            } else {
                result.append(each);
            }
        }
        return result.toString();
    }
    
    @RequiredArgsConstructor
    private class TableToken {
        
        private final String tableName;
        
        @Override
        public String toString() {
            return tableName;
        }
    }
    
    @RequiredArgsConstructor
    private class SchemaToken {
        
        private final String schemaName;
    
        private final String tableName;
        
        @Override
        public String toString() {
            return schemaName;
        }
    }
    
    @RequiredArgsConstructor
    private class IndexToken {
        
        private final String indexName;
        
        private final String tableName;
        
        @Override
        public String toString() {
            return indexName;
        }
    }
}
