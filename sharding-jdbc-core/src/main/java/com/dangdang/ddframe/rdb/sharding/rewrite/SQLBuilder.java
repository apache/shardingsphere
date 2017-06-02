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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
     * 追加占位符.
     * 
     * @param token 占位符
     */
    public void append(final SQLBuilderToken token) {
        segments.add(token);
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * 追加字面量.
     *
     * @param literals 字面量
     */
    public void append(final String literals) {
        currentSegment.append(literals);
    }
    
    /**
     * 用实际的值替代占位符,并返回新的构建器.
     *
     * @param tokens 占位符集合
     * @return 新SQL构建器
     */
    public SQLBuilder createNewSQLBuilder(final Collection<SQLBuilderToken> tokens) {
        for (Object each : segments) {
            if (each instanceof SQLBuilderToken) {
                setToken((SQLBuilderToken) each, tokens);
            }
        }
        return new SQLBuilder(segments);
    }
    
    private void setToken(final SQLBuilderToken targetToken, final Collection<SQLBuilderToken> tokens) {
        for (SQLBuilderToken each : tokens) {
            if (targetToken.getLabel().equals(each.getLabel())) {
                targetToken.setValue(each.getValue());
            }
        }
    }
    
    /**
     * 生成SQL语句.
     * 
     * @return SQL语句
     */
    public String toSQL() {
        StringBuilder result = new StringBuilder();
        for (Object each : segments) {
            result.append(each);
        }
        return result.toString();
    }
    
    // TODO remove
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Object each : segments) {
            if (each instanceof SQLBuilderToken) {
                result.append(((SQLBuilderToken) each).toToken());
            } else {
                result.append(each.toString());
            }
        }
        return result.toString();
    }
}
