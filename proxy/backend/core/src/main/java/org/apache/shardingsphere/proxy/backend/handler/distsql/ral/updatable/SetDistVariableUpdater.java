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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetDistVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.props.TypedPropertyKey;
import org.apache.shardingsphere.infra.props.TypedPropertyValue;
import org.apache.shardingsphere.infra.props.exception.TypedPropertyValueException;
import org.apache.shardingsphere.logging.constant.LoggingConstants;
import org.apache.shardingsphere.logging.util.LoggingUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.InvalidValueException;
import org.apache.shardingsphere.proxy.backend.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.updater.ConnectionSessionRequiredRALUpdater;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Set dist variable statement updater.
 */
public final class SetDistVariableUpdater implements ConnectionSessionRequiredRALUpdater<SetDistVariableStatement> {
    
    @Override
    public void executeUpdate(final ConnectionSession connectionSession, final SetDistVariableStatement sqlStatement) {
        Enum<?> enumType = getEnumType(sqlStatement.getName());
        if (enumType instanceof TypedPropertyKey) {
            handleConfigurationProperty((TypedPropertyKey) enumType, sqlStatement.getValue());
        } else {
            throw new UnsupportedVariableException(sqlStatement.getName());
        }
    }
    
    private Enum<?> getEnumType(final String name) {
        try {
            return ConfigurationPropertyKey.valueOf(name.toUpperCase());
        } catch (final IllegalArgumentException ex) {
            try {
                return TemporaryConfigurationPropertyKey.valueOf(name.toUpperCase());
            } catch (final IllegalArgumentException exception) {
                throw new UnsupportedVariableException(name);
            }
        }
    }
    
    private void handleConfigurationProperty(final TypedPropertyKey propertyKey, final String value) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        MetaDataContexts metaDataContexts = contextManager.getMetaDataContexts();
        Properties props = new Properties();
        props.putAll(metaDataContexts.getMetaData().getProps().getProps());
        props.putAll(metaDataContexts.getMetaData().getTemporaryProps().getProps());
        props.put(propertyKey.getKey(), getValue(propertyKey, value));
        contextManager.getInstanceContext().getModeContextManager().alterProperties(props);
        refreshRootLogger(props);
        syncSQLShowToLoggingRule(propertyKey, metaDataContexts, value);
        syncSQLSimpleToLoggingRule(propertyKey, metaDataContexts, value);
    }
    
    private Object getValue(final TypedPropertyKey propertyKey, final String value) {
        try {
            Object propertyValue = new TypedPropertyValue(propertyKey, value).getValue();
            return Enum.class.isAssignableFrom(propertyKey.getType()) ? propertyValue.toString() : propertyValue;
        } catch (final TypedPropertyValueException ignored) {
            throw new InvalidValueException(value);
        }
    }
    
    private void refreshRootLogger(final Properties props) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        renewRootLoggerLevel(rootLogger, props);
    }
    
    private void renewRootLoggerLevel(final Logger rootLogger, final Properties props) {
        rootLogger.setLevel(Level.valueOf(props.getOrDefault(ConfigurationPropertyKey.SYSTEM_LOG_LEVEL.getKey(), ConfigurationPropertyKey.SYSTEM_LOG_LEVEL.getDefaultValue()).toString()));
    }
    
    private void syncSQLShowToLoggingRule(final TypedPropertyKey propertyKey, final MetaDataContexts metaDataContexts, final String value) {
        if (LoggingConstants.SQL_SHOW.equalsIgnoreCase(propertyKey.getKey())) {
            LoggingUtils.getSQLLogger(metaDataContexts.getMetaData().getGlobalRuleMetaData()).ifPresent(option -> {
                option.getProps().setProperty(LoggingConstants.SQL_LOG_ENABLE, value);
                metaDataContexts.getPersistService().getGlobalRuleService().persist(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations());
            });
        }
    }
    
    private void syncSQLSimpleToLoggingRule(final TypedPropertyKey propertyKey, final MetaDataContexts metaDataContexts, final String value) {
        if (LoggingConstants.SQL_SIMPLE.equalsIgnoreCase(propertyKey.getKey())) {
            LoggingUtils.getSQLLogger(metaDataContexts.getMetaData().getGlobalRuleMetaData()).ifPresent(option -> {
                option.getProps().setProperty(LoggingConstants.SQL_LOG_SIMPLE, value);
                metaDataContexts.getPersistService().getGlobalRuleService().persist(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations());
            });
        }
    }
    
    @Override
    public String getType() {
        return SetDistVariableStatement.class.getName();
    }
}
