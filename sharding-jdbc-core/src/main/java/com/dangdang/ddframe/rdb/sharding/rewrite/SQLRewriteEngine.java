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

package com.dangdang.ddframe.rdb.sharding.rewrite;


import com.dangdang.ddframe.rdb.sharding.api.rule.BindingTableRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OffsetLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.SQLToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.routing.type.TableUnit;
import com.dangdang.ddframe.rdb.sharding.routing.type.complex.CartesianTableReference;
import com.google.common.base.Optional;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL重写引擎.
 *
 * @author zhangliang
 */
public final class SQLRewriteEngine {
    
    private final String originalSQL;
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    private final Collection<String> tableNames;
    
    private final Limit limit;
    
    public SQLRewriteEngine(final String originalSQL, final SQLStatement sqlStatement) {
        this.originalSQL = originalSQL;
        sqlTokens.addAll(sqlStatement.getSqlTokens());
        tableNames = sqlStatement.getTables().getTableNames();
        limit = sqlStatement.getLimit();
    }
    
    /**
     * SQL改写.
     *
     * @param isRewriteLimit 是否重写Limit
     * @return SQL构建器
     */
    public SQLBuilder rewrite(final boolean isRewriteLimit) {
        SQLBuilder result = new SQLBuilder();
        if (sqlTokens.isEmpty()) {
            result.append(originalSQL);
            return result;
        }
        int count = 0;
        sortByBeginPosition();
        for (SQLToken each : sqlTokens) {
            if (0 == count) {
                result.append(originalSQL.substring(0, each.getBeginPosition()));
            }
            if (each instanceof TableToken) {
                appendTableToken(result, (TableToken) each, count, sqlTokens);
            } else if (each instanceof ItemsToken) {
                appendItemsToken(result, (ItemsToken) each, count, sqlTokens);
            } else if (each instanceof RowCountLimitToken) {
                appendLimitRowCount(result, (RowCountLimitToken) each, count, sqlTokens, isRewriteLimit);
            } else if (each instanceof OffsetLimitToken) {
                appendLimitOffsetToken(result, (OffsetLimitToken) each, count, sqlTokens, isRewriteLimit);
            }
            count++;
        }
        return result;
    }
    
    private void sortByBeginPosition() {
        Collections.sort(sqlTokens, new Comparator<SQLToken>() {
            
            @Override
            public int compare(final SQLToken o1, final SQLToken o2) {
                return o1.getBeginPosition() - o2.getBeginPosition();
            }
        });
    }
    
    private void appendTableToken(final SQLBuilder sqlBuilder, final TableToken tableToken, final int count, final List<SQLToken> sqlTokens) {
        String tableName = tableNames.contains(tableToken.getTableName()) ? tableToken.getTableName() : tableToken.getOriginalLiterals();
        sqlBuilder.append(new SQLBuilderToken(tableName, tableName));
        int beginPosition = tableToken.getBeginPosition() + tableToken.getOriginalLiterals().length();
        int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
        sqlBuilder.append(originalSQL.substring(beginPosition, endPosition));
    }
    
    private void appendItemsToken(final SQLBuilder sqlBuilder, final ItemsToken itemsToken, final int count, final List<SQLToken> sqlTokens) {
        for (String item : itemsToken.getItems()) {
            sqlBuilder.append(", ");
            sqlBuilder.append(item);
        }
        int beginPosition = itemsToken.getBeginPosition();
        int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
        sqlBuilder.append(originalSQL.substring(beginPosition, endPosition));
    }
    
    private void appendLimitRowCount(final SQLBuilder sqlBuilder, final RowCountLimitToken rowCountLimitToken, final int count, final List<SQLToken> sqlTokens, final boolean isRewrite) {
        sqlBuilder.append(isRewrite ? String.valueOf(rowCountLimitToken.getRowCount() + limit.getOffset()) : String.valueOf(rowCountLimitToken.getRowCount()));
        int beginPosition = rowCountLimitToken.getBeginPosition() + String.valueOf(rowCountLimitToken.getRowCount()).length();
        int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
        sqlBuilder.append(originalSQL.substring(beginPosition, endPosition));
    }
    
    private void appendLimitOffsetToken(final SQLBuilder sqlBuilder, final OffsetLimitToken offsetLimitToken, final int count, final List<SQLToken> sqlTokens, final boolean isRewrite) {
        sqlBuilder.append(isRewrite ? "0" : String.valueOf(offsetLimitToken.getOffset()));
        int beginPosition = offsetLimitToken.getBeginPosition() + String.valueOf(offsetLimitToken.getOffset()).length();
        int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
        sqlBuilder.append(originalSQL.substring(beginPosition, endPosition));
    }
    
    /**
     * 改写SQL表.
     * 
     * @param tableUnit 路由表单元
     * @param sqlBuilder SQL构建器
     * @param shardingRule 分库分表规则配置对象
     * @return SQL构建器
     */
    public SQLBuilder rewriteTable(final TableUnit tableUnit, final SQLBuilder sqlBuilder, final ShardingRule shardingRule) {
        Collection<SQLBuilderToken> tokens = new LinkedList<>();
        tokens.add(new SQLBuilderToken(tableUnit.getLogicTableName(), tableUnit.getActualTableName()));
        Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(tableUnit.getLogicTableName());
        if (bindingTableRule.isPresent()) {
            tokens.addAll(getBindingTableTokens(tableUnit, bindingTableRule.get()));
        }
        return sqlBuilder.createNewSQLBuilder(tokens);
    }
    
    /**
     * 改写SQL表.
     * 
     * @param cartesianTableReference 笛卡尔积路由表单元
     * @param sqlBuilder SQL构建器
     * @param shardingRule 分库分表规则配置对象
     * @return SQL构建器
     */
    public SQLBuilder rewriteTable(final CartesianTableReference cartesianTableReference, final SQLBuilder sqlBuilder, final ShardingRule shardingRule) {
        Collection<SQLBuilderToken> tokens = new LinkedList<>();
        for (TableUnit each : cartesianTableReference.getTableUnits()) {
            tokens.add(new SQLBuilderToken(each.getLogicTableName(), each.getActualTableName()));
            Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(each.getLogicTableName());
            if (bindingTableRule.isPresent()) {
                tokens.addAll(getBindingTableTokens(each, bindingTableRule.get()));
            }
        }
        return sqlBuilder.createNewSQLBuilder(tokens);
    }
    
    private Collection<SQLBuilderToken> getBindingTableTokens(final TableUnit tableUnit, final BindingTableRule bindingTableRule) {
        Collection<SQLBuilderToken> result = new LinkedList<>();
        for (String eachTable : tableNames) {
            if (!eachTable.equalsIgnoreCase(tableUnit.getLogicTableName()) && bindingTableRule.hasLogicTable(eachTable)) {
                result.add(new SQLBuilderToken(eachTable, bindingTableRule.getBindingActualTable(tableUnit.getDataSourceName(), eachTable, tableUnit.getActualTableName())));
            }
        }
        return result;
    }
}
