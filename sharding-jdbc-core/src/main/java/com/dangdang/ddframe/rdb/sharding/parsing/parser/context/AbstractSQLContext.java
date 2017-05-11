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
    
    private final List<TableContext> tables = new ArrayList<>();
    
    private ConditionContext conditionContext = new ConditionContext();
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    @Override
    public final SQLType getType() {
        return type;
    }
    
    @Override
    public Optional<ShardingColumnContext> findColumn(final SQLExpr expr) {
        if (expr instanceof SQLPropertyExpr) {
            return Optional.fromNullable(getColumnWithQualifiedName((SQLPropertyExpr) expr));
        }
        if (expr instanceof SQLIdentifierExpr) {
            return Optional.fromNullable(getColumnWithoutAlias((SQLIdentifierExpr) expr));
        }
        return Optional.absent();
    }
    
    private ShardingColumnContext getColumnWithQualifiedName(final SQLPropertyExpr expr) {
        Optional<TableContext> table = findTable((expr.getOwner()).getName());
        return expr.getOwner() instanceof SQLIdentifierExpr && table.isPresent() ? createColumn(expr.getName(), table.get().getName()) : null;
    }
    
    private Optional<TableContext> findTable(final String tableNameOrAlias) {
        Optional<TableContext> tableFromName = findTableFromName(tableNameOrAlias);
        return tableFromName.isPresent() ? tableFromName : findTableFromAlias(tableNameOrAlias);
    }
    
    private Optional<TableContext> findTableFromName(final String name) {
        for (TableContext each : tables) {
            if (each.getName().equalsIgnoreCase(SQLUtil.getExactlyValue(name))) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private Optional<TableContext> findTableFromAlias(final String alias) {
        for (TableContext each : tables) {
            if (each.getAlias().isPresent() && each.getAlias().get().equalsIgnoreCase(SQLUtil.getExactlyValue(alias))) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private ShardingColumnContext getColumnWithoutAlias(final SQLIdentifierExpr expr) {
        return 1 == tables.size() ? createColumn(expr.getName(), tables.iterator().next().getName()) : null;
    }
    
    private ShardingColumnContext createColumn(final String columnName, final String tableName) {
        return new ShardingColumnContext(SQLUtil.getExactlyValue(columnName), SQLUtil.getExactlyValue(tableName));
    }
    
    @Override
    public List<OrderByContext> getOrderByContexts() {
        return Collections.emptyList();
    }
    
    @Override
    public List<GroupByContext> getGroupByContexts() {
        return Collections.emptyList();
    }
    
    @Override
    public List<AggregationSelectItemContext> getAggregationSelectItemContexts() {
        return Collections.emptyList();
    }
    
    @Override
    public LimitContext getLimitContext() {
        return null;
    }
    
    @Override
    public void setLimitContext(final LimitContext limitContext) {
    }
}
