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
import org.apache.shardingsphere.distsql.statement.ral.queryable.ShowDistVariablesStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.logging.constant.LoggingConstants;
import org.apache.shardingsphere.logging.logger.ShardingSphereLogger;
import org.apache.shardingsphere.logging.util.LoggingUtils;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.DistSQLVariable;
import org.apache.shardingsphere.distsql.handler.type.ral.query.ConnectionSizeAwareQueryableRALExecutor;
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
@Setter
public final class ShowDistVariablesExecutor implements ConnectionSizeAwareQueryableRALExecutor<ShowDistVariablesStatement> {
    
    private int connectionSize;
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("variable_name", "variable_value");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowDistVariablesStatement sqlStatement, final ShardingSphereMetaData metaData) {
        Collection<LocalDataQueryResultRow> result = ConfigurationPropertyKey.getKeyNames().stream()
                .filter(each -> !ConfigurationPropertyKey.SQL_SHOW.name().equals(each) && !ConfigurationPropertyKey.SQL_SIMPLE.name().equals(each))
                .map(each -> new LocalDataQueryResultRow(each.toLowerCase(), getStringResult(metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(each))))).collect(Collectors.toList());
        result.addAll(TemporaryConfigurationPropertyKey.getKeyNames().stream()
                .map(each -> new LocalDataQueryResultRow(each.toLowerCase(), getStringResult(metaData.getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.valueOf(each)))))
                .collect(Collectors.toList()));
        result.add(new LocalDataQueryResultRow(DistSQLVariable.CACHED_CONNECTIONS.name().toLowerCase(), connectionSize));
        addLoggingPropsRows(metaData, result);
        if (sqlStatement.getLikePattern().isPresent()) {
            String pattern = SQLUtils.convertLikePatternToRegex(sqlStatement.getLikePattern().get());
            result = result.stream().filter(each -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher((String) each.getCell(1)).matches()).collect(Collectors.toList());
        }
        return result.stream().sorted(Comparator.comparing(each -> each.getCell(1).toString())).collect(Collectors.toList());
    }
    
    private String getStringResult(final Object value) {
        if (null == value) {
            return "";
        }
        return value instanceof TypedSPI ? ((TypedSPI) value).getType().toString() : value.toString();
    }
    
    private void addLoggingPropsRows(final ShardingSphereMetaData metaData, final Collection<LocalDataQueryResultRow> rows) {
        Optional<ShardingSphereLogger> sqlLogger = LoggingUtils.getSQLLogger(metaData.getGlobalRuleMetaData());
        if (sqlLogger.isPresent()) {
            Properties sqlLoggerProps = sqlLogger.get().getProps();
            rows.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SHOW_VARIABLE_NAME, sqlLoggerProps.getOrDefault(LoggingConstants.SQL_LOG_ENABLE, Boolean.FALSE).toString()));
            rows.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SIMPLE_VARIABLE_NAME, sqlLoggerProps.getOrDefault(LoggingConstants.SQL_LOG_SIMPLE, Boolean.FALSE).toString()));
        } else {
            rows.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SHOW_VARIABLE_NAME,
                    metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(LoggingConstants.SQL_SHOW_VARIABLE_NAME.toUpperCase())).toString()));
            rows.add(new LocalDataQueryResultRow(LoggingConstants.SQL_SIMPLE_VARIABLE_NAME,
                    metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(LoggingConstants.SQL_SIMPLE_VARIABLE_NAME.toUpperCase())).toString()));
        }
    }
    
    @Override
    public Class<ShowDistVariablesStatement> getType() {
        return ShowDistVariablesStatement.class;
    }
}
