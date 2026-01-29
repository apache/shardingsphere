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

package org.apache.shardingsphere.distsql.handler.ral.queryable.variable;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorConnectionContextAware;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowDistVariablesStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.util.regex.RegexUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Show dist variables executor.
 */
@Setter
public final class ShowDistVariablesExecutor implements DistSQLQueryExecutor<ShowDistVariablesStatement>, DistSQLExecutorConnectionContextAware {
    
    private DistSQLConnectionContext connectionContext;
    
    @Override
    public Collection<String> getColumnNames(final ShowDistVariablesStatement sqlStatement) {
        return Arrays.asList("variable_name", "variable_value");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowDistVariablesStatement sqlStatement, final ContextManager contextManager) {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        if (sqlStatement.isShowTemporary()) {
            result.addAll(TemporaryConfigurationPropertyKey.getKeyNames().stream()
                    .map(each -> new LocalDataQueryResultRow(each.toLowerCase(), getStringResult(metaData.getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.valueOf(each)))))
                    .collect(Collectors.toList()));
        } else {
            result.addAll(ConfigurationPropertyKey.getKeyNames().stream()
                    .map(each -> new LocalDataQueryResultRow(each.toLowerCase(), getStringResult(metaData.getProps().getValue(ConfigurationPropertyKey.valueOf(each))))).collect(Collectors.toList()));
            result.add(new LocalDataQueryResultRow(DistSQLVariable.CACHED_CONNECTIONS.name().toLowerCase(), connectionContext.getConnectionSize()));
        }
        if (sqlStatement.getLikePattern().isPresent()) {
            String pattern = RegexUtils.convertLikePatternToRegex(sqlStatement.getLikePattern().get());
            result = result.stream().filter(each -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher((String) each.getCell(1)).matches()).collect(Collectors.toList());
        }
        return result.stream().sorted(Comparator.comparing(each -> each.getCell(1).toString())).collect(Collectors.toList());
    }
    
    private String getStringResult(final Object value) {
        if (null == value) {
            return "";
        }
        if (value instanceof Float || value instanceof Double) {
            return new BigDecimal(String.valueOf(value)).toPlainString();
        }
        return value instanceof TypedSPI ? ((TypedSPI) value).getType().toString() : value.toString();
    }
    
    @Override
    public Class<ShowDistVariablesStatement> getType() {
        return ShowDistVariablesStatement.class;
    }
}
