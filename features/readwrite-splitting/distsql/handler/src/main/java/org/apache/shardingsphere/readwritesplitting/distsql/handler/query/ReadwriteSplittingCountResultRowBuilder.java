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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.executor.rql.rule.CountResultRowBuilder;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Collection;
import java.util.Collections;

/**
 * Readwrite-splitting count result row builder.
 */
public final class ReadwriteSplittingCountResultRowBuilder implements CountResultRowBuilder<ReadwriteSplittingRule> {
    
    @Override
    public Collection<LocalDataQueryResultRow> generateRows(final ReadwriteSplittingRule rule, final String databaseName) {
        return Collections.singleton(
                new LocalDataQueryResultRow("readwrite_splitting", databaseName, rule.getAttributes().getAttribute(DataSourceMapperRuleAttribute.class).getDataSourceMapper().size()));
    }
    
    @Override
    public Class<ReadwriteSplittingRule> getRuleClass() {
        return ReadwriteSplittingRule.class;
    }
    
    @Override
    public String getType() {
        return "READWRITE_SPLITTING";
    }
}
