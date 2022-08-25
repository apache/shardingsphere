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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show transaction rule handler.
 */
public final class ShowTransactionRuleHandler extends QueryableRALBackendHandler<ShowTransactionRuleStatement> {
    
    private static final String DEFAULT_TYPE = "default_type";
    
    private static final String PROVIDER_TYPE = "provider_type";
    
    private static final String PROPS = "props";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(DEFAULT_TYPE, PROVIDER_TYPE, PROPS);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        TransactionRule rule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        return Collections.singleton(new LocalDataQueryResultRow(
                rule.getDefaultType().name(), null == rule.getProviderType() ? "" : rule.getProviderType(), null == rule.getProps() ? "" : new Gson().toJson(rule.getProps())));
    }
}
