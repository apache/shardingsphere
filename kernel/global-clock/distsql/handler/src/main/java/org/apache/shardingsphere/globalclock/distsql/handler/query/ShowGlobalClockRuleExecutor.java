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

package org.apache.shardingsphere.globalclock.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.GlobalRuleAwareRQLExecutor;
import org.apache.shardingsphere.globalclock.api.config.GlobalClockRuleConfiguration;
import org.apache.shardingsphere.globalclock.core.rule.GlobalClockRule;
import org.apache.shardingsphere.globalclock.distsql.statement.queryable.ShowGlobalClockRuleStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.props.PropertiesConverter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show global clock rule executor.
 */
@Setter
public final class ShowGlobalClockRuleExecutor implements GlobalRuleAwareRQLExecutor<ShowGlobalClockRuleStatement, GlobalClockRule> {
    
    private GlobalClockRule rule;
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("type", "provider", "enable", "props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowGlobalClockRuleStatement sqlStatement) {
        GlobalClockRuleConfiguration ruleConfig = rule.getConfiguration();
        return Collections.singleton(new LocalDataQueryResultRow(ruleConfig.getType(), ruleConfig.getProvider(),
                String.valueOf(ruleConfig.isEnabled()), PropertiesConverter.convert(ruleConfig.getProps())));
    }
    
    @Override
    public Class<GlobalClockRule> getRuleClass() {
        return GlobalClockRule.class;
    }
    
    @Override
    public Class<ShowGlobalClockRuleStatement> getType() {
        return ShowGlobalClockRuleStatement.class;
    }
}
