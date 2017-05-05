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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.context;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.contstant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLNumberExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLPlaceholderExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLTextExpr;
import com.google.common.base.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 条件对象上下文.
 * 
 * @author zhangliang
 */
@ToString
public final class ConditionContext {
    
    private final Map<ShardingColumnContext, Condition> conditions = new LinkedHashMap<>();
    
    /**
     * 添加条件对象.
     * 
     * @param condition 条件对象
     */
    // TODO 添加condition时进行判断, 比如:如果以存在 等于操作 的condition, 而已存在包含 =符号 的相同column的condition, 则不添加现有的condition, 而且删除原有condition
    public void add(final Condition condition) {
        // TODO 自关联有问题，表名可考虑使用别名对应
        conditions.put(condition.getShardingColumnContext(), condition);
    }
    
    /**
     * 查找条件对象.
     * 
     * @param table 表名称
     * @param column 列名称
     * @return 条件对象
     */
    public Optional<Condition> find(final String table, final String column) {
        return Optional.fromNullable(conditions.get(new ShardingColumnContext(column, table)));
    }
    
    /**
     * 解析参数中间的新数据.
     * 
     * @param parameters 参数列表
     */
    public void setNewConditionValue(final List<Object> parameters) {
        for (Condition each : conditions.values()) {
            if (each.getValueIndices().isEmpty()) {
                continue;
            }
            for (int i = 0; i < each.getValueIndices().size(); i++) {
                Object value = parameters.get(each.getValueIndices().get(i));
                if (value instanceof Comparable<?>) {
                    each.getValues().set(i, (Comparable<?>) value);
                } else {
                    each.getValues().set(i, "");
                }
            }
        }
    }
    
    /**
     * 条件对象.
     * 
     * @author gaohongtao
     */
    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static final class Condition {
        
        private final ShardingColumnContext shardingColumnContext;
        
        private final ShardingOperator operator;
        
        private final List<Comparable<?>> values = new ArrayList<>();
        
        private final List<Integer> valueIndices = new ArrayList<>();
        
        public Condition(final ShardingColumnContext shardingColumnContext, final SQLExpr sqlExpr) {
            this(shardingColumnContext, ShardingOperator.EQUAL);
            initSQLExpr(sqlExpr);
        }
        
        public Condition(final ShardingColumnContext shardingColumnContext, final SQLExpr beginSqlExpr, final SQLExpr endSqlExpr) {
            this(shardingColumnContext, ShardingOperator.BETWEEN);
            initSQLExpr(beginSqlExpr);
            initSQLExpr(endSqlExpr);
        }
        
        public Condition(final ShardingColumnContext shardingColumnContext, final List<SQLExpr> sqlExprs) {
            this(shardingColumnContext, ShardingOperator.IN);
            for (SQLExpr each : sqlExprs) {
                initSQLExpr(each);
            }
        }
        
        private void initSQLExpr(final SQLExpr sqlExpr) {
            if (sqlExpr instanceof SQLPlaceholderExpr) {
                values.add((Comparable) ((SQLPlaceholderExpr) sqlExpr).getValue());
                valueIndices.add(((SQLPlaceholderExpr) sqlExpr).getIndex());
            } else if (sqlExpr instanceof SQLTextExpr) {
                values.add(((SQLTextExpr) sqlExpr).getText());
            } else if (sqlExpr instanceof SQLNumberExpr) {
                values.add((Comparable) ((SQLNumberExpr) sqlExpr).getNumber());
            }
        }
    }
}
