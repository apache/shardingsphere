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

package org.apache.shardingsphere.distsql.handler.type.rql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.statement.rql.RQLStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Count RQL executor.
 * 
 * @param <T> type of RQL statement
 * @param <R> type of ShardingSphere rule
 */
@RequiredArgsConstructor
public abstract class CountRQLExecutor<T extends RQLStatement, R extends ShardingSphereRule> implements RQLExecutor<T> {
    
    private final Class<R> ruleClass; 
    
    @Override
    public final Collection<String> getColumnNames() {
        return Arrays.asList("rule_name", "database", "count");
    }
    
    @Override
    public final Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final T sqlStatement) {
        Optional<R> rule = database.getRuleMetaData().findSingleRule(ruleClass);
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        rule.ifPresent(optional -> result.addAll(generateRows(optional, database.getName())));
        return result;
    }
    
    protected abstract Collection<LocalDataQueryResultRow> generateRows(R rule, String databaseName);
}
