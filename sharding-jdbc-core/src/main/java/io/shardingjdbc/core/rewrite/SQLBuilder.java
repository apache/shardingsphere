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

package io.shardingjdbc.core.rewrite;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL builder.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLBuilder {
    
    private final List<Object> segments;
    
    private StringBuilder currentSegment;
    
    /**
     * Constructs a empty SQL builder.
     */
    public SQLBuilder() {
        segments = new LinkedList<>();
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Append literals.
     *
     * @param literals literals for SQL
     */
    public void appendLiterals(final String literals) {
        currentSegment.append(literals);
    }
    
    /**
     * Append index token.
     *
     * @param indexName index name
     * @param tableName table name
     */
    public void appendIndex(final String indexName, final String tableName) {
        segments.add(new IndexToken(indexName, tableName));
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Append table token.
     *
     * @param tableName table name
     */
    public void appendTable(final String tableName) {
        segments.add(new TableToken(tableName));
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Convert to SQL string.
     *
     * @param tableTokens table tokens
     * @return SQL string
     */
    public String toSQL(final Map<String, String> tableTokens) {
        StringBuilder result = new StringBuilder();
        for (Object each : segments) {
            if (each instanceof TableToken && tableTokens.containsKey(((TableToken) each).tableName)) {
                result.append(tableTokens.get(((TableToken) each).tableName));
            } else if (each instanceof IndexToken) {
                IndexToken indexToken = (IndexToken) each;
                result.append(indexToken.indexName);
                String tableName = tableTokens.get(indexToken.tableName);
                if (!Strings.isNullOrEmpty(tableName)) {
                    result.append("_");
                    result.append(tableName);
                }
            } else {
                result.append(each);
            }
        }
        return result.toString();
    }
    
    @RequiredArgsConstructor
    private class TableToken {
        
        private final String tableName;
        
        @Override
        public String toString() {
            return tableName;
        }
    }
    
    @RequiredArgsConstructor
    private class IndexToken {
        
        private final String indexName;
        
        private final String tableName;
        
        @Override
        public String toString() {
            return indexName;
        }
    }
}
