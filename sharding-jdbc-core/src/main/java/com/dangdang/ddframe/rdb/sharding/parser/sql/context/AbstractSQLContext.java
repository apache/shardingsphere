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

import com.dangdang.ddframe.rdb.sharding.parser.result.router.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL上下文抽象类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractSQLContext implements SQLContext {
    
    private final String originalSQL;
    
    private List<TableContext> tables = new ArrayList<>();
    
    private final Collection<ConditionContext> conditionContexts = new LinkedList<>();
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
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
