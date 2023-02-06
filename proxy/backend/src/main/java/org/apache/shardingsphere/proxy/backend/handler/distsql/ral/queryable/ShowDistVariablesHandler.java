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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.logging.constant.LoggingConstants;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.rule.LoggingRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.AbstractQueryableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Show dist variables handler.
 */
public final class ShowDistVariablesHandler extends AbstractQueryableRALBackendHandler<ShowDistVariablesStatement> {
    
    private static final String VARIABLE_NAME = "variable_name";
    
    private static final String VARIABLE_VALUE = "variable_value";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(VARIABLE_NAME, VARIABLE_VALUE);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        ConfigurationProperties props = contextManager.getMetaDataContexts().getMetaData().getProps();
        Collection<LocalDataQueryResultRow> result = ConfigurationPropertyKey.getKeyNames().stream()
                .filter(each -> !"sql_show".equalsIgnoreCase(each) && !"sql_simple".equalsIgnoreCase(each))
                .map(each -> new LocalDataQueryResultRow(each.toLowerCase(), props.getValue(ConfigurationPropertyKey.valueOf(each)).toString()))
                .collect(Collectors.toList());
        result.add(new LocalDataQueryResultRow(
                VariableEnum.AGENT_PLUGINS_ENABLED.name().toLowerCase(), SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.TRUE.toString())));
        result.add(new LocalDataQueryResultRow(VariableEnum.CACHED_CONNECTIONS.name().toLowerCase(), getConnectionSession().getBackendConnection().getConnectionSize()));
        result.add(new LocalDataQueryResultRow(VariableEnum.TRANSACTION_TYPE.name().toLowerCase(), getConnectionSession().getTransactionStatus().getTransactionType().name()));
        Optional<ShardingSphereLogger> sqlLogger = getSQLLogger(contextManager);
        if (sqlLogger.isPresent()) {
            Properties sqlLoggerProps = sqlLogger.get().getProps();
            result.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SHOW_VARIABLE_NAME, sqlLoggerProps.getOrDefault(LoggingConstants.SQL_LOG_ENABLE, Boolean.FALSE).toString()));
            result.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SIMPLE_VARIABLE_NAME, sqlLoggerProps.getOrDefault(LoggingConstants.SQL_LOG_SIMPLE, Boolean.FALSE).toString()));
        } else {
            result.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SHOW_VARIABLE_NAME,
                    contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.valueOf(LoggingConstants.SQL_SHOW_VARIABLE_NAME)).toString()));
            result.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SIMPLE_VARIABLE_NAME,
                    contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.valueOf(LoggingConstants.SQL_SHOW_VARIABLE_NAME)).toString()));
        }
        return result;
    }
    
    private Optional<ShardingSphereLogger> getSQLLogger(final ContextManager contextManager) {
        return contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(LoggingRule.class).getConfiguration().getLoggers().stream()
                .filter(each -> LoggingConstants.SQL_LOG_TOPIC.equalsIgnoreCase(each.getLoggerName())).findFirst();
    }
}
