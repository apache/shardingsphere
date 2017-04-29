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

package com.dangdang.ddframe.rdb.sharding.parser.sql.context;

import com.dangdang.ddframe.rdb.sharding.parser.contstant.SQLType;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLIdentifierExpr;
import com.dangdang.ddframe.rdb.sharding.parser.sql.expr.SQLPropertyExpr;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL上下文抽象类.
 *
 * @author zhangliang
 */
@Getter
public abstract class AbstractSQLContext implements SQLContext {
    
    @Setter
    private ConditionContext conditionContext = new ConditionContext();
    
    private final String originalSQL;
    
    private final SQLType type;
    
    private final List<TableContext> tables = new ArrayList<>();
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    public AbstractSQLContext(final String originalSQL, final SQLType type) {
        this.originalSQL = originalSQL;
        this.type = type;
    }
    
    @Override
    public final SQLType getType() {
        return type;
    }
    
    @Override
    public Optional<Condition.Column> findColumn(final SQLExpr expr) {
        if (expr instanceof SQLPropertyExpr) {
            return Optional.fromNullable(getColumnWithQualifiedName((SQLPropertyExpr) expr));
        }
        if (expr instanceof SQLIdentifierExpr) {
            return Optional.fromNullable(getColumnWithoutAlias((SQLIdentifierExpr) expr));
        }
        return Optional.absent();
    }
    
    private Condition.Column getColumnWithQualifiedName(final SQLPropertyExpr expr) {
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
    
    private Condition.Column getColumnWithoutAlias(final SQLIdentifierExpr expr) {
        return 1 == tables.size() ? createColumn(expr.getName(), tables.iterator().next().getName()) : null;
    }
    
    private Condition.Column createColumn(final String columnName, final String tableName) {
        return new Condition.Column(SQLUtil.getExactlyValue(columnName), SQLUtil.getExactlyValue(tableName));
    }
    
    @Override
    public SQLBuilder toSqlBuilder() {
        SQLBuilder result = new SQLBuilder();
        if (sqlTokens.isEmpty()) {
            append(result, originalSQL);
            return result;
        }
        int count = 0;
        Collections.sort(sqlTokens, new Comparator<SQLToken>() {
            
            @Override
            public int compare(final SQLToken o1, final SQLToken o2) {
                return o1.getBeginPosition() - o2.getBeginPosition();
            }
        });
        for (SQLToken each : sqlTokens) {
            if (0 == count) {
                append(result, originalSQL.substring(0, each.getBeginPosition()));
            }
            if (each instanceof TableToken) {
                String tableName = ((TableToken) each).getTableName();
                boolean found = false;
                for (TableContext tableContext : tables) {
                    if (tableContext.getName().equalsIgnoreCase(tableName)) {
                        found = true;
                        break;
                    }
                } 
                if (found) {
                    result.appendToken(tableName);
                    int beginPosition = each.getBeginPosition() + ((TableToken) each).getOriginalLiterals().length();
                    int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                    append(result, originalSQL.substring(beginPosition, endPosition));
                } else {
                    append(result, ((TableToken) each).getOriginalLiterals());
                    int beginPosition = each.getBeginPosition() + ((TableToken) each).getOriginalLiterals().length();
                    int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                    append(result, originalSQL.substring(beginPosition, endPosition));
                }
            } else if (each instanceof ItemsToken) {
                for (String item : ((ItemsToken) each).getItems()) {
                    append(result, ", ");
                    append(result, item);
                }
                int beginPosition = each.getBeginPosition();
                int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                append(result, originalSQL.substring(beginPosition, endPosition));
            } else if (each instanceof RowCountLimitToken) {
                result.appendToken(RowCountLimitToken.COUNT_NAME, ((RowCountLimitToken) each).getRowCount() + "");
                int beginPosition = each.getBeginPosition() + (((RowCountLimitToken) each).getRowCount() + "").length();
                int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                append(result, originalSQL.substring(beginPosition, endPosition));
            } else if (each instanceof OffsetLimitToken) {
                result.appendToken(OffsetLimitToken.OFFSET_NAME, ((OffsetLimitToken) each).getOffset() + "");
                int beginPosition = each.getBeginPosition() + (((OffsetLimitToken) each).getOffset() + "").length();
                int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                append(result, originalSQL.substring(beginPosition, endPosition));
            }
            count++;
        }
        return result;
    }
    
    private void append(final SQLBuilder sqlBuilder, final String literals) {
        try {
            sqlBuilder.append(literals);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
