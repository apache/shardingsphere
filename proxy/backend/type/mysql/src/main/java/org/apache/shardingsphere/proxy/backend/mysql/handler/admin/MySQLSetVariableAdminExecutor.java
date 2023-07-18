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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dialect.mysql.exception.UnknownSystemVariableException;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnectorFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariable;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.Scope;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Set variable admin executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLSetVariableAdminExecutor implements DatabaseAdminExecutor {
    
    private final SetStatement setStatement;
    
    @Override
    public void execute(final ConnectionSession connectionSession) throws SQLException {
        Map<String, String> sessionVariables = extractSessionVariables();
        validateSessionVariables(sessionVariables.keySet());
        Map<String, MySQLSessionVariableHandler> handlers = sessionVariables.keySet().stream()
                .collect(Collectors.toMap(Function.identity(), value -> TypedSPILoader.getService(MySQLSessionVariableHandler.class, value)));
        for (Entry<String, MySQLSessionVariableHandler> entry : handlers.entrySet()) {
            entry.getValue().handle(connectionSession, entry.getKey(), sessionVariables.get(entry.getKey()));
        }
        executeSetGlobalVariablesIfPresent(connectionSession);
    }
    
    private Map<String, String> extractSessionVariables() {
        return setStatement.getVariableAssigns().stream().filter(each -> !"global".equalsIgnoreCase(each.getVariable().getScope().orElse("")))
                .collect(Collectors.toMap(each -> each.getVariable().getVariable(), VariableAssignSegment::getAssignValue));
    }
    
    private void validateSessionVariables(final Collection<String> sessionVariables) {
        for (String each : sessionVariables) {
            MySQLSystemVariable systemVariable = MySQLSystemVariable.findSystemVariable(each).orElseThrow(() -> new UnknownSystemVariableException(each));
            systemVariable.validateSetTargetScope(Scope.SESSION);
        }
    }
    
    private void executeSetGlobalVariablesIfPresent(final ConnectionSession connectionSession) throws SQLException {
        if (null == connectionSession.getDatabaseName()) {
            return;
        }
        String concatenatedGlobalVariables = extractGlobalVariables().entrySet().stream().map(entry -> String.format("@@GLOBAL.%s = %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
        if (concatenatedGlobalVariables.isEmpty()) {
            return;
        }
        String sql = "SET " + concatenatedGlobalVariables;
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(TypedSPILoader.getService(DatabaseType.class, "MySQL").getType()).parse(sql, false);
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(),
                sqlStatement, connectionSession.getDefaultDatabaseName());
        DatabaseBackendHandler databaseBackendHandler = DatabaseConnectorFactory.getInstance()
                .newInstance(new QueryContext(sqlStatementContext, sql, Collections.emptyList()), connectionSession.getDatabaseConnectionManager(), false);
        try {
            databaseBackendHandler.execute();
        } finally {
            databaseBackendHandler.close();
        }
    }
    
    private Map<String, String> extractGlobalVariables() {
        return setStatement.getVariableAssigns().stream().filter(each -> "global".equalsIgnoreCase(each.getVariable().getScope().orElse("")))
                .collect(Collectors.toMap(each -> each.getVariable().getVariable(), VariableAssignSegment::getAssignValue, (oldValue, newValue) -> newValue, LinkedHashMap::new));
    }
}
