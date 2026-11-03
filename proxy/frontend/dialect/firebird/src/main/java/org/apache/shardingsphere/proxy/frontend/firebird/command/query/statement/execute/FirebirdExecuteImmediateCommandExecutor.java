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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidTransactionHandleException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdErrorPacketFactory;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteImmediatePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.ProxySQLComQueryParser;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdCancellationRegistry;
import org.apache.shardingsphere.proxy.frontend.firebird.command.admin.FirebirdCancellationRegistry.Scope;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionSessionRegistry;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionAccessType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Firebird execute immediate command executor.
 */
@RequiredArgsConstructor
public final class FirebirdExecuteImmediateCommandExecutor implements CommandExecutor {
    
    private static final Pattern SAVEPOINT_COMMAND = Pattern.compile("(?is)^(?:SAVEPOINT\\b|RELEASE\\s+SAVEPOINT\\b|ROLLBACK\\s+(?:WORK\\s+)?TO\\b)");
    
    private final FirebirdExecuteImmediatePacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    @HighFrequencyInvocation
    public Collection<DatabasePacket> execute() throws SQLException {
        if (SAVEPOINT_COMMAND.matcher(SQLUtils.trimComment(packet.getSQL())).find()) {
            return Collections.singleton(FirebirdGenericResponsePacket.getPacket());
        }
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
        SQLStatement sqlStatement;
        try {
            sqlStatement = ProxySQLComQueryParser.parse(packet.getSQL(), databaseType, connectionSession);
        } catch (final SQLParsingException | DialectSQLParsingException ex) {
            int transactionId = packet.getTransactionId();
            boolean active = FirebirdTransactionIdGenerator.getInstance().isTransactionActive(connectionSession.getConnectionId(), transactionId);
            return Collections.singleton(((FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(ex)).setHandle(active ? transactionId : 0));
        }
        int connectionId = connectionSession.getConnectionId();
        int transactionId = packet.getTransactionId();
        FirebirdTransactionIdGenerator transactions = FirebirdTransactionIdGenerator.getInstance();
        if (sqlStatement instanceof SetTransactionStatement) {
            SetTransactionStatement setTransaction = (SetTransactionStatement) sqlStatement;
            FirebirdTransactionOptions options = setTransaction.getAttributes().findAttribute(FirebirdTransactionOptions.class).orElseGet(FirebirdTransactionOptions::new);
            Optional<FirebirdStatusVector> reservationError = getReservationError(options);
            if (reservationError.isPresent()) {
                return Collections.singleton(FirebirdGenericResponsePacket.getPacket().setStatusVector(reservationError.get())
                        .setHandle(transactions.isTransactionActive(connectionId, transactionId) ? transactionId : 0));
            }
            if (null != options.getInvalidSyntaxToken()) {
                return Collections.singleton(syntaxErrorResponse(options.getInvalidSyntaxToken(), options.getInvalidSyntaxLine(), options.getInvalidSyntaxColumn(),
                        transactions.isTransactionActive(connectionId, transactionId) ? transactionId : 0));
            }
            if (null != options.getInvalidLockTimeoutToken()) {
                return Collections.singleton(syntaxErrorResponse(options.getInvalidLockTimeoutToken(), options.getInvalidLockTimeoutLine(), options.getInvalidLockTimeoutColumn(),
                        transactions.isTransactionActive(connectionId, transactionId) ? transactionId : 0));
            }
            if (null != options.getInvalidLockTimeoutLine()) {
                return Collections.singleton(FirebirdGenericResponsePacket.getPacket().setStatusVector(FirebirdStatusVector.fromSegments(Arrays.asList(
                        new FirebirdStatusVector.Segment(ISCConstants.isc_dsql_error, Collections.emptyList(), null),
                        FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_sqlerr, Collections.singletonList(FirebirdStatusVector.Argument.ofNumber(-842)), null),
                        new FirebirdStatusVector.Segment(ISCConstants.isc_expec_short, Collections.emptyList(), null),
                        FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_dsql_line_col_error,
                                Arrays.asList(FirebirdStatusVector.Argument.ofNumber(options.getInvalidLockTimeoutLine()),
                                        FirebirdStatusVector.Argument.ofNumber(options.getInvalidLockTimeoutColumn())),
                                null))))
                        .setHandle(transactions.isTransactionActive(connectionId, transactionId) ? transactionId : 0));
            }
            if (null != options.getDuplicateClause()) {
                return Collections.singleton(FirebirdGenericResponsePacket.getPacket().setStatusVector(FirebirdStatusVector.fromSegments(Arrays.asList(
                        FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_sqlerr, Collections.singletonList(FirebirdStatusVector.Argument.ofNumber(-637)), null),
                        FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_dsql_duplicate_spec,
                                Collections.singletonList(FirebirdStatusVector.Argument.ofString(options.getDuplicateClause())), null))))
                        .setHandle(transactions.isTransactionActive(connectionId, transactionId) ? transactionId : 0));
            }
            if (0 != transactionId) {
                return Collections.singleton(invalidTransactionResponse(transactionId, transactions.isTransactionActive(connectionId, transactionId)));
            }
            Optional<DatabasePacket> snapshotError = getSnapshotError(options);
            if (snapshotError.isPresent()) {
                return Collections.singleton(snapshotError.get());
            }
            int newTransactionId = transactions.nextTransactionId(connectionId);
            ConnectionSession transactionSession = FirebirdTransactionSessionRegistry.getInstance().register(connectionSession, newTransactionId);
            transactionSession.setAutoCommit(false);
            transactionSession.setReadOnly(setTransaction.isDesiredAccessMode(TransactionAccessType.READ_ONLY));
            transactionSession.setIsolationLevel(setTransaction.getIsolationLevel().orElse(TransactionIsolationLevel.REPEATABLE_READ));
            try (Scope ignored = FirebirdCancellationRegistry.getInstance().enter(connectionId, transactionSession.getDatabaseConnectionManager())) {
                startTransaction(transactionSession, setTransaction);
            } catch (final SQLException | IllegalArgumentException ex) {
                transactions.closeTransaction(connectionId, newTransactionId);
                unregisterOnFailure(newTransactionId, ex);
                Optional<FirebirdStatusVector> status = ex instanceof SQLException
                        ? FirebirdTransactionOptionsHandler.resolveBackendFailure((SQLException) ex, options.getSnapshotNumbers())
                        : Optional.empty();
                if (status.isPresent()) {
                    return Collections.singleton(FirebirdGenericResponsePacket.getPacket().setStatusVector(status.get()));
                }
                if (ex instanceof SQLException && transactionSession != connectionSession) {
                    return Collections.singleton(FirebirdErrorPacketFactory.newInstance(ex));
                }
                throw ex;
            }
            return Collections.singleton(FirebirdGenericResponsePacket.getPacket().setHandle(newTransactionId));
        }
        if (sqlStatement instanceof SavepointStatement || sqlStatement instanceof ReleaseSavepointStatement
                || sqlStatement instanceof RollbackStatement && ((RollbackStatement) sqlStatement).getSavepointName().isPresent()) {
            return Collections.singleton(FirebirdGenericResponsePacket.getPacket());
        }
        if (!transactions.isTransactionActive(connectionId, transactionId)) {
            if (0 == transactionId && (sqlStatement instanceof DMLStatement || sqlStatement instanceof DDLStatement)) {
                return Collections.singleton(FirebirdGenericResponsePacket.getPacket().setStatusVector(FirebirdStatusVector.fromSegments(Arrays.asList(
                        new FirebirdStatusVector.Segment(ISCConstants.isc_dsql_error, Collections.emptyList(), null),
                        FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_sqlerr, Collections.singletonList(FirebirdStatusVector.Argument.ofNumber(-901)), null),
                        new FirebirdStatusVector.Segment(ISCConstants.isc_bad_trans_handle, Collections.emptyList(), null)))));
            }
            return Collections.singleton(invalidTransactionResponse(transactionId, false));
        }
        ConnectionSession transactionSession = FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, transactionId);
        if (null == transactionSession) {
            return Collections.singleton(invalidTransactionResponse(transactionId, false));
        }
        try (Scope ignored = FirebirdCancellationRegistry.getInstance().enter(connectionId, transactionSession.getDatabaseConnectionManager())) {
            ProxyBackendHandlerFactory.newInstance(databaseType, packet.getSQL(), sqlStatement, transactionSession, packet.getHintValueContext()).execute();
            if (sqlStatement instanceof CommitStatement || sqlStatement instanceof RollbackStatement && !((RollbackStatement) sqlStatement).getSavepointName().isPresent()) {
                transactions.closeTransaction(connectionId, transactionId);
                FirebirdTransactionSessionRegistry.getInstance().unregister(connectionSession, transactionId);
                return Collections.singleton(FirebirdGenericResponsePacket.getPacket());
            }
            return Collections.singleton(FirebirdGenericResponsePacket.getPacket().setHandle(transactionId));
        } catch (final SQLException | ShardingSphereSQLException | SQLDialectException ex) {
            if (ex instanceof SQLException) {
                Optional<FirebirdStatusVector> status = FirebirdTransactionOptionsHandler.resolveBackendFailure((SQLException) ex, Collections.emptyList());
                if (status.isPresent()) {
                    return Collections.singleton(FirebirdGenericResponsePacket.getPacket().setStatusVector(status.get()).setHandle(transactionId));
                }
            }
            if (transactionSession == connectionSession) {
                throw ex;
            }
            return Collections.singleton(((FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(ex)).setHandle(transactionId));
        } finally {
            if (transactionSession != connectionSession) {
                transactionSession.clearQueryContext();
                FirebirdTransactionSessionRegistry.getInstance().closeExecutionResources(transactionSession);
            }
        }
    }
    
    private Optional<FirebirdStatusVector> getReservationError(final FirebirdTransactionOptions options) {
        if (options.getReservations().isEmpty()) {
            return Optional.empty();
        }
        return FirebirdReservationValidator.validate(options, packet.getSQL(), connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
    }
    
    private FirebirdGenericResponsePacket syntaxErrorResponse(final String token, final int line, final int column, final int transactionId) {
        return FirebirdGenericResponsePacket.getPacket().setStatusVector(FirebirdStatusVector.fromSegments(Arrays.asList(
                new FirebirdStatusVector.Segment(ISCConstants.isc_dsql_error, Collections.emptyList(), null),
                FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_sqlerr, Collections.singletonList(FirebirdStatusVector.Argument.ofNumber(-104)), null),
                FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_dsql_token_unk_err,
                        Arrays.asList(FirebirdStatusVector.Argument.ofNumber(line), FirebirdStatusVector.Argument.ofNumber(column)), null),
                FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_random, Collections.singletonList(FirebirdStatusVector.Argument.ofString(token)), null))))
                .setHandle(transactionId);
    }
    
    private FirebirdGenericResponsePacket invalidTransactionResponse(final int transactionId, final boolean active) {
        FirebirdGenericResponsePacket result = (FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(new InvalidTransactionHandleException(transactionId));
        return result.setHandle(active ? transactionId : 0);
    }
    
    private Optional<DatabasePacket> getSnapshotError(final FirebirdTransactionOptions options) {
        if (options.getSnapshotNumbers().isEmpty()) {
            return Optional.empty();
        }
        try {
            if (!FirebirdTransactionOptionsHandler.hasMultiplePhysicalDatabases(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()
                    .getDatabase(connectionSession.getCurrentDatabaseName()).getResourceMetaData().getStorageUnits().values())) {
                return Optional.empty();
            }
            return Optional.of(FirebirdGenericResponsePacket.getPacket().setStatusVector(FirebirdStatusVector.fromSegments(Arrays.asList(
                    new FirebirdStatusVector.Segment(ISCConstants.isc_wish_list, Collections.emptyList(), null),
                    FirebirdStatusVector.Segment.withArguments(ISCConstants.isc_random,
                            Collections.singletonList(FirebirdStatusVector.Argument.ofString("SNAPSHOT AT NUMBER is not supported across multiple physical databases")), null)))));
        } catch (final SQLException ex) {
            return Optional.of(FirebirdErrorPacketFactory.newInstance(ex));
        }
    }
    
    private void startTransaction(final ConnectionSession session, final SetTransactionStatement statement) throws SQLException {
        ProxyDatabaseConnectionManager manager = session.getDatabaseConnectionManager();
        manager.handleAutoCommit();
        FirebirdTransactionOptions options = statement.getAttributes().findAttribute(FirebirdTransactionOptions.class).orElseGet(FirebirdTransactionOptions::new);
        FirebirdTransactionOptionsHandler handler = new FirebirdTransactionOptionsHandler(options);
        manager.getConnectionInitializers().add(handler::initialize);
        manager.getConnectionReleaseProcessors().add(handler::release);
        String databaseName = session.getCurrentDatabaseName();
        try {
            for (String each : ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageUnits().keySet()) {
                manager.getConnections(databaseName, each, 0, 1, ConnectionMode.CONNECTION_STRICTLY);
            }
        } catch (final SQLException | IllegalArgumentException ex) {
            rollbackOnFailure(manager, ex);
            for (SQLException each : manager.closeConnections(false)) {
                ex.addSuppressed(each);
            }
            throw ex;
        }
    }
    
    private void rollbackOnFailure(final ProxyDatabaseConnectionManager manager, final Exception cause) {
        try {
            new ProxyBackendTransactionManager(manager).rollback();
        } catch (final SQLException ex) {
            cause.addSuppressed(ex);
        }
    }
    
    private void unregisterOnFailure(final int transactionId, final Exception cause) {
        try {
            FirebirdTransactionSessionRegistry.getInstance().unregister(connectionSession, transactionId);
        } catch (final SQLException ex) {
            cause.addSuppressed(ex);
        }
    }
}
