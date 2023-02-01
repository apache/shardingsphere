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

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.executor.ConnectionSessionRequiredQueryableRALExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.transaction.api.TransactionType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show dist variable executor.
 */
public final class ShowDistVariableExecutor implements ConnectionSessionRequiredQueryableRALExecutor<ShowDistVariableStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("variable_name", "variable_value");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final ConnectionSession connectionSession, final ShowDistVariableStatement sqlStatement) {
        return buildSpecifiedRow(metaData, connectionSession, sqlStatement.getName());
    }
    
    private Collection<LocalDataQueryResultRow> buildSpecifiedRow(final ShardingSphereMetaData metaData, final ConnectionSession connectionSession, final String variableName) {
        return isConfigurationKey(variableName)
                ? Collections.singletonList(new LocalDataQueryResultRow(variableName.toLowerCase(), getConfigurationValue(metaData, variableName)))
                : Collections.singletonList(new LocalDataQueryResultRow(variableName.toLowerCase(), getSpecialValue(connectionSession, variableName)));
    }
    
    private boolean isConfigurationKey(final String key) {
        return ConfigurationPropertyKey.getKeyNames().contains(key);
    }
    
    private String getConfigurationValue(final ShardingSphereMetaData metaData, final String variableName) {
        return metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(variableName)).toString();
    }
    
    private String getSpecialValue(final ConnectionSession connectionSession, final String variableName) {
        VariableEnum variable = VariableEnum.getValueOf(variableName);
        switch (variable) {
            case AGENT_PLUGINS_ENABLED:
                return SystemPropertyUtil.getSystemProperty(variable.name(), Boolean.TRUE.toString());
            case CACHED_CONNECTIONS:
                int connectionSize = connectionSession.getBackendConnection().getConnectionSize();
                return String.valueOf(connectionSize);
            case TRANSACTION_TYPE:
                TransactionType transactionType = connectionSession.getTransactionStatus().getTransactionType();
                return transactionType.name();
            default:
        }
        throw new UnsupportedVariableException(variableName);
    }
    
    @Override
    public String getType() {
        return ShowDistVariableStatement.class.getName();
    }
}
