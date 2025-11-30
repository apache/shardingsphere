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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.rewrite.sql.impl.DefaultSQLBuilder;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sqltranslator.context.SQLTranslatorContext;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Generic SQL rewrite engine.
 */
@RequiredArgsConstructor
public final class GenericSQLRewriteEngine {
    
    private final SQLTranslatorRule translatorRule;
    
    private final ShardingSphereDatabase database;
    
    private final RuleMetaData globalRuleMetaData;
    
    /**
     * Rewrite SQL and parameters.
     *
     * @param sqlRewriteContext SQL rewrite context
     * @param queryContext query context
     * @return SQL rewrite result
     */
    public GenericSQLRewriteResult rewrite(final SQLRewriteContext sqlRewriteContext, final QueryContext queryContext) {
        DatabaseType protocolType = database.getProtocolType();
        Map<String, StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnits();
        DatabaseType storageType = storageUnits.isEmpty() ? protocolType : storageUnits.values().iterator().next().getStorageType();
        String sql = sqlRewriteContext.getSql();
        List<Object> parameters = sqlRewriteContext.getParameterBuilder().getParameters();
        Optional<SQLTranslatorContext> sqlTranslatorContext = translatorRule.translate(
                new DefaultSQLBuilder(sql, sqlRewriteContext.getSqlTokens()).toSQL(), parameters, queryContext, storageType, database, globalRuleMetaData);
        String translatedSQL = sqlTranslatorContext.isPresent() ? sqlTranslatorContext.get().getSql() : sql;
        List<Object> translatedParameters = sqlTranslatorContext.isPresent() ? sqlTranslatorContext.get().getParameters() : parameters;
        return new GenericSQLRewriteResult(new SQLRewriteUnit(translatedSQL, translatedParameters));
    }
}
