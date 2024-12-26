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

package org.apache.shardingsphere.infra.parser.cache.hook;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.util.Collection;

public class SPISQLStatementParserCacheHook implements SQLStatementParserCacheHook {
    
    private final Collection<SQLStatementParserCacheHook> sqlStatementParserCacheHooks = ShardingSphereServiceLoader.getServiceInstances(SQLStatementParserCacheHook.class);
    
    @Override
    public void start(final String sql) {
        for (SQLStatementParserCacheHook each : sqlStatementParserCacheHooks) {
            each.start(sql);
        }
    }
    
    @Override
    public void finishSuccess(final String sql, final SQLStatement sqlStatement) {
        for (SQLStatementParserCacheHook each : sqlStatementParserCacheHooks) {
            each.finishSuccess(sql, sqlStatement);
        }
    }
    
    @Override
    public void finishFailure(final String sql, final Exception cause) {
        for (SQLStatementParserCacheHook each : sqlStatementParserCacheHooks) {
            each.finishFailure(sql, cause);
        }
    }
}
