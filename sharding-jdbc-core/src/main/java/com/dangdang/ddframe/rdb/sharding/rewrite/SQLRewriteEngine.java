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

import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ItemsToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.OffsetLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.RowCountLimitToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.SQLToken;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.TableToken;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * SQL重写引擎.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLRewriteEngine {
    
    private final SQLBuilderContext sqlBuilderContext;
    
    private SQLBuilder sqlBuilder;
    
    /**
     * SQL重写.
     * 
     * @return SQL构建器
     */
    public SQLBuilder rewrite() {
        if (null == sqlBuilder) {
            sqlBuilder = rewriteInternal(sqlBuilderContext);
        }
        return sqlBuilder;
    }
    
    private SQLBuilder rewriteInternal(final SQLBuilderContext sqlBuilderContext) {
        SQLBuilder result = new SQLBuilder();
        if (sqlBuilderContext.getSqlTokens().isEmpty()) {
            result.append(sqlBuilderContext.getOriginalSQL());
            return result;
        }
        int count = 0;
        List<SQLToken> sqlTokens = sqlBuilderContext.getSqlTokens();
        sortByBeginPosition();
        for (SQLToken each : sqlTokens) {
            if (0 == count) {
                result.append(sqlBuilderContext.getOriginalSQL().substring(0, each.getBeginPosition()));
            }
            if (each instanceof TableToken) {
                String tableName = ((TableToken) each).getTableName();
                if (sqlBuilderContext.getTableNames().contains(tableName)) {
                    result.appendToken(tableName);
                    int beginPosition = each.getBeginPosition() + ((TableToken) each).getOriginalLiterals().length();
                    int endPosition = sqlTokens.size() - 1 == count ? sqlBuilderContext.getOriginalSQL().length() : sqlTokens.get(count + 1).getBeginPosition();
                    result.append(sqlBuilderContext.getOriginalSQL().substring(beginPosition, endPosition));
                } else {
                    result.append(((TableToken) each).getOriginalLiterals());
                    int beginPosition = each.getBeginPosition() + ((TableToken) each).getOriginalLiterals().length();
                    int endPosition = sqlTokens.size() - 1 == count ? sqlBuilderContext.getOriginalSQL().length() : sqlTokens.get(count + 1).getBeginPosition();
                    result.append(sqlBuilderContext.getOriginalSQL().substring(beginPosition, endPosition));
                }
            } else if (each instanceof ItemsToken) {
                for (String item : ((ItemsToken) each).getItems()) {
                    result.append(", ");
                    result.append(item);
                }
                int beginPosition = each.getBeginPosition();
                int endPosition = sqlTokens.size() - 1 == count ? sqlBuilderContext.getOriginalSQL().length() : sqlTokens.get(count + 1).getBeginPosition();
                result.append(sqlBuilderContext.getOriginalSQL().substring(beginPosition, endPosition));
            } else if (each instanceof RowCountLimitToken) {
                result.appendToken(RowCountLimitToken.COUNT_NAME, ((RowCountLimitToken) each).getRowCount() + "");
                int beginPosition = each.getBeginPosition() + (((RowCountLimitToken) each).getRowCount() + "").length();
                int endPosition = sqlTokens.size() - 1 == count ? sqlBuilderContext.getOriginalSQL().length() : sqlTokens.get(count + 1).getBeginPosition();
                result.append(sqlBuilderContext.getOriginalSQL().substring(beginPosition, endPosition));
            } else if (each instanceof OffsetLimitToken) {
                result.appendToken(OffsetLimitToken.OFFSET_NAME, ((OffsetLimitToken) each).getOffset() + "");
                int beginPosition = each.getBeginPosition() + (((OffsetLimitToken) each).getOffset() + "").length();
                int endPosition = sqlTokens.size() - 1 == count ? sqlBuilderContext.getOriginalSQL().length() : sqlTokens.get(count + 1).getBeginPosition();
                result.append(sqlBuilderContext.getOriginalSQL().substring(beginPosition, endPosition));
            }
            count++;
        }
        return result;
    }
    
    private void sortByBeginPosition() {
        Collections.sort(sqlBuilderContext.getSqlTokens(), new Comparator<SQLToken>() {
            
            @Override
            public int compare(final SQLToken o1, final SQLToken o2) {
                return o1.getBeginPosition() - o2.getBeginPosition();
            }
        });
    }
}
