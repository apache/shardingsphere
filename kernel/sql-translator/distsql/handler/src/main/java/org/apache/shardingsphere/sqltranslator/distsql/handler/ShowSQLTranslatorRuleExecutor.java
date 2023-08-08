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

package org.apache.shardingsphere.sqltranslator.distsql.handler;

import org.apache.shardingsphere.distsql.handler.ral.query.MetaDataRequiredQueryableRALExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.distsql.parser.statement.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show SQL translator rule executor.
 */
public final class ShowSQLTranslatorRuleExecutor implements MetaDataRequiredQueryableRALExecutor<ShowSQLTranslatorRuleStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final ShowSQLTranslatorRuleStatement sqlStatement) {
        SQLTranslatorRule rule = metaData.getGlobalRuleMetaData().getSingleRule(SQLTranslatorRule.class);
        return buildData(rule.getConfiguration());
    }
    
    private Collection<LocalDataQueryResultRow> buildData(final SQLTranslatorRuleConfiguration ruleConfig) {
        return Collections.singleton(new LocalDataQueryResultRow(null != ruleConfig.getType() ? ruleConfig.getType() : "", String.valueOf(ruleConfig.isUseOriginalSQLWhenTranslatingFailed())));
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("type", "use_original_sql_when_translating_failed");
    }
    
    @Override
    public Class<ShowSQLTranslatorRuleStatement> getType() {
        return ShowSQLTranslatorRuleStatement.class;
    }
}
