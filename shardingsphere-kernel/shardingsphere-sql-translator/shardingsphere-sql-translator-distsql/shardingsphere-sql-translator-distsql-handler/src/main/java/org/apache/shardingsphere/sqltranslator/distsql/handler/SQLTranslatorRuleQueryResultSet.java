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

import org.apache.shardingsphere.infra.distsql.query.GlobalRuleDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.distsql.parser.statement.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

/**
 * Query result set for SQL translator rule.
 */
public final class SQLTranslatorRuleQueryResultSet implements GlobalRuleDistSQLResultSet {
    
    private static final String TYPE = "type";
    
    private static final String USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED = "use_original_sql_when_translating_failed";
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereRuleMetaData ruleMetaData, final SQLStatement sqlStatement) {
        Optional<SQLTranslatorRule> rule = ruleMetaData.findSingleRule(SQLTranslatorRule.class);
        rule.ifPresent(optional -> data = buildData(optional.getConfiguration()).iterator());
    }
    
    private Collection<Collection<Object>> buildData(final SQLTranslatorRuleConfiguration ruleConfig) {
        return Collections.singleton(Arrays.asList(null != ruleConfig.getType() ? ruleConfig.getType() : "", String.valueOf(ruleConfig.isUseOriginalSQLWhenTranslatingFailed())));
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(TYPE, USE_ORIGINAL_SQL_WHEN_TRANSLATING_FAILED);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowSQLTranslatorRuleStatement.class.getName();
    }
}
