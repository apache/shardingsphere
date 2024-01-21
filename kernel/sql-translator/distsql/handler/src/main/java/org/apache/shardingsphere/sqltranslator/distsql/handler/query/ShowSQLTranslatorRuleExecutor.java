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

package org.apache.shardingsphere.sqltranslator.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.GlobalRuleAwareRQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.props.PropertiesConverter;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.distsql.statement.queryable.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show SQL translator rule executor.
 */
@Setter
public final class ShowSQLTranslatorRuleExecutor implements GlobalRuleAwareRQLExecutor<ShowSQLTranslatorRuleStatement, SQLTranslatorRule> {
    
    private SQLTranslatorRule rule;
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("type", "props", "use_original_sql_when_translating_failed");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowSQLTranslatorRuleStatement sqlStatement) {
        SQLTranslatorRuleConfiguration ruleConfig = rule.getConfiguration();
        return Collections.singleton(new LocalDataQueryResultRow(null == ruleConfig.getType() ? "" : ruleConfig.getType(),
                PropertiesConverter.convert(ruleConfig.getProps()), String.valueOf(ruleConfig.isUseOriginalSQLWhenTranslatingFailed())));
    }
    
    @Override
    public Class<SQLTranslatorRule> getRuleClass() {
        return SQLTranslatorRule.class;
    }
    
    @Override
    public Class<ShowSQLTranslatorRuleStatement> getType() {
        return ShowSQLTranslatorRuleStatement.class;
    }
}
