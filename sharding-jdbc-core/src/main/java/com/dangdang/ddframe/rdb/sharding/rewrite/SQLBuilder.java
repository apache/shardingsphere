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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL构建器.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLBuilder {
    
    private final List<Object> segments;
    
    private StringBuilder currentSegment;
    
    public SQLBuilder() {
        segments = new LinkedList<>();
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * 追加字面量.
     *
     * @param literals 字面量
     */
    public void appendLiterals(final String literals) {
        currentSegment.append(literals);
    }
    
    /**
     * 追加表占位符.
     *
     * @param tableName 表名称
     */
    public void appendTable(final String tableName) {
        segments.add(new TableToken(tableName));
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * 生成SQL语句.
     *
     * @param tableTokens 占位符集合
     * @return SQL语句
     */
    public String toSQL(final Map<String, String> tableTokens) {
        StringBuilder result = new StringBuilder();
        for (Object each : segments) {
            if (each instanceof TableToken && tableTokens.containsKey(((TableToken) each).tableName)) {
                result.append(tableTokens.get(((TableToken) each).tableName));
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
}
