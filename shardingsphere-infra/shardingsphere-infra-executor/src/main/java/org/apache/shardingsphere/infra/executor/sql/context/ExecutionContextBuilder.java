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

package org.apache.shardingsphere.infra.executor.sql.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Execution context builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutionContextBuilder {
    
    /**
     * Build execution contexts.
     * 
     * @param metaData meta data
     * @param sqlRewriteResult SQL rewrite result
     * @param sqlStatementContext SQL statement context
     * @return execution contexts
     */
    public static Collection<ExecutionUnit> build(final ShardingSphereMetaData metaData, final SQLRewriteResult sqlRewriteResult, final SQLStatementContext<?> sqlStatementContext) {
        return sqlRewriteResult instanceof GenericSQLRewriteResult ? build(metaData, (GenericSQLRewriteResult) sqlRewriteResult, sqlStatementContext)
                : build(metaData, (RouteSQLRewriteResult) sqlRewriteResult);
    }
    
    private static Collection<ExecutionUnit> build(final ShardingSphereMetaData metaData, final GenericSQLRewriteResult sqlRewriteResult, final SQLStatementContext<?> sqlStatementContext) {
        String dataSourceName = metaData.getDataSourceMetaDatas().getAllInstanceDataSourceNames().iterator().next();
        return Collections.singletonList(new ExecutionUnit(dataSourceName,
                new SQLUnit(sqlRewriteResult.getSqlRewriteUnit().getSql(), sqlRewriteResult.getSqlRewriteUnit().getParameters(), getSQLRuntimeContext(metaData, sqlStatementContext))));
    }
    
    private static Collection<ExecutionUnit> build(final ShardingSphereMetaData metaData, final RouteSQLRewriteResult sqlRewriteResult) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        for (Entry<RouteUnit, SQLRewriteUnit> entry : sqlRewriteResult.getSqlRewriteUnits().entrySet()) {
            Collection<RouteMapper> tableMappers = entry.getKey().getTableMappers();
            result.add(new ExecutionUnit(entry.getKey().getDataSourceMapper().getActualName(),
                    new SQLUnit(entry.getValue().getSql(), entry.getValue().getParameters(), getSQLRuntimeContext(metaData, tableMappers))));
        }
        return result;
    }
    
    private static SQLRuntimeContext getSQLRuntimeContext(final ShardingSphereMetaData metaData, final SQLStatementContext<?> sqlStatementContext) {
        return new SQLRuntimeContext(getLogicTableNames(sqlStatementContext), getActualTableNames(sqlStatementContext), getPrimaryKeyColumns(metaData, sqlStatementContext));
    }
    
    private static SQLRuntimeContext getSQLRuntimeContext(final ShardingSphereMetaData metaData, final Collection<RouteMapper> tableMappers) {
        return new SQLRuntimeContext(getLogicTableNames(tableMappers), getActualTableNames(tableMappers), getPrimaryKeyColumns(metaData, tableMappers));
    }
    
    private static List<String> getLogicTableNames(final SQLStatementContext<?> sqlStatementContext) {
        return getGenericTableNames(sqlStatementContext);
    }
    
    private static List<String> getLogicTableNames(final Collection<RouteMapper> tableMappers) {
        return null == tableMappers ? Collections.emptyList() : tableMappers.stream().map(RouteMapper::getLogicName).collect(Collectors.toList());
    }
    
    private static List<String> getActualTableNames(final SQLStatementContext<?> sqlStatementContext) {
        return getGenericTableNames(sqlStatementContext);
    }
    
    private static List<String> getActualTableNames(final Collection<RouteMapper> tableMappers) {
        if (null == tableMappers) {
            return Collections.emptyList();
        }
        return tableMappers.stream().map(RouteMapper::getActualName).collect(Collectors.toList());
    }
    
    private static List<String> getGenericTableNames(final SQLStatementContext<?> sqlStatementContext) {
        TablesContext tablesContext = null;
        if (null != sqlStatementContext) {
            tablesContext = sqlStatementContext.getTablesContext();
        }
        return null == tablesContext ? Collections.emptyList() : new ArrayList<>(tablesContext.getTableNames());
    }
    
    private static List<PrimaryKeyMetaData> getPrimaryKeyColumns(final ShardingSphereMetaData metaData, final SQLStatementContext<?> sqlStatementContext) {
        return getPrimaryKeyColumns(metaData, getActualTableNames(sqlStatementContext));
    }
    
    private static List<PrimaryKeyMetaData> getPrimaryKeyColumns(final ShardingSphereMetaData metaData, final Collection<RouteMapper> tableMappers) {
        return getPrimaryKeyColumns(metaData, getLogicTableNames(tableMappers));
    }
    
    private static List<PrimaryKeyMetaData> getPrimaryKeyColumns(final ShardingSphereMetaData metaData, final List<String> actualTableNames) {
        List<PrimaryKeyMetaData> result = new LinkedList<>();
        for (String each: actualTableNames) {
            TableMetaData tableMetaData = metaData.getRuleSchemaMetaData().getSchemaMetaData().get(each);
            if (null != tableMetaData) {
                result.add(new PrimaryKeyMetaData(each, tableMetaData.getPrimaryKeyColumns()));
            }
        }
        return result;
    }
}
