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

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariablesStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.executor.ConnectionSessionRequiredQueryableRALExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Show dist variables executor.
 */
public final class ShowDistVariablesExecutor implements ConnectionSessionRequiredQueryableRALExecutor<ShowDistVariablesStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("variable_name", "variable_value");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final ConnectionSession connectionSession, final ShowDistVariablesStatement sqlStatement) {
        Collection<LocalDataQueryResultRow> result = ConfigurationPropertyKey.getKeyNames().stream()
                .map(each -> new LocalDataQueryResultRow(each.toLowerCase(), metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(each)).toString())).collect(Collectors.toList());
        result.add(new LocalDataQueryResultRow(
                VariableEnum.AGENT_PLUGINS_ENABLED.name().toLowerCase(), SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.TRUE.toString())));
        result.add(new LocalDataQueryResultRow(VariableEnum.CACHED_CONNECTIONS.name().toLowerCase(), connectionSession.getBackendConnection().getConnectionSize()));
        result.add(new LocalDataQueryResultRow(VariableEnum.TRANSACTION_TYPE.name().toLowerCase(), connectionSession.getTransactionStatus().getTransactionType().name()));
        return result;
    }
    
    @Override
    public String getType() {
        return ShowDistVariablesStatement.class.getName();
    }
}
