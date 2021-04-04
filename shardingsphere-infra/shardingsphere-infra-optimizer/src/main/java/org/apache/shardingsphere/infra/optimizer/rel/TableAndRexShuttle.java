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

package org.apache.shardingsphere.infra.optimizer.rel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import lombok.Getter;
import org.apache.calcite.plan.RelOptUtil;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.RelShuttleImpl;
import org.apache.calcite.rel.core.TableScan;
import org.apache.calcite.rel.logical.LogicalFilter;
import org.apache.calcite.rel.logical.LogicalTableScan;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.rex.RexDynamicParam;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexLiteral;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.infra.optimizer.tools.RelNodeUtil;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class TableAndRexShuttle extends RelShuttleImpl {
    
    private ShardingRule shardingRule;
    
    @Getter
    private Map<String, List<ShardingConditionValue>> tableShardingConditions = Maps.newHashMap();
    
    public TableAndRexShuttle(final ShardingRule shardingRule) {
        this.shardingRule = shardingRule;
    }
    
    @Override
    public RelNode visit(final TableScan scan) {
        String tableName = RelNodeUtil.getTableName(scan);
        if (!tableShardingConditions.containsKey(tableName)) {
            tableShardingConditions.put(tableName, new ArrayList<>());
        }
        return super.visit(scan);
    }
    
    @Override
    public RelNode visit(final LogicalFilter filter) {
        if (filter.getInput() instanceof LogicalTableScan) {
            String tableName = RelNodeUtil.getTableName((TableScan) filter.getInput());
            List<RexNode> rexNodes = RelOptUtil.conjunctions(filter.getCondition());
            
            for (RexNode rexNode : rexNodes) {
                if (!(rexNode instanceof RexCall)) {
                    continue;
                }
                
                RexCall rexCall = (RexCall) rexNode;
                List<RexNode> operands = rexCall.getOperands();
                RexNode operand0 = operands.get(0);
                RexNode operand1 = operands.get(1);
                
                Comparable operandVal;
                String columnName;
    
                SqlKind sqlKind = rexCall.getKind();
                if (operand0 instanceof RexInputRef) {
                    operandVal = value(operand1);
                    columnName = getColumnName((RexInputRef) operand0, filter.getRowType());
                    // TODO if operand1 is RexInputRef instead of operand0  sqlKind.reverse();
                } else {
                    continue;
                }
                collectShardingFilter(sqlKind, tableName, columnName, operandVal);
            }
        }
        return super.visit(filter);
    }
    
    private Comparable value(final RexNode operand) {
        if (operand instanceof RexLiteral) {
            RexLiteral rexLiteral = (RexLiteral) operand;
            RelDataType type = rexLiteral.getType();
            SqlTypeName sqlTypeName = type.getSqlTypeName();
            switch (sqlTypeName.getJdbcOrdinal()) {
                case Types.BIT:
                case Types.INTEGER:
                case Types.SMALLINT:
                    return rexLiteral.getValueAs(Integer.class);
                case Types.BIGINT:
                    return rexLiteral.getValueAs(Long.class);
                    // TODO 
                default:
                    return rexLiteral.getValue4();
            }
        } else if (operand instanceof RexDynamicParam) {
            // TODO 
            throw new UnsupportedOperationException();
        }
        // TODO 
        return null;
    }
    
    private String getColumnName(final RexInputRef rexInputRef, final RelDataType relDataType) {
        RelDataTypeField relDataTypeField = relDataType.getFieldList().get(rexInputRef.getIndex());
        return relDataTypeField.getName();
    }
    
    private void collectShardingFilter(final SqlKind sqlKind, final String tableName, final String columnName, 
                                       final Comparable operandVal) {
        ShardingConditionValue shardingConditionValue = null;
        switch (sqlKind) {
            case IN:
                // TODO ListShardingConditionValue
            case EQUALS:
                shardingConditionValue = new ListShardingConditionValue<>(columnName, tableName, Arrays.asList(operandVal));
                break;
            case GREATER_THAN:
                shardingConditionValue = new RangeShardingConditionValue<>(columnName, tableName, Range.greaterThan(operandVal));
                break;
            case GREATER_THAN_OR_EQUAL:
                shardingConditionValue = new RangeShardingConditionValue<>(columnName, tableName, Range.atLeast(operandVal));
                break;
            case LESS_THAN:
                shardingConditionValue = new RangeShardingConditionValue<>(columnName, tableName, Range.lessThan(operandVal));
                break;
            case LESS_THAN_OR_EQUAL:
                shardingConditionValue = new RangeShardingConditionValue<>(columnName, tableName, Range.atMost(operandVal));
                break;
            case BETWEEN:
                // TODO 
                break;
            case NOT_EQUALS:
                // TODO 
                break;
            default:
                throw new UnsupportedOperationException();
        }
        if (shardingConditionValue == null) {
            return;
        }
        
        List<ShardingConditionValue> shardingConditionValues = tableShardingConditions.getOrDefault(tableName, Lists.newArrayList());
        shardingConditionValues.add(shardingConditionValue);
        tableShardingConditions.putIfAbsent(tableName, shardingConditionValues);
    }
    
    /**
     * Get table with condition.
     * @param relNode relnode
     * @param shardingRule shardingRule
     * @return table with condition mapping.
     */
    public static Map<String, List<ShardingConditionValue>> getTableAndShardingCondition(final RelNode relNode, final ShardingRule shardingRule) {
        TableAndRexShuttle shuttle = new TableAndRexShuttle(shardingRule);
        relNode.accept(shuttle);
        return shuttle.getTableShardingConditions();
    }
}
