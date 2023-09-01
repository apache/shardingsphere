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

package org.apache.shardingsphere.broadcast.distsql.handler.query;

import org.apache.shardingsphere.broadcast.distsql.parser.statement.ShowBroadcastTableRulesStatement;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Show broadcast table rule executor.
 */
public final class ShowBroadcastTableRuleExecutor implements RQLExecutor<ShowBroadcastTableRulesStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowBroadcastTableRulesStatement sqlStatement) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        Optional<BroadcastRule> rule = database.getRuleMetaData().findSingleRule(BroadcastRule.class);
        rule.ifPresent(optional -> optional.getConfiguration().getTables().forEach(each -> result.add(new LocalDataQueryResultRow(each))));
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Collections.singleton("broadcast_table");
    }
    
    @Override
    public Class<ShowBroadcastTableRulesStatement> getType() {
        return ShowBroadcastTableRulesStatement.class;
    }
}
