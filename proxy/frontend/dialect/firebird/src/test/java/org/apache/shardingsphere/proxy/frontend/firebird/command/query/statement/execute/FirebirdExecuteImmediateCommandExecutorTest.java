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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteImmediatePacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.ProxySQLComQueryParser;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionSessionRegistry;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.LockMode;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.Option;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.Reservation;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.SetRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jaybird.xca.FBLocalTransaction;
import org.firebirdsql.jaybird.xca.FBManagedConnection;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBSQLExceptionInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.Mock;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxySQLComQueryParser.class, ProxyBackendHandlerFactory.class, ProxyContext.class})
class FirebirdExecuteImmediateCommandExecutorTest {
    
    private static final int CONNECTION_ID = 3;
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    @Mock
    private FirebirdExecuteImmediatePacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private ProxyBackendHandler proxyBackendHandler;
    
    @Mock
    private Connection connection;
    
    @Mock
    private FBConnection firebird;
    
    @Mock
    private FBManagedConnection managedConnection;
    
    @Mock
    private FBLocalTransaction transaction;
    
    @Mock
    private TransactionParameterBuffer original;
    
    @Mock
    private TransactionParameterBuffer saved;
    
    @Mock
    private TransactionParameterBuffer parameters;
    
    @Mock
    private FbDatabase database;
    
    private ShardingSphereDatabase logicalDatabase;
    
    private ProxyDatabaseConnectionManager manager;
    
    @BeforeEach
    void setUp() {
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
    }
    
    @AfterEach
    void tearDown() throws SQLException {
        try {
            if (null != manager) {
                manager.closeConnections(false);
                FirebirdTransactionSessionRegistry.getInstance().unregister(connectionSession, 1);
            }
        } finally {
            FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        }
    }
    
    @Test
    void assertExecuteStartsTransaction() throws SQLException {
        mockSetTransaction();
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        Collection<DatabasePacket> actual = new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute();
        int actualHandle = ((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle();
        assertThat(actualHandle, is(1));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, actualHandle));
    }
    
