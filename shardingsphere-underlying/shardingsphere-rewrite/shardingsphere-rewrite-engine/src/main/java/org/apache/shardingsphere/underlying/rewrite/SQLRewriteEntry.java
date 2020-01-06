/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.underlying.rewrite;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContextDecorator;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL rewrite entry.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLRewriteEntry {
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingSphereProperties properties;
    
    /**
     * Create SQL rewrite context.
     * 
     * @param sql SQL
     * @param parameters parameters
     * @param sqlStatementContext SQL statement context
     * @param decorators SQL rewrite context decorators
     * @return SQL rewrite context
     */
    public SQLRewriteContext createSQLRewriteContext(final String sql, final List<Object> parameters, 
                                                     final SQLStatementContext sqlStatementContext, final Map<BaseRule, SQLRewriteContextDecorator> decorators) {
        SQLRewriteContext result = new SQLRewriteContext(metaData.getRelationMetas(), sqlStatementContext, sql, parameters);
        decorate(decorators, result);
        result.generateSQLTokens();
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private void decorate(final Map<BaseRule, SQLRewriteContextDecorator> decorators, final SQLRewriteContext sqlRewriteContext) {
        for (Entry<BaseRule, SQLRewriteContextDecorator> entry : decorators.entrySet()) {
            entry.getValue().decorate(entry.getKey(), properties, sqlRewriteContext);
        }
    }
}
