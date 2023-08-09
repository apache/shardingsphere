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
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.logging.constant.LoggingConstants;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.util.LoggingUtils;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.executor.ConnectionSessionRequiredQueryableRALExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
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
        Collection<LocalDataQueryResultRow> result = ConfigurationPropertyKey.getKeyNames().stream().filter(each -> !"sql_show".equalsIgnoreCase(each) && !"sql_simple".equalsIgnoreCase(each)
                && null != metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(each)))
                .map(each -> new LocalDataQueryResultRow(each.toLowerCase(), metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(each)).toString())).collect(Collectors.toList());
        result.addAll(TemporaryConfigurationPropertyKey.getKeyNames().stream()
                .map(each -> new LocalDataQueryResultRow(each.toLowerCase(), metaData.getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.valueOf(each)).toString()))
                .collect(Collectors.toList()));
        result.add(new LocalDataQueryResultRow(VariableEnum.CACHED_CONNECTIONS.name().toLowerCase(), connectionSession.getDatabaseConnectionManager().getConnectionSize()));
        addLoggingPropsRows(metaData, result);
        if (sqlStatement.getLikePattern().isPresent()) {
            String pattern = SQLUtils.convertLikePatternToRegex(sqlStatement.getLikePattern().get());
            result = result.stream().filter(each -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher((String) each.getCell(1)).matches()).collect(Collectors.toList());
        }
        return result.stream().sorted(Comparator.comparing(each -> each.getCell(1).toString())).collect(Collectors.toList());
    }
    
    private void addLoggingPropsRows(final ShardingSphereMetaData metaData, final Collection<LocalDataQueryResultRow> result) {
        Optional<ShardingSphereLogger> sqlLogger = LoggingUtils.getSQLLogger(metaData.getGlobalRuleMetaData());
        if (sqlLogger.isPresent()) {
            Properties sqlLoggerProps = sqlLogger.get().getProps();
            result.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SHOW_VARIABLE_NAME, sqlLoggerProps.getOrDefault(LoggingConstants.SQL_LOG_ENABLE, Boolean.FALSE).toString()));
            result.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SIMPLE_VARIABLE_NAME, sqlLoggerProps.getOrDefault(LoggingConstants.SQL_LOG_SIMPLE, Boolean.FALSE).toString()));
        } else {
            result.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SHOW_VARIABLE_NAME,
                    metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(LoggingConstants.SQL_SHOW_VARIABLE_NAME.toUpperCase())).toString()));
            result.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SIMPLE_VARIABLE_NAME,
                    metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(LoggingConstants.SQL_SIMPLE_VARIABLE_NAME.toUpperCase())).toString()));
        }
    }
    
    @Override
    public String getType() {
        return ShowDistVariablesStatement.class.getName();
    }
}
