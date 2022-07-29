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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show SQL translator rule handler.
 */
public final class ShowSQLTranslatorRuleHandler extends QueryableRALBackendHandler<ShowSQLTranslatorRuleStatement> {
    
    private static final String TYPE = "type";
    
    private static final String USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED = "use_original_sql_when_translating_failed";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(TYPE, USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        SQLTranslatorRule sqlTranslatorRule = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLTranslatorRule.class);
        return Collections.singleton(new LocalDataQueryResultRow(null == sqlTranslatorRule.getConfiguration().getType() ? "" : sqlTranslatorRule.getConfiguration().getType(),
                String.valueOf(sqlTranslatorRule.getConfiguration().isUseOriginalSQLWhenTranslatingFailed())));
    }
}
