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

package io.shardingsphere.core.rewrite;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.optimizer.condition.ShardingCondition;
import io.shardingsphere.core.optimizer.insert.InsertShardingCondition;
import io.shardingsphere.core.rewrite.placeholder.IndexPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.InsertValuesPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.SchemaPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.ShardingPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.TablePlaceholder;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.core.routing.type.TableUnit;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL builder.
 * 
 * @author gaohongtao
 * @author zhangliang
 * @author maxiaoguang
 */
public final class SQLBuilder {
    
    private final List<Object> segments;
    
    private final List<Object> parameters;
    
    private StringBuilder currentSegment;
    
    public SQLBuilder() {
        this(Collections.emptyList());
    }
    
    public SQLBuilder(final List<Object> parameters) {
        segments = new LinkedList<>();
        this.parameters = parameters;
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
     * Append sharding placeholder.
     *
     * @param shardingPlaceholder sharding placeholder
     */
    public void appendPlaceholder(final ShardingPlaceholder shardingPlaceholder) {
        segments.add(shardingPlaceholder);
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Convert to SQL unit.
     *
     * @param tableUnit table unit
     * @param logicAndActualTableMap logic and actual map
     * @param shardingRule sharding rule
     * @return SQL unit
     */
    public SQLUnit toSQL(final TableUnit tableUnit, final Map<String, String> logicAndActualTableMap, final ShardingRule shardingRule) {
        List<Object> insertParameters = new LinkedList<>();
        StringBuilder result = new StringBuilder();
        for (Object each : segments) {
            if (!(each instanceof ShardingPlaceholder)) {
                result.append(each);
                continue;
            }
            String logicTableName = ((ShardingPlaceholder) each).getLogicTableName();
            String actualTableName = logicAndActualTableMap.get(logicTableName);
            if (each instanceof TablePlaceholder) {
                result.append(null == actualTableName ? logicTableName : actualTableName);
            } else if (each instanceof SchemaPlaceholder) {
                SchemaPlaceholder schemaPlaceholder = (SchemaPlaceholder) each;
                Optional<TableRule> tableRule = shardingRule.tryFindTableRuleByActualTable(actualTableName);
                if (!tableRule.isPresent() && Strings.isNullOrEmpty(shardingRule.getShardingDataSourceNames().getDefaultDataSourceName())) {
                    throw new ShardingException("Cannot found schema name '%s' in sharding rule.", schemaPlaceholder.getLogicSchemaName());
                }
                // TODO 目前只能找到真实数据源名称. 未来需要在初始化sharding rule时创建connection,并验证连接是否正确,并获取出真实的schema的名字, 然后在这里替换actualDataSourceName为actualSchemaName
                // TODO 目前actualDataSourceName必须actualSchemaName一样,才能保证替换schema的场景不出错, 如: show columns xxx
                Preconditions.checkState(tableRule.isPresent());
                result.append(tableRule.get().getActualDatasourceNames().iterator().next());
            } else if (each instanceof IndexPlaceholder) {
                IndexPlaceholder indexPlaceholder = (IndexPlaceholder) each;
                result.append(indexPlaceholder.getLogicIndexName());
                if (!Strings.isNullOrEmpty(actualTableName)) {
                    result.append("_");
                    result.append(actualTableName);
                }
            } else if (each instanceof InsertValuesPlaceholder) {
                InsertValuesPlaceholder insertValuesPlaceholder = (InsertValuesPlaceholder) each;
                List<String> expressions = new LinkedList<>();
                for (ShardingCondition shardingCondition : insertValuesPlaceholder.getShardingConditions().getShardingConditions()) {
                    processInsertShardingCondition(tableUnit, (InsertShardingCondition) shardingCondition, expressions, insertParameters);
                }
                int count = 0;
                for (String s : expressions) {
                    if (0 != count) {
                        result.append(", ");
                    }
                    result.append(s);
                    count++;
                }
            } else {
                result.append(each);
            }
        }
        if (insertParameters.isEmpty()) {
            return new SQLUnit(result.toString(), new ArrayList<>(Collections.singleton(parameters)));
        } else {
            return new SQLUnit(result.toString(), new ArrayList<>(Collections.singleton(insertParameters)));
        }
    }
    
    private void processInsertShardingCondition(final TableUnit tableUnit, final InsertShardingCondition shardingCondition, final List<String> expressions, final List<Object> parameters) {
        for (DataNode dataNode : shardingCondition.getDataNodes()) {
            if (dataNode.getDataSourceName().equals(tableUnit.getDataSourceName()) && dataNode.getTableName().equals(tableUnit.getRoutingTables().iterator().next().getActualTableName())) {
                expressions.add(shardingCondition.getInsertValueExpression());
                parameters.addAll(shardingCondition.getParameters());
                break;
            }
        }
    }
}