    private void mockSetTransaction() {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getSQL()).thenReturn("SET TRANSACTION");
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE));
    }
    
    @Test
    void assertDuplicateTransactionOption() throws SQLException {
        mockSetTransaction();
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.setDuplicateClause("[NO] WAIT");
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, options));
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actualResponse.getHandle(), is(0));
        assertThat(actualResponse.getStatusVector().getSegments().size(), is(2));
        assertThat(actualResponse.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_sqlerr));
        assertThat(actualResponse.getStatusVector().getSegments().get(0).getTypedArguments().get(0).getNumber(), is(-637));
        assertThat(actualResponse.getStatusVector().getSegments().get(1).getGdsCode(), is(ISCConstants.isc_dsql_duplicate_spec));
        assertThat(actualResponse.getStatusVector().getSegments().get(1).getArguments().get(0), is("[NO] WAIT"));
        assertFalse(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
    }
    
    @Test
    void assertDuplicateTransactionOptionPreservesActiveHandle() throws SQLException {
        mockSetTransaction();
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        when(packet.getTransactionId()).thenReturn(transactionId);
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.setDuplicateClause("[NO] WAIT");
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, options));
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actualResponse.getHandle(), is(transactionId));
        assertThat(actualResponse.getStatusVector().getSegments().get(1).getArguments().get(0), is("[NO] WAIT"));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @ParameterizedTest
    @MethodSource("invalidReservationNames")
    void assertInvalidReservationName(final String prefix, final String name, final int error, final int line, final int column) throws SQLException {
        String sql = prefix + name + " FOR WRITE";
        mockReservation(sql, name);
        int existingTransaction = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdTransactionSessionRegistry.getInstance().register(connectionSession, existingTransaction);
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actual.getHandle(), is(0));
        assertThat(actual.getStatusVector().getSegments().size(), is(4));
        assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_dsql_error));
        assertThat(actual.getStatusVector().getSegments().get(1).getGdsCode(), is(ISCConstants.isc_sqlerr));
        assertThat(actual.getStatusVector().getSegments().get(1).getTypedArguments().get(0).getNumber(), is(-104));
        assertThat(actual.getStatusVector().getSegments().get(2).getGdsCode(), is(error));
        assertThat(actual.getStatusVector().getSegments().get(3).getGdsCode(), is(ISCConstants.isc_dsql_line_col_error));
        assertThat(actual.getStatusVector().getSegments().get(3).getTypedArguments().get(0).getNumber(), is(line));
        assertThat(actual.getStatusVector().getSegments().get(3).getTypedArguments().get(1).getNumber(), is(column));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, existingTransaction));
        assertThat(FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, existingTransaction), sameInstance(connectionSession));
    }
    
    private static Stream<Arguments> invalidReservationNames() {
        String unicode = "\"" + repeat("é", 64) + "\"";
        return Stream.of(
                Arguments.of("SET TRANSACTION RESERVING ", repeat("X", 64), ISCConstants.isc_dyn_name_longer, 1, 27),
                Arguments.of("SET TRANSACTION RESERVING ", repeat("X", 256), ISCConstants.isc_token_too_long, 1, 27),
                Arguments.of("SET TRANSACTION RESERVING ", "\"" + repeat("X", 253) + "\"", ISCConstants.isc_dyn_name_longer, 1, 27),
                Arguments.of("SET TRANSACTION RESERVING ", "\"" + repeat("X", 256) + "\"", ISCConstants.isc_token_too_long, 1, 27),
                Arguments.of("SET TRANSACTION RESERVING ", "\"\"", ISCConstants.isc_dyn_zero_len_id, 1, 27),
                Arguments.of("SET TRANSACTION RESERVING ", "\"" + repeat("\"\"", 64) + "\"", ISCConstants.isc_dyn_name_longer, 1, 27),
                Arguments.of("SET TRANSACTION RESERVING ", unicode, ISCConstants.isc_dyn_name_longer, 1, 27),
                Arguments.of("SET /* é */ TRANSACTION RESERVING ", unicode, ISCConstants.isc_dyn_name_longer, 1, 36),
                Arguments.of("SET /* 😀 */ TRANSACTION RESERVING ", unicode, ISCConstants.isc_dyn_name_longer, 1, 38),
                Arguments.of("SET\rTRANSACTION RESERVING ", unicode, ISCConstants.isc_dyn_name_longer, 2, 23),
                Arguments.of("SET TRANSACTION RESERVING \"a\rb\", ", unicode, ISCConstants.isc_dyn_name_longer, 1, 34));
    }
    
    private static String repeat(final String text, final int count) {
        return String.join("", Collections.nCopies(count, text));
    }
    
    private FirebirdTransactionOptions mockReservation(final String sql, final String name) throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        when(packet.getSQL()).thenReturn(sql);
        int position = sql.lastIndexOf(name);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(position, position + name.length() - 1, new IdentifierValue(name)));
        FirebirdTransactionOptions result = new FirebirdTransactionOptions();
        result.getReservations().add(new Reservation(Collections.singletonList(table), LockMode.WRITE));
        when(ProxySQLComQueryParser.parse(sql, DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, result));
        return result;
    }
    
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void assertReservationErrorOrder(final boolean duplicateFirst) throws SQLException {
        String name = "\"" + repeat("é", 64) + "\"";
        String sql = duplicateFirst ? "SET TRANSACTION WAIT NO WAIT RESERVING " + name : "SET TRANSACTION RESERVING " + name + " WAIT NO WAIT";
        FirebirdTransactionOptions options = mockReservation(sql, name);
        options.setDuplicateClause("[NO] WAIT");
        options.setValidationPosition(sql.lastIndexOf("WAIT") + "WAIT".length() - 1);
        FirebirdStatusVector actual = ((FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next()).getStatusVector();
        assertThat(actual.getSegments().get(duplicateFirst ? 1 : 2).getGdsCode(), is(duplicateFirst ? ISCConstants.isc_dsql_duplicate_spec : ISCConstants.isc_dyn_name_longer));
    }
    
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void assertQualifiedReservationError(final boolean longOwner) throws SQLException {
        String prefix = "SET /* é */ TRANSACTION RESERVING ";
        String owner = longOwner ? repeat("X", 256) : "T";
        String name = longOwner ? "T" : repeat("X", 256);
        String sql = prefix + owner + "." + name;
        FirebirdTransactionOptions options = mockReservation(sql, name);
        int ownerPosition = prefix.length();
        SimpleTableSegment actualTable = options.getReservations().iterator().next().getTables().iterator().next();
        actualTable.setOwner(new OwnerSegment(ownerPosition, ownerPosition + owner.length() - 1, new IdentifierValue(owner)));
        options.setInvalidSyntaxToken(".");
        options.setValidationPosition(ownerPosition + owner.length());
        FirebirdStatusVector actual = ((FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next()).getStatusVector();
        assertThat(actual.getSegments().get(0).getGdsCode(), is(ISCConstants.isc_dsql_error));
        assertThat(actual.getSegments().get(1).getTypedArguments().get(0).getNumber(), is(-104));
        assertThat(actual.getSegments().get(2).getGdsCode(), is(longOwner ? ISCConstants.isc_token_too_long : ISCConstants.isc_dsql_token_unk_err));
        assertThat(actual.getSegments().get(longOwner ? 3 : 2).getTypedArguments().get(0).getNumber(), is(1));
        assertThat(actual.getSegments().get(longOwner ? 3 : 2).getTypedArguments().get(1).getNumber(), is(longOwner ? 36 : 37));
        assertThat(actual.getSegments().get(3).getGdsCode(), is(longOwner ? ISCConstants.isc_dsql_line_col_error : ISCConstants.isc_random));
        if (!longOwner) {
            assertThat(actual.getSegments().get(3).getArguments().get(0), is("."));
        }
    }
    
    @Test
    void assertInvalidLockTimeout() throws SQLException {
        mockSetTransaction();
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.setInvalidLockTimeoutLine(1);
        options.setInvalidLockTimeoutColumn(30);
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, options));
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actualResponse.getStatusVector().getSegments().size(), is(4));
        assertThat(actualResponse.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_dsql_error));
        assertThat(actualResponse.getStatusVector().getSegments().get(1).getTypedArguments().get(0).getNumber(), is(-842));
        assertThat(actualResponse.getStatusVector().getSegments().get(2).getGdsCode(), is(ISCConstants.isc_expec_short));
        assertThat(actualResponse.getStatusVector().getSegments().get(3).getGdsCode(), is(ISCConstants.isc_dsql_line_col_error));
        assertThat(actualResponse.getStatusVector().getSegments().get(3).getTypedArguments().get(0).getNumber(), is(1));
        assertThat(actualResponse.getStatusVector().getSegments().get(3).getTypedArguments().get(1).getNumber(), is(30));
        assertFalse(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
    }
    
    @Test
    void assertOverflowingLockTimeout() throws SQLException {
        mockSetTransaction();
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.setInvalidLockTimeoutLine(1);
        options.setInvalidLockTimeoutColumn(30);
        options.setInvalidLockTimeoutToken("2147483648");
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, options));
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actualResponse.getStatusVector().getSegments().size(), is(4));
        assertThat(actualResponse.getStatusVector().getSegments().get(1).getTypedArguments().get(0).getNumber(), is(-104));
        assertThat(actualResponse.getStatusVector().getSegments().get(2).getGdsCode(), is(ISCConstants.isc_dsql_token_unk_err));
        assertThat(actualResponse.getStatusVector().getSegments().get(2).getTypedArguments().get(0).getNumber(), is(1));
        assertThat(actualResponse.getStatusVector().getSegments().get(2).getTypedArguments().get(1).getNumber(), is(30));
        assertThat(actualResponse.getStatusVector().getSegments().get(3).getArguments().get(0), is("2147483648"));
    }
    
    @Test
    void assertInvalidSyntax() throws SQLException {
        mockSetTransaction();
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.setInvalidSyntaxToken("VERSION");
        options.setInvalidSyntaxLine(1);
        options.setInvalidSyntaxColumn(32);
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, options));
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actualResponse.getStatusVector().getSegments().get(1).getTypedArguments().get(0).getNumber(), is(-104));
        assertThat(actualResponse.getStatusVector().getSegments().get(2).getTypedArguments().get(1).getNumber(), is(32));
        assertThat(actualResponse.getStatusVector().getSegments().get(3).getArguments().get(0), is("VERSION"));
    }
    
    @Test
    void assertTransactionStartFailurePreservesBackendSegments() throws SQLException {
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.getSnapshotNumbers().add("0");
        SQLException detail = new FBSQLExceptionInfo("invalid parameter in transaction parameter block", "HY000", ISCConstants.isc_bad_tpb_content);
        detail.setNextException(new FBSQLExceptionInfo("Option isc_tpb_lock_timeout is not valid if isc_tpb_nowait was used previously in TPB", "HY000", ISCConstants.isc_tpb_conflicting_options));
        SQLException error = new SQLException("transaction start failed", "HY000", ISCConstants.isc_bad_tpb_content, detail);
        mockTransactionStartFailure(options, error);
        FirebirdStatusVector actual = ((FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next()).getStatusVector();
        assertThat(actual.getSegments().size(), is(2));
        assertThat(actual.getSegments().get(0).getGdsCode(), is(ISCConstants.isc_bad_tpb_content));
        assertThat(actual.getSegments().get(1).getGdsCode(), is(ISCConstants.isc_tpb_conflicting_options));
        assertThat(actual.getSegments().get(1).getArguments(), is(Arrays.asList("isc_tpb_lock_timeout", "isc_tpb_nowait")));
    }
    
    private void mockTransactionStartFailure(final FirebirdTransactionOptions options, final Exception error) throws SQLException {
        mockSetTransaction();
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, options));
        RuleMetaData ruleMetaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData();
        doReturn(mock(TransactionRule.class)).when(ruleMetaData).getSingleRule(TransactionRule.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageUnits())
                .thenReturn(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        when(connectionSession.getDatabaseConnectionManager().getConnections("foo_db", "foo_ds", 0, 1, ConnectionMode.CONNECTION_STRICTLY)).thenThrow(error);
    }
    
    @Test
    void assertZeroSnapshotFailurePreservesBackendArgument() throws SQLException {
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.getSnapshotNumbers().add("0");
        SQLException detail = new FBSQLExceptionInfo("invalid parameter in transaction parameter block; isc_tpb_at_snapshot_number", "HY000", ISCConstants.isc_bad_tpb_content);
        SQLException error = new SQLException("transaction start failed", "HY000", ISCConstants.isc_bad_tpb_content, detail);
        mockTransactionStartFailure(options, error);
        FirebirdStatusVector actual = ((FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next()).getStatusVector();
        assertThat(actual.getSegments().size(), is(1));
        assertThat(actual.getSegments().get(0).getGdsCode(), is(ISCConstants.isc_bad_tpb_content));
        assertThat(actual.getSegments().get(0).getArguments().get(0), is("isc_tpb_at_snapshot_number"));
    }
    
    @Test
    void assertTransactionStartArgumentFailure() throws SQLException {
        IllegalArgumentException expected = new IllegalArgumentException("invalid transaction argument");
        mockTransactionStartFailure(new FirebirdTransactionOptions(), expected);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute());
        assertThat(actual, sameInstance(expected));
        assertFalse(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
        verify(connectionSession.getDatabaseConnectionManager()).closeConnections(false);
    }
    
    @Test
    void assertIndependentTransactionStartFailurePreservesExistingTransaction() throws SQLException {
        mockSetTransaction();
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        int firstTransactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdTransactionSessionRegistry.getInstance().register(connectionSession, firstTransactionId);
        TransactionRule rule = mock(TransactionRule.class);
        when(rule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        RuleMetaData ruleMetaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData();
        doReturn(rule).when(ruleMetaData).getSingleRule(TransactionRule.class);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageUnits())
                .thenReturn(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        SQLException detail = new FBSQLExceptionInfo("arithmetic exception", "22001", ISCConstants.isc_arith_except);
        SQLException failure = new SQLException("string truncation", "22001", ISCConstants.isc_string_truncation, detail);
        when(ProxyContext.getInstance().getBackendDataSource().getConnections(anyString(), anyString(), anyInt(), any(), any())).thenThrow(failure);
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actual.getHandle(), is(0));
        assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_string_truncation));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, firstTransactionId));
        assertThat(FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, firstTransactionId), sameInstance(connectionSession));
    }
    
    @Test
    void assertExecuteWithActiveTransaction() throws SQLException {
        mockSetTransaction();
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        when(packet.getTransactionId()).thenReturn(transactionId);
        Collection<DatabasePacket> actual = new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute();
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) actual.iterator().next();
        assertThat(actualResponse.getHandle(), is(transactionId));
        assertThat(actualResponse.getStatusVector().getSegments().size(), is(1));
        assertThat(actualResponse.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_bad_trans_handle));
        assertTrue(actualResponse.getStatusVector().getSegments().get(0).getArguments().isEmpty());
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @Test
    void assertExecuteStatementInTransaction() throws SQLException {
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        SQLStatement statement = InsertStatement.builder().databaseType(DATABASE_TYPE).build();
        mockStatement("INSERT INTO T VALUES (1)", statement, transactionId);
        Collection<DatabasePacket> actual = new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(transactionId));
        verify(proxyBackendHandler).execute();
    }
    
    private void mockStatement(final String sql, final SQLStatement statement, final int transactionId) throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        FirebirdTransactionSessionRegistry.getInstance().register(connectionSession, transactionId);
        when(packet.getSQL()).thenReturn(sql);
        when(packet.getTransactionId()).thenReturn(transactionId);
        when(packet.getHintValueContext()).thenReturn(new HintValueContext());
        when(ProxySQLComQueryParser.parse(sql, DATABASE_TYPE, connectionSession)).thenReturn(statement);
        when(ProxyBackendHandlerFactory.newInstance(eq(DATABASE_TYPE), eq(sql), eq(statement), eq(connectionSession), any(HintValueContext.class))).thenReturn(proxyBackendHandler);
    }
    
    @Test
    void assertServerErrorDoesNotEndTransaction() throws SQLException {
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        SQLStatement statement = InsertStatement.builder().databaseType(DATABASE_TYPE).build();
        mockStatement("INSERT INTO T VALUES (1)", statement, transactionId);
        SQLException detail = new FBSQLExceptionInfo("lock conflict on no wait transaction", "40001", ISCConstants.isc_lock_conflict);
        detail.setNextException(new FBSQLExceptionInfo("update conflicts with concurrent update", "40001", ISCConstants.isc_update_conflict));
        SQLException error = new SQLException("lock conflict", "40001", ISCConstants.isc_lock_conflict, detail);
        when(proxyBackendHandler.execute()).thenThrow(error);
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actual.getHandle(), is(transactionId));
        assertThat(actual.getStatusVector().getSegments().size(), is(2));
        assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_lock_conflict));
        assertThat(actual.getStatusVector().getSegments().get(1).getGdsCode(), is(ISCConstants.isc_update_conflict));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @Test
    void assertBackendErrorPreservesSqlStateTextInsideIdentifier() throws SQLException {
        SQLException detail = new FBSQLExceptionInfo("invalid parameter in transaction parameter block", "HY000", ISCConstants.isc_bad_tpb_content);
        detail.setNextException(new FBSQLExceptionInfo("Table or view X [SQLState: Z not defined in system tables after table reservation isc_tpb_lock_write in TPB",
                "HY000", ISCConstants.isc_tpb_reserv_relnotfound));
        SQLException error = new SQLException("transaction start failed", "HY000", ISCConstants.isc_bad_tpb_content, detail);
        mockTransactionStartFailure(new FirebirdTransactionOptions(), error);
        FirebirdStatusVector actual = ((FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next()).getStatusVector();
        assertThat(actual.getSegments().get(1).getGdsCode(), is(ISCConstants.isc_tpb_reserv_relnotfound));
        assertThat(actual.getSegments().get(1).getArguments(), is(Arrays.asList("X [SQLState: Z", "isc_tpb_lock_write")));
    }
    
    @Test
    void assertBackendErrorPreservesIdentifierContainingMessageTemplate() throws SQLException {
        String table = "X not defined in system tables after table reservation Y";
        SQLException detail = new FBSQLExceptionInfo("invalid parameter in transaction parameter block", "HY000", ISCConstants.isc_bad_tpb_content);
        detail.setNextException(new FBSQLExceptionInfo("Table or view " + table + " not defined in system tables after table reservation isc_tpb_lock_write in TPB",
                "HY000", ISCConstants.isc_tpb_reserv_relnotfound));
        SQLException error = new SQLException("transaction start failed", "HY000", ISCConstants.isc_bad_tpb_content, detail);
        mockTransactionStartFailure(new FirebirdTransactionOptions(), error);
        FirebirdStatusVector actual = ((FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next()).getStatusVector();
        assertThat(actual.getSegments().get(1).getGdsCode(), is(ISCConstants.isc_tpb_reserv_relnotfound));
        assertThat(actual.getSegments().get(1).getArguments(), is(Arrays.asList(table, "isc_tpb_lock_write")));
    }
    
    @Test
    void assertExecuteCommit() throws SQLException {
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        mockStatement("COMMIT", new CommitStatement(DATABASE_TYPE), transactionId);
        Collection<DatabasePacket> actual = new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(0));
        assertFalse(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @Test
    void assertExecuteRollback() throws SQLException {
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        mockStatement("ROLLBACK", new RollbackStatement(DATABASE_TYPE), transactionId);
        Collection<DatabasePacket> actual = new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute();
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getHandle(), is(0));
        assertFalse(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("statementsWithoutTransaction")
    void assertExecuteStatementWithoutTransaction(final String sql, final SQLStatement statement) throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getSQL()).thenReturn(sql);
        when(ProxySQLComQueryParser.parse(sql, DATABASE_TYPE, connectionSession)).thenReturn(statement);
        Collection<DatabasePacket> actual = new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute();
        FirebirdGenericResponsePacket actualResponse = (FirebirdGenericResponsePacket) actual.iterator().next();
        assertThat(actualResponse.getHandle(), is(0));
        assertThat(actualResponse.getStatusVector().getSegments().size(), is(3));
        assertThat(actualResponse.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_dsql_error));
        assertThat(actualResponse.getStatusVector().getSegments().get(1).getGdsCode(), is(ISCConstants.isc_sqlerr));
        assertThat(actualResponse.getStatusVector().getSegments().get(1).getTypedArguments().get(0).getNumber(), is(-901));
        assertThat(actualResponse.getStatusVector().getSegments().get(2).getGdsCode(), is(ISCConstants.isc_bad_trans_handle));
    }
    
    private static Stream<Arguments> statementsWithoutTransaction() {
        return Stream.of(Arguments.of("INSERT INTO T VALUES (1)", InsertStatement.builder().databaseType(DATABASE_TYPE).build()),
                Arguments.of("UPDATE T SET A = 1", UpdateStatement.builder().databaseType(DATABASE_TYPE).build()),
                Arguments.of("DELETE FROM T", DeleteStatement.builder().databaseType(DATABASE_TYPE).build()),
                Arguments.of("SELECT A FROM T", SelectStatement.builder().databaseType(DATABASE_TYPE).build()),
                Arguments.of("CREATE TABLE T (A INTEGER)", CreateTableStatement.builder().databaseType(DATABASE_TYPE).build()));
    }
    
    @Test
    void assertExecuteStatementWithInvalidNonzeroTransaction() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getTransactionId()).thenReturn(123);
        when(packet.getSQL()).thenReturn("INSERT INTO T VALUES (1)");
        when(ProxySQLComQueryParser.parse("INSERT INTO T VALUES (1)", DATABASE_TYPE, connectionSession)).thenReturn(InsertStatement.builder().databaseType(DATABASE_TYPE).build());
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actual.getStatusVector().getSegments().size(), is(1));
        assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_bad_trans_handle));
    }
    
    @Test
    void assertSessionStatementWithoutTransactionKeepsExistingResponse() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getSQL()).thenReturn("SET ROLE FOO");
        when(ProxySQLComQueryParser.parse("SET ROLE FOO", DATABASE_TYPE, connectionSession)).thenReturn(new SetRoleStatement(DATABASE_TYPE));
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actual.getStatusVector().getSegments().size(), is(1));
        assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_bad_trans_handle));
    }
    
    @Test
    void assertParsingErrorKeepsActiveTransaction() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        when(packet.getTransactionId()).thenReturn(transactionId);
        when(packet.getSQL()).thenReturn("SET TRANSACTION INVALID");
        when(ProxySQLComQueryParser.parse("SET TRANSACTION INVALID", DATABASE_TYPE, connectionSession)).thenThrow(new SQLParsingException("SET TRANSACTION INVALID"));
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actual.getHandle(), is(transactionId));
        assertTrue(0 != actual.getStatusVector().getSegments().get(0).getGdsCode());
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @Test
    void assertDialectParsingErrorKeepsActiveTransaction() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        when(packet.getTransactionId()).thenReturn(transactionId);
        when(packet.getSQL()).thenReturn("SET TRANSACTION INVALID");
        when(ProxySQLComQueryParser.parse("SET TRANSACTION INVALID", DATABASE_TYPE, connectionSession)).thenThrow(new DialectSQLParsingException("invalid", null, 1));
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actual.getHandle(), is(transactionId));
        assertTrue(0 != actual.getStatusVector().getSegments().get(0).getGdsCode());
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"SAVEPOINT SP1", "ROLLBACK TO \"SP1\"", "RELEASE SAVEPOINT SP1"})
    void assertSavepointRetainsUnsupportedResponse(final String sql) throws SQLException {
        int transactionId = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        when(packet.getSQL()).thenReturn(sql);
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actual.getHandle(), is(0));
        assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, transactionId));
    }
    
    @Test
    void assertInitialize() throws SQLException {
        setUpOptions();
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.getOptions().add(Option.READ_ONLY);
        options.getOptions().add(Option.NO_WAIT);
        options.getOptions().add(Option.READ_COMMITTED_VERSION);
        startTransaction(options);
        verify(connection).setAutoCommit(false);
        verify(parameters).addArgument(ISCConstants.isc_tpb_read);
        verify(parameters).addArgument(ISCConstants.isc_tpb_nowait);
        verify(parameters).addArgument(ISCConstants.isc_tpb_read_committed);
        verify(parameters).addArgument(ISCConstants.isc_tpb_rec_version);
        verify(firebird).setTransactionParameters(parameters);
        verify(transaction).begin();
    }
    
    private void setUpOptions() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        when(connectionSession.isAutoCommit()).thenReturn(true);
        when(connectionSession.getRequiredSessionVariableRecorder().isEmpty()).thenReturn(true);
        when(packet.getSQL()).thenReturn("SET TRANSACTION");
        logicalDatabase = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(logicalDatabase);
        when(logicalDatabase.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        when(ProxyContext.getInstance().getBackendDataSource().getConnections(anyString(), anyString(), anyInt(), any(), any())).thenReturn(Collections.singletonList(connection));
        manager = new ProxyDatabaseConnectionManager(connectionSession);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(manager);
        when(connection.unwrap(FBConnection.class)).thenReturn(firebird);
        when(firebird.getManagedConnection()).thenReturn(managedConnection);
        when(managedConnection.getTransactionParameters()).thenReturn(original);
        when(original.deepCopy()).thenReturn(saved);
        when(firebird.createTransactionParameterBuffer()).thenReturn(parameters);
        when(firebird.getLocalTransaction()).thenReturn(transaction);
    }
    
    private void startTransaction(final FirebirdTransactionOptions options) throws SQLException {
        if (!options.getReservations().isEmpty()) {
            when(connectionSession.getAttributeMap().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        }
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, options));
        new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("transactionOptions")
    void assertInitializeOption(final Option option, final int[] expected) throws SQLException {
        setUpOptions();
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.getOptions().add(option);
        startTransaction(options);
        for (int each : expected) {
            verify(parameters).addArgument(each);
        }
    }
    
    private static Stream<Arguments> transactionOptions() {
        return Stream.of(
                Arguments.of(Option.READ_WRITE, new int[]{ISCConstants.isc_tpb_write}),
                Arguments.of(Option.WAIT, new int[]{ISCConstants.isc_tpb_wait}),
                Arguments.of(Option.SNAPSHOT, new int[]{ISCConstants.isc_tpb_concurrency}),
                Arguments.of(Option.REPEATABLE_READ, new int[]{ISCConstants.isc_tpb_concurrency}),
                Arguments.of(Option.SNAPSHOT_TABLE_STABILITY, new int[]{ISCConstants.isc_tpb_consistency}),
                Arguments.of(Option.SERIALIZABLE, new int[]{ISCConstants.isc_tpb_consistency}),
                Arguments.of(Option.READ_COMMITTED, new int[]{ISCConstants.isc_tpb_read_committed, ISCConstants.isc_tpb_no_rec_version}),
                Arguments.of(Option.READ_COMMITTED_NO_VERSION, new int[]{ISCConstants.isc_tpb_read_committed, ISCConstants.isc_tpb_no_rec_version}),
                Arguments.of(Option.READ_COMMITTED_CONSISTENCY, new int[]{ISCConstants.isc_tpb_read_committed, 22}),
                Arguments.of(Option.NO_AUTO_UNDO, new int[]{ISCConstants.isc_tpb_no_auto_undo}),
                Arguments.of(Option.IGNORE_LIMBO, new int[]{ISCConstants.isc_tpb_ignore_limbo}),
                Arguments.of(Option.RESTART_REQUESTS, new int[]{ISCConstants.isc_tpb_restart_requests}),
                Arguments.of(Option.AUTO_COMMIT, new int[]{ISCConstants.isc_tpb_autocommit}),
                Arguments.of(Option.AUTO_RELEASE_TEMP_BLOBID, new int[]{24}));
    }
    
    @Test
    void assertInitializeNumericOptions() throws SQLException {
        setUpOptions();
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.getOptions().add(Option.SNAPSHOT);
        options.getLockTimeouts().add("2");
        options.getSnapshotNumbers().add("123");
        startTransaction(options);
        verify(parameters).addArgument(ISCConstants.isc_tpb_lock_timeout, 2);
        verify(parameters).addArgument(23, 123L);
    }
    
    @Test
    void assertSnapshotNumberRejectsDifferentPhysicalDatabases() throws SQLException {
        setUpSnapshotGuard();
        Connection[] probes = mockPhysicalDatabases(true);
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.getSnapshotNumbers().add("123");
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, options));
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        verify(probes[0]).close();
        verify(probes[1]).close();
        assertThat(actual.getHandle(), is(0));
        assertNotNull(actual.getStatusVector());
        assertThat(actual.getStatusVector().getSegments().size(), is(2));
        assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_wish_list));
        assertThat(actual.getStatusVector().getSegments().get(1).getGdsCode(), is(ISCConstants.isc_random));
        assertThat(actual.getStatusVector().getSegments().get(1).getArguments().get(0), is("SNAPSHOT AT NUMBER is not supported across multiple physical databases"));
        assertFalse(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
        verify(connection, never()).setAutoCommit(false);
    }
    
    private void setUpSnapshotGuard() {
        mockSetTransaction();
        when(connectionSession.getCurrentDatabaseName()).thenReturn("foo_db");
        logicalDatabase = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(logicalDatabase);
    }
    
    private Connection[] mockPhysicalDatabases(final boolean different) throws SQLException {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>();
        Connection[] result = new Connection[2];
        for (int index = 0; index < result.length; index++) {
            StorageUnit storageUnit = mock(StorageUnit.class);
            DataSource dataSource = mock(DataSource.class);
            Connection probe = mock(Connection.class);
            FBConnection probeFirebird = mock(FBConnection.class);
            FbDatabase probeDatabase = mock(FbDatabase.class);
            when(storageUnit.getDataSource()).thenReturn(dataSource);
            when(dataSource.getConnection()).thenReturn(probe);
            when(probe.unwrap(FBConnection.class)).thenReturn(probeFirebird);
            when(probeFirebird.getFbDatabase()).thenReturn(probeDatabase);
            byte fileId = (byte) (different ? index + 1 : 1);
            when(probeDatabase.getDatabaseInfo(any(byte[].class), eq(1024))).thenReturn(new byte[]{(byte) ISCConstants.fb_info_db_file_id, 1, 0, fileId,
                    ISCConstants.isc_info_db_id, 5, 0, 2, 1, 'd', 1, 'h', ISCConstants.isc_info_end});
            storageUnits.put("foo_ds_" + index, storageUnit);
            result[index] = probe;
        }
        when(logicalDatabase.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        return result;
    }
    
    @Test
    void assertSnapshotNumberRejectsDifferentDatabasesWithoutChangingActiveTransaction() throws SQLException {
        setUpSnapshotGuard();
        mockPhysicalDatabases(true);
        int existingTransaction = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdTransactionSessionRegistry.getInstance().register(connectionSession, existingTransaction);
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.getSnapshotNumbers().add("123");
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, options));
        try {
            FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
            assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_wish_list));
            assertTrue(FirebirdTransactionIdGenerator.getInstance().isTransactionActive(CONNECTION_ID, existingTransaction));
            assertThat(FirebirdTransactionSessionRegistry.getInstance().get(connectionSession, existingTransaction), sameInstance(connectionSession));
            verify(connection, never()).rollback();
        } finally {
            FirebirdTransactionSessionRegistry.getInstance().unregister(connectionSession, existingTransaction);
        }
    }
    
    @Test
    void assertSnapshotNumberAllowsTwoAliasesOfSamePhysicalDatabase() throws SQLException {
        setUpOptions();
        Connection[] probes = mockPhysicalDatabases(false);
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.getSnapshotNumbers().add("123");
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, options));
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertTrue(actual.getHandle() > 0);
        assertNull(actual.getStatusVector());
        verify(transaction, times(2)).begin();
        verify(probes[0]).close();
        verify(probes[1]).close();
    }
    
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void assertSnapshotNumberRequiresPhysicalIdentity(final boolean emptyFileId) throws SQLException {
        setUpSnapshotGuard();
        StorageUnit first = mock(StorageUnit.class);
        StorageUnit second = mock(StorageUnit.class);
        DataSource dataSource = mock(DataSource.class);
        Connection probe = mock(Connection.class);
        FBConnection probeFirebird = mock(FBConnection.class);
        FbDatabase probeDatabase = mock(FbDatabase.class);
        when(first.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(probe);
        when(probe.unwrap(FBConnection.class)).thenReturn(probeFirebird);
        when(probeFirebird.getFbDatabase()).thenReturn(probeDatabase);
        byte[] info = emptyFileId ? new byte[]{(byte) ISCConstants.fb_info_db_file_id, 0, 0, ISCConstants.isc_info_db_id, 5, 0, 2, 1, 'd', 1, 'h', ISCConstants.isc_info_end}
                : new byte[]{ISCConstants.isc_info_end};
        when(probeDatabase.getDatabaseInfo(any(byte[].class), eq(1024))).thenReturn(info);
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>();
        storageUnits.put("foo_ds_0", first);
        storageUnits.put("foo_ds_1", second);
        when(logicalDatabase.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.getSnapshotNumbers().add("123");
        when(ProxySQLComQueryParser.parse("SET TRANSACTION", DATABASE_TYPE, connectionSession)).thenReturn(new SetTransactionStatement(DATABASE_TYPE, null, null, null, options));
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) new FirebirdExecuteImmediateCommandExecutor(packet, connectionSession).execute().iterator().next();
        assertThat(actual.getHandle(), is(0));
        assertThat(actual.getStatusVector().getSegments().get(0).getGdsCode(), is(ISCConstants.isc_random));
        assertThat(actual.getStatusVector().getSegments().get(0).getArguments().get(0), is("Could not identify the physical database for SNAPSHOT AT NUMBER"));
        assertFalse(FirebirdTransactionIdGenerator.getInstance().hasActiveTransaction(CONNECTION_ID));
        verify(probe).close();
        verify(second, never()).getDataSource();
    }
    
    @Test
    void assertTransactionWithoutSnapshotNumberDoesNotProbePhysicalDatabase() throws SQLException {
        setUpOptions();
        StorageUnit first = mock(StorageUnit.class);
        StorageUnit second = mock(StorageUnit.class);
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>();
        storageUnits.put("foo_ds_0", first);
        storageUnits.put("foo_ds_1", second);
        when(logicalDatabase.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        startTransaction(new FirebirdTransactionOptions());
        verify(first, never()).getDataSource();
        verify(second, never()).getDataSource();
    }
    
    @Test
    void assertInitializeReservationForStorageUnit() throws SQLException {
        setUpOptions();
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 6, new IdentifierValue("t_plain")));
        options.getOptions().add(Option.RESERVING);
        options.getReservations().add(new Reservation(Collections.singletonList(table), LockMode.PROTECTED_WRITE));
        DataNodeRuleAttribute rule = mock(DataNodeRuleAttribute.class);
        Encoding encoding = mock(Encoding.class);
        when(logicalDatabase.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)).thenReturn(Collections.singletonList(rule));
        when(rule.getDataNodesByTableName("T_PLAIN")).thenReturn(Arrays.asList(new DataNode("foo_ds", (String) null, "T_PLAIN_0"), new DataNode("bar_ds", (String) null, "T_PLAIN_1")));
        when(firebird.getFbDatabase()).thenReturn(database);
        when(database.getEncoding()).thenReturn(encoding);
        startTransaction(options);
        verify(parameters).addArgument(ISCConstants.isc_tpb_lock_write, "T_PLAIN_0", encoding);
        verify(parameters).addArgument(ISCConstants.isc_tpb_protected);
        verify(parameters, never()).addArgument(ISCConstants.isc_tpb_lock_write, "T_PLAIN_1", encoding);
    }
    
    @Test
    void assertReservationDecodesQuotedIdentifier() throws SQLException {
        setUpOptions();
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("\"X\"\"Y\"")));
        options.getReservations().add(new Reservation(Collections.singletonList(table), LockMode.WRITE));
        Encoding encoding = mock(Encoding.class);
        when(logicalDatabase.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)).thenReturn(Collections.emptyList());
        when(firebird.getFbDatabase()).thenReturn(database);
        when(database.getEncoding()).thenReturn(encoding);
        startTransaction(options);
        verify(parameters).addArgument(ISCConstants.isc_tpb_lock_write, "X\"Y", encoding);
    }
    
    @Test
    void assertReleaseRollsBackAndRestores() throws SQLException {
        setUpOptions();
        startTransaction(new FirebirdTransactionOptions());
        when(transaction.inTransaction()).thenReturn(true);
        manager.closeConnections(false);
        verify(connection).rollback();
        verify(firebird).setTransactionParameters(saved);
    }
    
    @Test
    void assertReleaseAfterCommit() throws SQLException {
        setUpOptions();
        startTransaction(new FirebirdTransactionOptions());
        manager.closeConnections(false);
        verify(connection, never()).rollback();
        verify(firebird).setTransactionParameters(saved);
    }
    
    @Test
    void assertReleaseAfterConnectionCloses() throws SQLException {
        setUpOptions();
        startTransaction(new FirebirdTransactionOptions());
        when(firebird.isClosed()).thenReturn(true);
        manager.closeConnections(false);
        verify(transaction, never()).inTransaction();
        verify(firebird, never()).setTransactionParameters(saved);
    }
    
    @Test
    void assertReleaseAbortsWhenRollbackFails() throws SQLException {
        setUpOptions();
        startTransaction(new FirebirdTransactionOptions());
        SQLException expected = new SQLException("rollback failed");
        when(transaction.inTransaction()).thenReturn(true);
        when(firebird.getFbDatabase()).thenReturn(database);
        doThrow(expected).when(connection).rollback();
        Collection<SQLException> actual = manager.closeConnections(false);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(expected));
        verify(database).cancelOperation(ISCConstants.fb_cancel_abort);
        verify(firebird, never()).setTransactionParameters(saved);
    }
    
    @Test
    void assertReleasePreservesAbortFailure() throws SQLException {
        setUpOptions();
        startTransaction(new FirebirdTransactionOptions());
        SQLException expected = new SQLException("rollback failed");
        SQLException abortFailure = new SQLException("abort failed");
        when(transaction.inTransaction()).thenReturn(true);
        when(firebird.getFbDatabase()).thenReturn(database);
        doThrow(expected).when(connection).rollback();
        doThrow(abortFailure).when(database).cancelOperation(ISCConstants.fb_cancel_abort);
        Collection<SQLException> actual = manager.closeConnections(false);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(expected));
        assertThat(actual.iterator().next().getSuppressed().length, is(1));
        assertThat(actual.iterator().next().getSuppressed()[0], sameInstance(abortFailure));
    }
    
    @Test
    void assertInvalidTimeoutIsRejected() throws SQLException {
        setUpOptions();
        RuleMetaData ruleMetaData = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData();
        doReturn(mock(TransactionRule.class)).when(ruleMetaData).getSingleRule(TransactionRule.class);
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        options.getLockTimeouts().add("32768");
        SQLException actual = assertThrows(SQLException.class, () -> startTransaction(options));
        assertThat(actual.getSQLState(), is("42000"));
        verify(firebird).setTransactionParameters(saved);
    }
}
