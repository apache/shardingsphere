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

import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLIdentifierExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expr.SQLPropertyExpr;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.SQLToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL上下文抽象类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@Setter
public abstract class AbstractSQLContext implements SQLContext {
    
    private final SQLType type;
    
    private final List<Table> tables = new ArrayList<>();
    
    private ConditionContext conditionContext = new ConditionContext();
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    @Override
    public final SQLType getType() {
        return type;
    }
    
    @Override
    public Optional<ShardingColumn> findColumn(final SQLExpr expr) {
        if (expr instanceof SQLPropertyExpr) {
            return Optional.fromNullable(getColumnWithQualifiedName((SQLPropertyExpr) expr));
        }
        if (expr instanceof SQLIdentifierExpr) {
            return Optional.fromNullable(getColumnWithoutAlias((SQLIdentifierExpr) expr));
        }
        return Optional.absent();
    }
    
    private ShardingColumn getColumnWithQualifiedName(final SQLPropertyExpr expr) {
        Optional<Table> table = findTable((expr.getOwner()).getName());
        return expr.getOwner() instanceof SQLIdentifierExpr && table.isPresent() ? createColumn(expr.getName(), table.get().getName()) : null;
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
    
    private ShardingColumn getColumnWithoutAlias(final SQLIdentifierExpr expr) {
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
