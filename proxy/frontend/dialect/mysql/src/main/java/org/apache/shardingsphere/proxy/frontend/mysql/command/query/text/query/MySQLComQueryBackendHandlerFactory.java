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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.admin.MySQLComSetOptionPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.ProxySQLComQueryParser;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.sqlmode.MySQLSessionSQLMode;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query.MySQLComQueryBinaryParameterExtractor.ExtractionResult;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.MultiSQLSplitter;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Backend handler factory for MySQL COM_QUERY.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MySQLComQueryBackendHandlerFactory {
    
    private static final String BINARY_INTRODUCER = "_binary";
    
    private static final char MALFORMED_INPUT_REPLACEMENT = (char) 0xFFFD;
    
    static ProxyBackendHandler newInstance(final MySQLComQueryPacket packet, final ConnectionSession connectionSession) throws SQLException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        String sql = packet.getSQL();
        List<Object> binaryLiteralValues = Collections.emptyList();
        SQLStatement sqlStatement;
        Optional<ExtractionResult> extractionResult = tryExtractBinaryLiteral(sql, packet, connectionSession);
        if (extractionResult.isPresent() && 1 == MultiSQLSplitter.split(extractionResult.get().getSql()).size()) {
            String parameterizedSQL = SQLHintUtils.removeHint(extractionResult.get().getSql());
            try {
                sqlStatement = ProxySQLComQueryParser.parse(parameterizedSQL, databaseType, connectionSession);
                sql = parameterizedSQL;
                binaryLiteralValues = extractionResult.get().getBinaryLiteralValues();
            } catch (final DialectSQLParsingException ignored) {
                sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
            }
        } else {
            sqlStatement = ProxySQLComQueryParser.parse(sql, databaseType, connectionSession);
        }
        return binaryLiteralValues.isEmpty() && areMultiStatements(connectionSession, sqlStatement, sql)
                ? new MySQLMultiStatementsProxyBackendHandler(connectionSession, sqlStatement, sql)
                : createSingleStatementHandler(databaseType, sql, sqlStatement, binaryLiteralValues, packet, connectionSession);
    }
    
    private static Optional<ExtractionResult> tryExtractBinaryLiteral(final String sql, final MySQLComQueryPacket packet, final ConnectionSession connectionSession) throws SQLException {
        Optional<byte[]> originalSQLBytes = packet.findOriginalSQLBytes();
        if (!originalSQLBytes.isPresent() || !requiresBinaryLiteralInspection(sql)) {
            return Optional.empty();
        }
        boolean noBackslashEscapes = MySQLSessionSQLMode.get(connectionSession.getAttributeMap()).isNoBackslashEscapes();
        ExtractionResult extractionResult = MySQLComQueryBinaryParameterExtractor.extract(
                originalSQLBytes.get(), connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get(), noBackslashEscapes);
        return extractionResult.getBinaryLiteralValues().isEmpty() ? Optional.empty() : Optional.of(extractionResult);
    }
    
    private static ProxyBackendHandler createSingleStatementHandler(final DatabaseType databaseType, final String sql, final SQLStatement sqlStatement,
                                                                    final List<Object> binaryLiteralValues, final MySQLComQueryPacket packet,
                                                                    final ConnectionSession connectionSession) throws SQLException {
        if (binaryLiteralValues.isEmpty()) {
            return ProxyBackendHandlerFactory.newInstance(databaseType, sql, sqlStatement, connectionSession, packet.getHintValueContext());
        }
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData();
        SQLStatementContext sqlStatementContext = new SQLBindEngine(metaData, connectionSession.getCurrentDatabaseName(), packet.getHintValueContext()).bind(sqlStatement);
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).bindParameters(binaryLiteralValues);
        }
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, binaryLiteralValues, packet.getHintValueContext(), connectionSession.getConnectionContext(), metaData, true);
        return ProxyBackendHandlerFactory.newInstance(databaseType, queryContext, connectionSession, true);
    }
    
    private static boolean requiresBinaryLiteralInspection(final String sql) {
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            if (MALFORMED_INPUT_REPLACEMENT == current || '_' == current && sql.regionMatches(true, i, BINARY_INTRODUCER, 0, BINARY_INTRODUCER.length())) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean areMultiStatements(final ConnectionSession connectionSession, final SQLStatement sqlStatement, final String sql) {
        return isMultiStatementsEnabled(connectionSession)
                && isSuitableMultiStatementsSQLStatement(sqlStatement) && MultiSQLSplitter.hasSameTypeMultiStatements(sqlStatement, MultiSQLSplitter.split(sql));
    }
    
    private static boolean isMultiStatementsEnabled(final ConnectionSession connectionSession) {
        return connectionSession.getAttributeMap().hasAttr(MySQLConstants.OPTION_MULTI_STATEMENTS_ATTRIBUTE_KEY)
                && MySQLComSetOptionPacket.MYSQL_OPTION_MULTI_STATEMENTS_ON == connectionSession.getAttributeMap().attr(MySQLConstants.OPTION_MULTI_STATEMENTS_ATTRIBUTE_KEY).get();
    }
    
    private static boolean isSuitableMultiStatementsSQLStatement(final SQLStatement sqlStatement) {
        return containsInsertOnDuplicateKey(sqlStatement) || sqlStatement instanceof UpdateStatement || sqlStatement instanceof DeleteStatement;
    }
    
    private static boolean containsInsertOnDuplicateKey(final SQLStatement sqlStatement) {
        return sqlStatement instanceof InsertStatement && ((InsertStatement) sqlStatement).getOnDuplicateKeyColumns().isPresent();
    }
}
