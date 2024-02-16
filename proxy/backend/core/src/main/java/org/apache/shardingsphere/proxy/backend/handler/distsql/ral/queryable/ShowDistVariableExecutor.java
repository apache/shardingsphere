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

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorConnectionContextAware;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.ral.queryable.show.ShowDistVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.logging.constant.LoggingConstants;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.util.LoggingUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.DistSQLVariable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

/**
 * Show dist variable executor.
 */
@Setter
public final class ShowDistVariableExecutor implements DistSQLQueryExecutor<ShowDistVariableStatement>, DistSQLExecutorConnectionContextAware {
    
    private DistSQLConnectionContext connectionContext;
    
    @Override
    public Collection<String> getColumnNames(final ShowDistVariableStatement sqlStatement) {
        return Arrays.asList("variable_name", "variable_value");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowDistVariableStatement sqlStatement, final ContextManager contextManager) {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        String variableName = sqlStatement.getName();
        if (isConfigurationKey(variableName)) {
            return Collections.singleton(new LocalDataQueryResultRow(variableName.toLowerCase(), getConfigurationValue(metaData, variableName)));
        }
        if (isTemporaryConfigurationKey(variableName)) {
            return Collections.singleton(new LocalDataQueryResultRow(variableName.toLowerCase(), getTemporaryConfigurationValue(metaData, variableName)));
        }
        return Collections.singleton(new LocalDataQueryResultRow(variableName.toLowerCase(), getConnectionSize(variableName)));
    }
    
    private boolean isConfigurationKey(final String variableName) {
        return ConfigurationPropertyKey.getKeyNames().contains(variableName);
    }
    
    private String getConfigurationValue(final ShardingSphereMetaData metaData, final String variableName) {
        return LoggingConstants.SQL_SHOW_VARIABLE_NAME.equalsIgnoreCase(variableName) || LoggingConstants.SQL_SIMPLE_VARIABLE_NAME.equalsIgnoreCase(variableName)
                ? getLoggingPropsValue(metaData, variableName)
                : getStringResult(metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(variableName)));
    }
    
    private String getLoggingPropsValue(final ShardingSphereMetaData metaData, final String variableName) {
        Optional<ShardingSphereLogger> sqlLogger = LoggingUtils.getSQLLogger(metaData.getGlobalRuleMetaData());
        if (sqlLogger.isPresent()) {
            Properties props = sqlLogger.get().getProps();
            switch (variableName) {
                case LoggingConstants.SQL_SHOW_VARIABLE_NAME:
                    return props.getOrDefault(LoggingConstants.SQL_LOG_ENABLE,
                            metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(variableName)).toString()).toString();
                case LoggingConstants.SQL_SIMPLE_VARIABLE_NAME:
                    return props.getOrDefault(LoggingConstants.SQL_LOG_SIMPLE,
                            metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(variableName)).toString()).toString();
                default:
            }
        }
        return getStringResult(metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(variableName)));
    }
    
    private boolean isTemporaryConfigurationKey(final String variableName) {
        return TemporaryConfigurationPropertyKey.getKeyNames().contains(variableName);
    }
    
    private String getTemporaryConfigurationValue(final ShardingSphereMetaData metaData, final String variableName) {
        return getStringResult(metaData.getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.valueOf(variableName)).toString());
    }
    
    private String getConnectionSize(final String variableName) {
        ShardingSpherePreconditions.checkState(DistSQLVariable.CACHED_CONNECTIONS == DistSQLVariable.getValueOf(variableName), () -> new UnsupportedVariableException(variableName));
        return String.valueOf(connectionContext.getConnectionSize());
    }
    
    private String getStringResult(final Object value) {
        if (null == value) {
            return "";
        }
        return value instanceof TypedSPI ? ((TypedSPI) value).getType().toString() : value.toString();
    }
    
    @Override
    public Class<ShowDistVariableStatement> getType() {
        return ShowDistVariableStatement.class;
    }
}
