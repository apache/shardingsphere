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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetVariableStatement;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.properties.TypedPropertyValue;
import org.apache.shardingsphere.infra.properties.TypedPropertyValueException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.InvalidValueException;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.SetStatementExecutor;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.Optional;
import java.util.Properties;

/**
 * Set variable statement executor.
 */
@AllArgsConstructor
public final class SetVariableExecutor implements SetStatementExecutor {
    
    private final SetVariableStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    public ResponseHeader execute() {
        Enum type = getType(sqlStatement.getName());
        if (type instanceof ConfigurationPropertyKey) {
            handleConfigurationProperty((ConfigurationPropertyKey) type, sqlStatement.getValue());
        } else if (type instanceof VariableEnum) {
            handleVariables(sqlStatement);
        } else {
            throw new UnsupportedVariableException(sqlStatement.getName());
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private Enum getType(final String name) {
        try {
            return ConfigurationPropertyKey.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return VariableEnum.getValueOf(name);
        }
    }
    
    private void handleConfigurationProperty(final ConfigurationPropertyKey propertyKey, final String value) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        Optional<MetaDataPersistService> metaDataPersistService = contextManager.getMetaDataContexts().getMetaDataPersistService();
        Properties props = contextManager.getMetaDataContexts().getProps().getProps();
        props.put(propertyKey.getKey(), getValue(propertyKey, value));
        metaDataPersistService.ifPresent(persistService -> persistService.getPropsService().persist(props, true));
    }
    
    private Object getValue(final ConfigurationPropertyKey propertyKey, final String value) {
        try {
            TypedPropertyValue propertyValue = new TypedPropertyValue(propertyKey, value);
            return propertyValue.getValue();
        } catch (TypedPropertyValueException e) {
            throw new InvalidValueException(value);
        }
    }
    
    private void handleVariables(final SetVariableStatement statement) {
        VariableEnum variable = VariableEnum.getValueOf(statement.getName());
        switch (variable) {
            case AGENT_PLUGINS_ENABLED:
                Boolean agentPluginsEnabled = BooleanUtils.toBooleanObject(sqlStatement.getValue());
                SystemPropertyUtil.setSystemProperty(variable.name(), null == agentPluginsEnabled ? Boolean.FALSE.toString() : agentPluginsEnabled.toString());
                break;
            case TRANSACTION_TYPE:
                backendConnection.getTransactionStatus().setTransactionType(getTransactionType(sqlStatement.getValue()));
                break;
            default:
                throw new UnsupportedVariableException(statement.getName());
        }
    }
    
    private TransactionType getTransactionType(final String transactionTypeName) throws UnsupportedVariableException {
        try {
            return TransactionType.valueOf(transactionTypeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnsupportedVariableException(transactionTypeName);
        }
    }
}
