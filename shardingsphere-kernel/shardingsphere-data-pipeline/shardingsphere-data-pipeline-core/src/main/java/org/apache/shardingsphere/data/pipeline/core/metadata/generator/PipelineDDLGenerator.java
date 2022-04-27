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

package org.apache.shardingsphere.data.pipeline.core.metadata.generator;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Pipeline ddl generator.
 */
@RequiredArgsConstructor
public final class PipelineDDLGenerator {
    
    private final ContextManager contextManager;
    
    /**
     * Replace table name with prefix.
     *
     * @param sql sql
     * @param prefix prefix
     * @param databaseType database type
     * @param databaseName database name
     * @return replaced sql
     */
    public String replaceTableNameWithPrefix(final String sql, final String prefix, final DatabaseType databaseType, final String databaseName) {
        LogicSQL logicSQL = getLogicSQL(sql, databaseType, databaseName);
        RouteContext routeContext = getRouteContext(prefix, logicSQL);
        return getRewriteSQL(sql, databaseName, logicSQL, routeContext);
    }
    
    private String getRewriteSQL(final String sql, final String databaseName, final LogicSQL logicSQL, final RouteContext routeContext) {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData(databaseName);
        SQLRewriteEntry rewriteEntry = new SQLRewriteEntry(metaData.getDatabaseName(), metaData.getSchemas(), contextManager.getMetaDataContexts().getProps(), metaData.getRuleMetaData().getRules());
        SQLRewriteResult sqlRewriteResult = rewriteEntry.rewrite(logicSQL.getSql(), logicSQL.getParameters(), logicSQL.getSqlStatementContext(), routeContext);
        if (sqlRewriteResult instanceof RouteSQLRewriteResult) {
            return ((RouteSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnits().get(routeContext.getRouteUnits().iterator().next()).getSql();
        }
        return sql;
    }
    
    private RouteContext getRouteContext(final String prefix, final LogicSQL logicSQL) {
        Collection<String> tableNames = logicSQL.getSqlStatementContext().getTablesContext().getTableNames();
        RouteContext result = new RouteContext();
        Collection<RouteMapper> tableMappers = new LinkedList<>();
        for (String each : tableNames) {
            tableMappers.add(new RouteMapper(each, prefix + each));
        }
        RouteUnit routeUnit = new RouteUnit(new RouteMapper("", ""), tableMappers);
        result.getRouteUnits().add(routeUnit);
        return result;
    }
    
    private LogicSQL getLogicSQL(final String sql, final DatabaseType databaseType, final String databaseName) {
        Optional<SQLParserRule> sqlParserRule = contextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(SQLParserRule.class);
        Preconditions.checkState(sqlParserRule.isPresent());
        SQLStatement sqlStatement = new ShardingSphereSQLParserEngine(databaseType.getName(), sqlParserRule.get().toParserConfiguration()).parse(sql, false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(contextManager.getMetaDataContexts().getMetaDataMap(),
                sqlStatement, databaseName);
        return new LogicSQL(sqlStatementContext, sql, Collections.emptyList());
    }
}
