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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.exception.core.exception.data.InvalidParameterValueException;
import org.apache.shardingsphere.database.exception.mysql.exception.CollationCharsetMismatchException;
import org.apache.shardingsphere.database.exception.mysql.exception.UnknownSystemVariableException;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminUpdateExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.session.SessionVariableRecordExecutor;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariable;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariableScope;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.UnicastResourceShowExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.charset.MySQLSessionCharsetContext;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.sqlmode.MySQLSessionSQLMode;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment.VariableType;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Set variable admin executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLSetVariableAdminExecutor implements DatabaseAdminUpdateExecutor {
    
    private static final String CHARACTER_SET_CLIENT = "character_set_client";
    
    private static final String CHARACTER_SET_RESULTS = "character_set_results";
    
    private static final String CHARACTER_SET_CONNECTION = "character_set_connection";
    
    private static final String COLLATION_CONNECTION = "collation_connection";
    
    private static final String SQL_MODE = "sql_mode";
    
    private static final Collection<String> IMPERMISSIBLE_CLIENT_CHARACTER_SETS = Arrays.asList("ucs2", "utf16", "utf16le", "utf32");
    
    private final SetStatement sqlStatement;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) throws SQLException {
        List<VariableAssignSegment> sessionVariableAssigns = extractSessionVariableAssigns();
        validateSessionVariables(sessionVariableAssigns.stream().map(VariableAssignSegment::getVariable).collect(Collectors.toList()));
        boolean setNamesWithCollation = isSetNamesWithCollation(sessionVariableAssigns);
        MySQLSessionCharsetContext charsetContext = setNamesWithCollation ? MySQLSessionCharsetContext.create(MySQLConstants.DEFAULT_CHARSET) : null;
        String sqlMode = null;
        Map<String, String> replayedSessionVariables = new LinkedHashMap<>();
        String connectionCollationReplayValue = null;
        for (int i = 0; i < sessionVariableAssigns.size(); i++) {
            VariableAssignSegment each = sessionVariableAssigns.get(i);
            String variableName = each.getVariable().getVariable();
            if (SQL_MODE.equalsIgnoreCase(variableName)) {
                sqlMode = parseSQLMode(each.getAssignValue(), connectionSession, metaData);
                replayedSessionVariables.put(SQL_MODE, MySQLSessionSQLMode.formatAssignValue(sqlMode));
                continue;
            }
            if (!isCharsetVariable(variableName)) {
                replayedSessionVariables.put(variableName, each.getAssignValue());
                continue;
            }
            if (null == charsetContext) {
                charsetContext = MySQLSessionCharsetContext.get(connectionSession.getAttributeMap());
            }
            charsetContext = updateCharsetContext(charsetContext, each, setNamesWithCollation && i < 4);
            if (CHARACTER_SET_CONNECTION.equalsIgnoreCase(variableName) || COLLATION_CONNECTION.equalsIgnoreCase(variableName)) {
                connectionCollationReplayValue = String.format("'%s'", charsetContext.getConnectionCollationName());
            }
        }
        if (null != charsetContext) {
            charsetContext.apply(connectionSession.getAttributeMap());
        }
        if (null != sqlMode) {
            MySQLSessionSQLMode.set(sqlMode, connectionSession.getAttributeMap());
        }
        SessionVariableRecordExecutor sessionVariableRecordExecutor = new SessionVariableRecordExecutor(sqlStatement.getDatabaseType(), connectionSession);
        sessionVariableRecordExecutor.recordVariable(replayedSessionVariables);
        if (null != connectionCollationReplayValue) {
            sessionVariableRecordExecutor.recordVariable(COLLATION_CONNECTION, connectionCollationReplayValue);
        }
        executeSetGlobalVariablesIfPresent(connectionSession, metaData);
    }
    
    private String parseSQLMode(final String assignValue, final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) throws SQLException {
        if (isKeyword(assignValue, "default")) {
            return MySQLSessionSQLMode.getGlobalValue();
        }
        String value = QuoteCharacter.SINGLE_QUOTE.isWrapped(assignValue)
                ? assignValue.substring(1, assignValue.length() - 1)
                : evaluateSQLModeExpression(assignValue, connectionSession, metaData);
        return Arrays.stream(value.split(",", -1)).map(String::trim).map(each -> each.toUpperCase(Locale.ROOT)).collect(Collectors.joining(","));
    }
    
    private String evaluateSQLModeExpression(final String expression, final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) throws SQLException {
        String sql = "SELECT " + expression;
        SQLParserRule sqlParserRule = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SelectStatement selectStatement = (SelectStatement) sqlParserRule.getSQLParserEngine(sqlStatement.getDatabaseType()).parse(sql, false);
        UnicastResourceShowExecutor executor = new UnicastResourceShowExecutor(selectStatement, sql);
        executor.execute(connectionSession, metaData);
        ShardingSpherePreconditions.checkState(executor.getMergedResult().next(), () -> new InvalidParameterValueException(SQL_MODE, expression));
        Object result = executor.getMergedResult().getValue(1, String.class);
        ShardingSpherePreconditions.checkNotNull(result, () -> new InvalidParameterValueException(SQL_MODE, expression));
        return result.toString();
    }
    
    private List<VariableAssignSegment> extractSessionVariableAssigns() {
        return sqlStatement.getVariableAssigns().stream().filter(each -> !"global".equalsIgnoreCase(each.getVariable().getScope().orElse(""))).collect(Collectors.toList());
    }
    
    private void validateSessionVariables(final Collection<VariableSegment> variables) {
        for (VariableSegment each : variables) {
            if (VariableType.USER == each.getVariableType()) {
                continue;
            }
            MySQLSystemVariable systemVariable = MySQLSystemVariable.findSystemVariable(each.getVariable()).orElseThrow(() -> new UnknownSystemVariableException(each.getVariable()));
            systemVariable.validateSetTargetScope(MySQLSystemVariableScope.SESSION);
        }
    }
    
    private boolean isSetNamesWithCollation(final List<VariableAssignSegment> variableAssigns) {
        if (variableAssigns.size() < 4) {
            return false;
        }
        VariableAssignSegment first = variableAssigns.get(0);
        return CHARACTER_SET_CLIENT.equalsIgnoreCase(first.getVariable().getVariable())
                && CHARACTER_SET_RESULTS.equalsIgnoreCase(variableAssigns.get(1).getVariable().getVariable())
                && CHARACTER_SET_CONNECTION.equalsIgnoreCase(variableAssigns.get(2).getVariable().getVariable())
                && COLLATION_CONNECTION.equalsIgnoreCase(variableAssigns.get(3).getVariable().getVariable())
                && variableAssigns.subList(1, 4).stream().allMatch(each -> first.getStartIndex() == each.getStartIndex() && first.getStopIndex() == each.getStopIndex());
    }
    
    private MySQLSessionCharsetContext updateCharsetContext(final MySQLSessionCharsetContext context, final VariableAssignSegment variableAssign,
                                                            final boolean setNamesWithCollationAssignment) {
        String variableName = variableAssign.getVariable().getVariable().toLowerCase(Locale.ROOT);
        switch (variableName) {
            case CHARACTER_SET_CLIENT:
                return context.withClientCharacterSet(setNamesWithCollationAssignment
                        ? parseCharacterSet(variableAssign.getAssignValue())
                        : parseClientCharacterSet(variableAssign.getAssignValue()));
            case CHARACTER_SET_RESULTS:
                return updateResultCharacterSet(context, variableAssign.getAssignValue());
            case CHARACTER_SET_CONNECTION:
                return context.withConnectionCollation(parseConnectionCharacterSet(variableAssign.getAssignValue()));
            case COLLATION_CONNECTION:
                MySQLCharacterSets collation = parseCollation(variableAssign.getAssignValue());
                if (setNamesWithCollationAssignment && !context.getConnectionCharacterSetName().equals(collation.getCharacterSetName())) {
                    throw new CollationCharsetMismatchException(collation.getCollationName(), context.getConnectionCharacterSetName());
                }
                if (setNamesWithCollationAssignment) {
                    validateClientCharacterSet(context.getClientCharacterSetName());
                }
                return context.withConnectionCollation(collation);
            default:
                return context;
        }
    }
    
    private MySQLCharacterSets parseClientCharacterSet(final String value) {
        String normalizedValue = formatVariableValue(value).toLowerCase(Locale.ROOT);
        validateClientCharacterSet(normalizedValue);
        return parseCharacterSet(value);
    }
    
    private void validateClientCharacterSet(final String characterSet) {
        if (IMPERMISSIBLE_CLIENT_CHARACTER_SETS.contains(characterSet)) {
            throw new InvalidParameterValueException(CHARACTER_SET_CLIENT, characterSet);
        }
    }
    
    private MySQLSessionCharsetContext updateResultCharacterSet(final MySQLSessionCharsetContext context, final String value) {
        String normalizedValue = formatVariableValue(value).toLowerCase(Locale.ROOT);
        if (isKeyword(value, "null")) {
            return context.withoutResultConversion();
        }
        return "binary".equals(normalizedValue) ? context.withBinaryResult() : context.withResultCharacterSet(parseCharacterSet(value));
    }
    
    private MySQLCharacterSets parseCharacterSet(final String value) {
        String normalizedValue = formatVariableValue(value);
        return isKeyword(value, "default") ? MySQLConstants.DEFAULT_CHARSET : MySQLCharacterSets.findByCharacterSetName(normalizedValue);
    }
    
    private MySQLCharacterSets parseConnectionCharacterSet(final String value) {
        if (isKeyword(value, "default")) {
            return parseCollation(value);
        }
        MySQLCharacterSets result = parseCharacterSet(value);
        return "utf8mb4".equals(result.getCharacterSetName())
                ? MySQLCharacterSets.findByCollationName(MySQLSystemVariable.DEFAULT_COLLATION_FOR_UTF8MB4.getDefaultValue())
                : result;
    }
    
    private MySQLCharacterSets parseCollation(final String value) {
        String normalizedValue = formatVariableValue(value);
        if (isKeyword(value, "@@collation_database")) {
            return MySQLCharacterSets.findByCollationName(MySQLSystemVariable.COLLATION_DATABASE.getDefaultValue());
        }
        return isKeyword(value, "default")
                ? MySQLCharacterSets.findByCollationName(MySQLSystemVariable.COLLATION_CONNECTION.getDefaultValue())
                : MySQLCharacterSets.findByCollationName(normalizedValue);
    }
    
    private boolean isKeyword(final String value, final String keyword) {
        return keyword.equalsIgnoreCase(value.trim());
    }
    
    private String formatVariableValue(final String value) {
        return QuoteCharacter.SINGLE_QUOTE.isWrapped(value) || QuoteCharacter.QUOTE.isWrapped(value) ? value.substring(1, value.length() - 1) : value.trim();
    }
    
    private boolean isCharsetVariable(final String variableName) {
        return CHARACTER_SET_CLIENT.equalsIgnoreCase(variableName) || CHARACTER_SET_RESULTS.equalsIgnoreCase(variableName)
                || CHARACTER_SET_CONNECTION.equalsIgnoreCase(variableName) || COLLATION_CONNECTION.equalsIgnoreCase(variableName);
    }
    
    private void executeSetGlobalVariablesIfPresent(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) throws SQLException {
        if (null == connectionSession.getUsedDatabaseName()) {
            return;
        }
        Map<String, String> globalVariables = extractGlobalVariables();
        String concatenatedGlobalVariables = globalVariables.entrySet().stream().map(entry -> String.format("@@GLOBAL.%s = %s", entry.getKey(), entry.getValue()))
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
        if (globalVariables.keySet().stream().anyMatch(SQL_MODE::equalsIgnoreCase)) {
            MySQLSessionSQLMode.setGlobalValue(parseSQLMode("@@global.sql_mode", connectionSession, metaData));
        }
    }
    
    private Map<String, String> extractGlobalVariables() {
        return sqlStatement.getVariableAssigns().stream().filter(each -> "global".equalsIgnoreCase(each.getVariable().getScope().orElse("")))
                .collect(Collectors.toMap(each -> each.getVariable().getVariable(), VariableAssignSegment::getAssignValue, (oldValue, newValue) -> newValue, LinkedHashMap::new));
    }
}
