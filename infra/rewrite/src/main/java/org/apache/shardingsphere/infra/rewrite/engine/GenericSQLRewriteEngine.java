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

package org.apache.shardingsphere.infra.rewrite.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.rewrite.sql.impl.DefaultSQLBuilder;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;

import java.util.Map;

/**
 * Generic SQL rewrite engine.
 */
@RequiredArgsConstructor
public final class GenericSQLRewriteEngine {
    
    private final SQLTranslatorRule translatorRule;
    
    private final DatabaseType protocolType;
    
    private final Map<String, DatabaseType> storageTypes;
    
    /**
     * Rewrite SQL and parameters.
     *
     * @param sqlRewriteContext SQL rewrite context
     * @return SQL rewrite result
     */
    public GenericSQLRewriteResult rewrite(final SQLRewriteContext sqlRewriteContext) {
        String sql = translatorRule.translate(
                new DefaultSQLBuilder(sqlRewriteContext).toSQL(), sqlRewriteContext.getSqlStatementContext().getSqlStatement(), protocolType,
                storageTypes.isEmpty() ? protocolType : storageTypes.values().iterator().next());
        return new GenericSQLRewriteResult(new SQLRewriteUnit(sql, sqlRewriteContext.getParameterBuilder().getParameters()));
    }
}
