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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountSingleTableStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Count single table executor.
 */
public final class CountSingleTableExecutor implements RQLExecutor<CountSingleTableStatement> {
    
    private String databaseName;
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final CountSingleTableStatement sqlStatement) {
        Optional<SingleRule> rule = database.getRuleMetaData().findSingleRule(SingleRule.class);
        databaseName = database.getName();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        rule.ifPresent(optional -> addSingleTableData(result, databaseName, rule.get()));
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("database", "count");
    }
    
    private void addSingleTableData(final Collection<LocalDataQueryResultRow> row, final String databaseName, final SingleRule rule) {
        row.add(new LocalDataQueryResultRow(databaseName, rule.getAllTables().size()));
    }
    
    @Override
    public String getType() {
        return CountSingleTableStatement.class.getName();
    }
}
