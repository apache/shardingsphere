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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.util.props.TypedPropertyValue;
import org.apache.shardingsphere.infra.util.props.exception.TypedPropertyValueException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.InvalidValueException;
import org.apache.shardingsphere.proxy.backend.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.Properties;

/**
 * Set dist variable statement handler.
 */
public final class SetDistVariableHandler extends UpdatableRALBackendHandler<SetDistVariableStatement> {
    
    @Override
    protected void update(final ContextManager contextManager) {
        Enum<?> enumType = getEnumType(getSqlStatement().getName());
        if (enumType instanceof ConfigurationPropertyKey) {
            handleConfigurationProperty((ConfigurationPropertyKey) enumType, getSqlStatement().getValue());
        } else if (enumType instanceof VariableEnum) {
            handleVariables();
        } else {
            throw new UnsupportedVariableException(getSqlStatement().getName());
        }
    }
    
    private Enum<?> getEnumType(final String name) {
        try {
            return ConfigurationPropertyKey.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return VariableEnum.getValueOf(name);
        }
    }
    
    private void handleConfigurationProperty(final ConfigurationPropertyKey propertyKey, final String value) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        MetaDataContexts metaDataContexts = contextManager.getMetaDataContexts();
        Properties props = new Properties();
        props.putAll(metaDataContexts.getMetaData().getProps().getProps());
        props.put(propertyKey.getKey(), getValue(propertyKey, value));
        contextManager.alterProperties(props);
        MetaDataPersistService persistService = metaDataContexts.getPersistService();
        if (null != persistService.getPropsService()) {
            persistService.getPropsService().persist(props);
        }
    }
    
    private Object getValue(final ConfigurationPropertyKey propertyKey, final String value) {
        try {
            return new TypedPropertyValue(propertyKey, value).getValue();
        } catch (final TypedPropertyValueException ex) {
            throw new InvalidValueException(value);
        }
    }
    
    private void handleVariables() {
        VariableEnum variable = VariableEnum.getValueOf(getSqlStatement().getName());
        switch (variable) {
            case AGENT_PLUGINS_ENABLED:
                Boolean agentPluginsEnabled = BooleanUtils.toBooleanObject(getSqlStatement().getValue());
                SystemPropertyUtil.setSystemProperty(variable.name(), null == agentPluginsEnabled ? Boolean.FALSE.toString() : agentPluginsEnabled.toString());
                break;
            case TRANSACTION_TYPE:
                getConnectionSession().getTransactionStatus().setTransactionType(getTransactionType(getSqlStatement().getValue()));
                break;
            default:
                throw new UnsupportedVariableException(getSqlStatement().getName());
        }
    }
    
    private TransactionType getTransactionType(final String transactionTypeName) throws UnsupportedVariableException {
        try {
            return TransactionType.valueOf(transactionTypeName.toUpperCase());
        } catch (final IllegalArgumentException ex) {
            throw new UnsupportedVariableException(transactionTypeName);
        }
    }
}
