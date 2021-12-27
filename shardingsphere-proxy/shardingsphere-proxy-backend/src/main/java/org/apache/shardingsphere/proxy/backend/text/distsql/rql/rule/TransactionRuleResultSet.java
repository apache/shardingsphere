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

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowTransactionRuleStatement;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

/**
 * Result set for show transaction rule.
 */
public final class TransactionRuleResultSet implements DistSQLResultSet {
    
    private Iterator<TransactionRuleConfiguration> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<TransactionRuleConfiguration> ruleConfiguration = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getGlobalRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof TransactionRuleConfiguration).map(each -> (TransactionRuleConfiguration) each).findAny();
        ruleConfiguration.ifPresent(op -> data = Collections.singletonList(op).iterator());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("default_type", "provider_type");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        TransactionRuleConfiguration next = data.next();
        return Arrays.asList(next.getDefaultType(), next.getProviderType());
    }
    
    @Override
    public String getType() {
        return ShowTransactionRuleStatement.class.getCanonicalName();
    }
}
