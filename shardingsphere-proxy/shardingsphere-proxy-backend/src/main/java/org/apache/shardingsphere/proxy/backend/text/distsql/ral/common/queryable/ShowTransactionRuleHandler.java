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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Show transaction rule handler.
 */
@RequiredArgsConstructor
public final class ShowTransactionRuleHandler extends QueryableRALBackendHandler<ShowTransactionRuleStatement, ShowTransactionRuleHandler> {
    
    private static final String DEFAULT_TYPE = "default_type";
    
    private static final String PROVIDER_TYPE = "provider_type";
    
    private static final String PROPS = "props";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(DEFAULT_TYPE, PROVIDER_TYPE, PROPS);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        Optional<TransactionRuleConfiguration> optionalTransactionRuleConfiguration = ProxyContext.getInstance().getContextManager()
                .getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(TransactionRuleConfiguration.class).stream().findAny();
        if (!optionalTransactionRuleConfiguration.isPresent()) {
            return Collections.emptyList();
        }
        TransactionRuleConfiguration transactionRuleConfiguration = optionalTransactionRuleConfiguration.get();
        List<Object> row = new LinkedList<>();
        row.add(transactionRuleConfiguration.getDefaultType());
        row.add(null == transactionRuleConfiguration.getProviderType() ? "" : transactionRuleConfiguration.getProviderType());
        row.add(null == transactionRuleConfiguration.getProps() ? "" : new Gson().toJson(transactionRuleConfiguration.getProps()));
        Collection<List<Object>> rows = new LinkedList<>();
        rows.add(row);
        return rows;
    }
}
