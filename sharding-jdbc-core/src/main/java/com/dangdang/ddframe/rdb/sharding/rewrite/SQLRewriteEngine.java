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


import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.limit.Limit;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.OffsetLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.RowCountLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.SQLToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;

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
     * @return SQL构建器
     */
    public SQLBuilder rewrite() {
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
                appendLimitRowCount(result, (RowCountLimitToken) each, count, sqlTokens);
            } else if (each instanceof OffsetLimitToken) {
                appendLimitOffsetToken(result, (OffsetLimitToken) each, count, sqlTokens);
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
        sqlBuilder.appendToken(tableName);
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
    
    private void appendLimitRowCount(final SQLBuilder sqlBuilder, final RowCountLimitToken rowCountLimitToken, final int count, final List<SQLToken> sqlTokens) {
        sqlBuilder.appendToken(RowCountLimitToken.COUNT_NAME, rowCountLimitToken.getRowCount() + limit.getOffset() + "");
        int beginPosition = rowCountLimitToken.getBeginPosition() + (rowCountLimitToken.getRowCount() + "").length();
        int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
        sqlBuilder.append(originalSQL.substring(beginPosition, endPosition));
    }
    
    private void appendLimitOffsetToken(final SQLBuilder sqlBuilder, final OffsetLimitToken offsetLimitToken, final int count, final List<SQLToken> sqlTokens) {
        sqlBuilder.appendToken(OffsetLimitToken.OFFSET_NAME, "0");
        int beginPosition = offsetLimitToken.getBeginPosition() + (offsetLimitToken.getOffset() + "").length();
        int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
        sqlBuilder.append(originalSQL.substring(beginPosition, endPosition));
    }
    
    /**
     * 修订SQL和参数.
     * 
     * @param sqlBuilder SQL构建器
     * @param parameters 参数
     */
    public void amend(final SQLBuilder sqlBuilder, final List<Object> parameters) {
        if (limit.getOffsetParameterIndex() > -1) {
            parameters.set(limit.getOffsetParameterIndex(), limit.getOffset());
        } else {
            sqlBuilder.amend(OffsetLimitToken.OFFSET_NAME, String.valueOf(limit.getOffset()));
        }
        if (limit.getRowCountParameterIndex() > -1) {
            parameters.set(limit.getRowCountParameterIndex(), limit.getRowCount());
        } else {
            sqlBuilder.amend(RowCountLimitToken.COUNT_NAME, String.valueOf(limit.getRowCount()));
        }
    }
}
