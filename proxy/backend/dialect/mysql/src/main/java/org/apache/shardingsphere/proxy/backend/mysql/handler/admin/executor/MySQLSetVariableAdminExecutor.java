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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownSystemVariableException;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminUpdateExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.charset.CharsetSetExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.session.SessionVariableRecordExecutor;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariable;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariableScope;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Set variable admin executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLSetVariableAdminExecutor implements DatabaseAdminUpdateExecutor {
    
    private final SetStatement sqlStatement;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) throws SQLException {
        Map<String, String> sessionVariables = extractSessionVariables();
        validateSessionVariables(sessionVariables.keySet());
        CharsetSetExecutor charsetSetExecutor = new CharsetSetExecutor(sqlStatement.getDatabaseType(), connectionSession);
        sessionVariables.forEach(charsetSetExecutor::set);
        new SessionVariableRecordExecutor(sqlStatement.getDatabaseType(), connectionSession).recordVariable(sessionVariables);
        executeSetGlobalVariablesIfPresent(connectionSession, metaData);
    }
    
    private Map<String, String> extractSessionVariables() {
        return sqlStatement.getVariableAssigns().stream().filter(each -> !"global".equalsIgnoreCase(each.getVariable().getScope().orElse("")))
                .collect(Collectors.toMap(each -> each.getVariable().getVariable(), VariableAssignSegment::getAssignValue));
    }
    
    private void validateSessionVariables(final Collection<String> sessionVariables) {
        for (String each : sessionVariables) {
            MySQLSystemVariable systemVariable = MySQLSystemVariable.findSystemVariable(each).orElseThrow(() -> new UnknownSystemVariableException(each));
            systemVariable.validateSetTargetScope(MySQLSystemVariableScope.SESSION);
        }
    }
    
    private void executeSetGlobalVariablesIfPresent(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) throws SQLException {
        if (null == connectionSession.getUsedDatabaseName()) {
            return;
        }
        String concatenatedGlobalVariables = extractGlobalVariables().entrySet().stream().map(entry -> String.format("@@GLOBAL.%s = %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
        if (concatenatedGlobalVariables.isEmpty()) {
            return;
        }
        String sql = "SET " + concatenatedGlobalVariables;
        SQLParserRule sqlParserRule = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(this.sqlStatement.getDatabaseType()).parse(sql, false);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaData,
                connectionSession.getCurrentDatabaseName(), new HintValueContext()).bind(sqlStatement);
        DatabaseProxyBackendHandler databaseProxyBackendHandler = DatabaseProxyConnectorFactory.newInstance(
                new QueryContext(sqlStatementContext, sql, Collections.emptyList(), new HintValueContext(), connectionSession.getConnectionContext(), metaData),
                connectionSession.getDatabaseConnectionManager(), false);
        try {
            databaseProxyBackendHandler.execute();
        } finally {
            databaseProxyBackendHandler.close();
        }
    }
    
    private Map<String, String> extractGlobalVariables() {
        return sqlStatement.getVariableAssigns().stream().filter(each -> "global".equalsIgnoreCase(each.getVariable().getScope().orElse("")))
                .collect(Collectors.toMap(each -> each.getVariable().getVariable(), VariableAssignSegment::getAssignValue, (oldValue, newValue) -> newValue, LinkedHashMap::new));
    }
}
