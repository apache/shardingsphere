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

package org.apache.shardingsphere.shadow.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.type.rql.CountRQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.shadow.distsql.statement.CountShadowRuleStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.Collections;

/**
 * Count shadow rule executor.
 */
public final class CountShadowRuleExecutor extends CountRQLExecutor<CountShadowRuleStatement, ShadowRule> {
    
    public CountShadowRuleExecutor() {
        super(ShadowRule.class);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> generateRows(final ShadowRule rule, final String databaseName) {
        return Collections.singleton(new LocalDataQueryResultRow("shadow", databaseName, rule.getDataSourceMapper().size()));
    }
    
    @Override
    public Class<CountShadowRuleStatement> getType() {
        return CountShadowRuleStatement.class;
    }
}
