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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.rule;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowSingleTableRulesStatement;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

/**
 * Result set for show single table rules.
 */
public final class SingleTableRulesQueryResultSet implements DistSQLResultSet {
    
    private Iterator<String> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<SingleTableRuleConfiguration> ruleConfiguration = metaData.getRuleMetaData().getConfigurations().stream()
                .filter(each -> each instanceof SingleTableRuleConfiguration).map(each -> (SingleTableRuleConfiguration) each).findAny();
        ruleConfiguration.flatMap(SingleTableRuleConfiguration::getDefaultDataSource).ifPresent(op -> data = Collections.singletonList(op).iterator());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "resource_name");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return Arrays.asList("default", data.next());
    }
    
    @Override
    public String getType() {
        return ShowSingleTableRulesStatement.class.getCanonicalName();
    }
}
