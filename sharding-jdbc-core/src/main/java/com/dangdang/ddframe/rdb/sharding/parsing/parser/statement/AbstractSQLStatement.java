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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement;

import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.GroupBy;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OrderBy;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ShardingColumn;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLIdentifierExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPropertyExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.SQLToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL语句对象抽象类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractSQLStatement implements SQLStatement {
    
    private final SQLType type;
    
    private final List<Table> tables = new ArrayList<>();
    
    private final Map<ShardingColumn, Condition> conditions = new LinkedHashMap<>();
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    @Override
    public final SQLType getType() {
        return type;
    }
    
    @Override
    // TODO 添加condition时进行判断, 比如:如果以存在 等于操作 的condition, 而已存在包含 =符号 的相同column的condition, 则不添加现有的condition, 而且删除原有condition
    public void add(final Condition condition) {
        // TODO 自关联有问题，表名可考虑使用别名对应
        conditions.put(condition.getShardingColumn(), condition);
    }
    
    @Override
    public Optional<Condition> find(final String table, final String column) {
        return Optional.fromNullable(conditions.get(new ShardingColumn(column, table)));
    }
    
    @Override
    public Optional<ShardingColumn> findColumn(final SQLExpression sqlExpression) {
        if (sqlExpression instanceof SQLPropertyExpression) {
            return Optional.fromNullable(getColumnWithQualifiedName((SQLPropertyExpression) sqlExpression));
        }
        if (sqlExpression instanceof SQLIdentifierExpression) {
            return Optional.fromNullable(getColumnWithoutAlias((SQLIdentifierExpression) sqlExpression));
        }
        return Optional.absent();
    }
    
    private ShardingColumn getColumnWithQualifiedName(final SQLPropertyExpression expr) {
        Optional<Table> table = findTable((expr.getOwner()).getName());
        return expr.getOwner() instanceof SQLIdentifierExpression && table.isPresent() ? createColumn(expr.getName(), table.get().getName()) : null;
    }
    
    private Optional<Table> findTable(final String tableNameOrAlias) {
        Optional<Table> tableFromName = findTableFromName(tableNameOrAlias);
        return tableFromName.isPresent() ? tableFromName : findTableFromAlias(tableNameOrAlias);
    }
    
    private Optional<Table> findTableFromName(final String name) {
        for (Table each : tables) {
            if (each.getName().equalsIgnoreCase(SQLUtil.getExactlyValue(name))) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private Optional<Table> findTableFromAlias(final String alias) {
        for (Table each : tables) {
            if (each.getAlias().isPresent() && each.getAlias().get().equalsIgnoreCase(SQLUtil.getExactlyValue(alias))) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private ShardingColumn getColumnWithoutAlias(final SQLIdentifierExpression expr) {
        return 1 == tables.size() ? createColumn(expr.getName(), tables.iterator().next().getName()) : null;
    }
    
    private ShardingColumn createColumn(final String columnName, final String tableName) {
        return new ShardingColumn(SQLUtil.getExactlyValue(columnName), SQLUtil.getExactlyValue(tableName));
    }
    
    public List<OrderBy> getOrderByList() {
        return Collections.emptyList();
    }
    
    public List<GroupBy> getGroupByList() {
        return Collections.emptyList();
    }
    
    @Override
    public List<AggregationSelectItem> getAggregationSelectItems() {
        return Collections.emptyList();
    }
    
    public Limit getLimit() {
        return null;
    }
    
    public void setLimit(final Limit limit) {
    }
}
