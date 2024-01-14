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

package org.apache.shardingsphere.mask.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.type.rql.CountRQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mask.distsql.statement.CountMaskRuleStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;

import java.util.Collection;
import java.util.Collections;

/**
 * Count mask rule executor.
 */
public final class CountMaskRuleExecutor extends CountRQLExecutor<CountMaskRuleStatement, MaskRule> {
    
    public CountMaskRuleExecutor() {
        super(MaskRule.class);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> generateRows(final MaskRule rule, final String databaseName) {
        return Collections.singleton(new LocalDataQueryResultRow("mask", databaseName, rule.getLogicTableMapper().getTableNames().size()));
    }
    
    @Override
    public Class<CountMaskRuleStatement> getType() {
        return CountMaskRuleStatement.class;
    }
}
