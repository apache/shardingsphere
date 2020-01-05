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

package org.apache.shardingsphere.core.shard;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.rewrite.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.sharding.rewrite.context.ShardingSQLRewriteContextDecorator;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;

import java.util.List;

/**
 * SQL rewrite entry.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLRewriteEntry {
    
    private final ShardingRule shardingRule;
    
    private final ShardingSphereProperties properties;
    
    private final ShardingSphereMetaData metaData;
    
    /**
     * Create SQL rewrite context.
     * 
     * @param sql SQL
     * @param parameters parameters
     * @param sqlRouteResult SQL route result
     * @return SQL rewrite context
     */
    public SQLRewriteContext createSQLRewriteContext(final String sql, final List<Object> parameters, final SQLRouteResult sqlRouteResult) {
        SQLRewriteContext result = new SQLRewriteContext(metaData.getRelationMetas(), sqlRouteResult.getSqlStatementContext(), sql, parameters);
        new ShardingSQLRewriteContextDecorator(sqlRouteResult).decorate(shardingRule, properties, result);
        new EncryptSQLRewriteContextDecorator().decorate(shardingRule.getEncryptRule(), properties, result);
        result.generateSQLTokens();
        return result;
    }
}
