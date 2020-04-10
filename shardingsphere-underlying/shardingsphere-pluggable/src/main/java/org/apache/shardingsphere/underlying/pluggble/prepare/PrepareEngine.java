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

package org.apache.shardingsphere.underlying.pluggble.prepare;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContextBuilder;
import org.apache.shardingsphere.underlying.executor.log.SQLLogger;
import org.apache.shardingsphere.underlying.rewrite.engine.result.SQLRewriteResult;

/**
 * Prepare engine.
 */
@RequiredArgsConstructor
public final class PrepareEngine {
    
    private final ConfigurationProperties properties;
    
    private final ShardingSphereMetaData metaData;
    
    /**
     * Prepare to execute.
     *
     * @param sql SQL
     * @param sqlStatementContext SQL statement context
     * @param sqlRewriteResult SQL rewrite result
     * @return execution context
     */
    public ExecutionContext prepare(final String sql, final SQLStatementContext sqlStatementContext, final SQLRewriteResult sqlRewriteResult) {
        ExecutionContext result = new ExecutionContext(sqlStatementContext, ExecutionContextBuilder.build(metaData, sqlRewriteResult));
        if (properties.<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(sql, properties.<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), result);
        }
        return result;
    }
}
