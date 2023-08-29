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
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.logging.constant.LoggingConstants;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.util.LoggingUtils;
import org.apache.shardingsphere.proxy.backend.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.executor.ConnectionSessionRequiredQueryableRALExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

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
        if (isConfigurationKey(variableName)) {
            return Collections.singletonList(new LocalDataQueryResultRow(variableName.toLowerCase(), getConfigurationValue(metaData, variableName)));
        } else if (isTemporaryConfigurationKey(variableName)) {
            return Collections.singletonList(new LocalDataQueryResultRow(variableName.toLowerCase(), getTemporaryConfigurationValue(metaData, variableName)));
        }
        return Collections.singletonList(new LocalDataQueryResultRow(variableName.toLowerCase(), getSpecialValue(connectionSession, variableName)));
    }
    
    private boolean isConfigurationKey(final String variableName) {
        return ConfigurationPropertyKey.getKeyNames().contains(variableName);
    }
    
    private String getConfigurationValue(final ShardingSphereMetaData metaData, final String variableName) {
        if (LoggingConstants.SQL_SHOW_VARIABLE_NAME.equalsIgnoreCase(variableName) || LoggingConstants.SQL_SIMPLE_VARIABLE_NAME.equalsIgnoreCase(variableName)) {
            return getLoggingPropsValue(metaData, variableName);
        }
        return metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(variableName)).toString();
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
        return metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(variableName)).toString();
    }
    
    private boolean isTemporaryConfigurationKey(final String variableName) {
        return TemporaryConfigurationPropertyKey.getKeyNames().contains(variableName);
    }
    
    private String getTemporaryConfigurationValue(final ShardingSphereMetaData metaData, final String variableName) {
        return metaData.getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.valueOf(variableName)).toString();
    }
    
    private String getSpecialValue(final ConnectionSession connectionSession, final String variableName) {
        VariableEnum variable = VariableEnum.getValueOf(variableName);
        if (variable == VariableEnum.CACHED_CONNECTIONS) {
            int connectionSize = connectionSession.getDatabaseConnectionManager().getConnectionSize();
            return String.valueOf(connectionSize);
        }
        throw new UnsupportedVariableException(variableName);
    }
    
    @Override
    public Class<ShowDistVariableStatement> getType() {
        return ShowDistVariableStatement.class;
    }
}
