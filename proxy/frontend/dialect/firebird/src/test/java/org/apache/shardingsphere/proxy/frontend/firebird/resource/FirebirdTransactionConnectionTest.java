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

package org.apache.shardingsphere.proxy.frontend.firebird.resource;

import lombok.SneakyThrows;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.firebirdsql.extern.decimal.Decimal64;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.impl.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBPreparedStatement;
import org.firebirdsql.jdbc.FBRowId;
import org.firebirdsql.jdbc.FirebirdPreparedStatement;
import org.firebirdsql.jdbc.FirebirdStatement;
import org.firebirdsql.jdbc.JaybirdTypeCodes;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class FirebirdTransactionConnectionTest {
    
    @Mock
    private Connection pooledConnection;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private FBConnection firebirdConnection;
    
    @Mock
    private FbDatabase database;
    
    @Mock
    private FbTransaction firstTransaction;
    
    @Mock
    private FbTransaction secondTransaction;
    
    @Mock
    private FbTransaction metadataTransaction;
    
    private Connection attachment;
    
    private final AtomicReference<FbTransaction> currentTransaction = new AtomicReference<>();
    
    @BeforeEach
    void setup() throws SQLException {
        when(pooledConnection.unwrap(FBConnection.class)).thenReturn(firebirdConnection);
        when(firebirdConnection.getFbDatabase()).thenReturn(database);
        when(firebirdConnection.createTransactionParameterBuffer()).thenAnswer(invocation -> new TransactionParameterBufferImpl());
        when(database.startTransaction(any())).thenReturn(firstTransaction, secondTransaction, metadataTransaction);
        GDSHelper helper = firebirdConnection.getGDSHelper();
        when(helper.getCurrentTransaction()).thenAnswer(invocation -> currentTransaction.get());
        doAnswer(invocation -> {
            currentTransaction.set(invocation.getArgument(0));
            return null;
        }).when(helper).setCurrentTransaction(nullable(FbTransaction.class));
        attachment = FirebirdTransactionConnection.createAttachment(pooledConnection);
    }
    
    @Test
    void assertReuseAttachment() throws SQLException {
        withJdbcConnections(jdbcConnections -> {
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            assertThat(FirebirdTransactionConnection.createAttachment(attachment), sameInstance(attachment));
            verify(pooledConnection).setAutoCommit(false);
            connection.close();
            attachment.close();
        });
    }
    
    @SneakyThrows(Throwable.class)
    private void withJdbcConnections(final ThrowingConsumer<MockedConstruction<FBConnection>> assertions) {
        try (MockedConstruction<FBConnection> construction = mockConstruction(FBConnection.class, withSettings().defaultAnswer(invocation -> invokeJdbcConnection(invocation)))) {
            try {
                assertions.accept(construction);
            } finally {
                attachment.close();
            }
        }
    }
    
    @Nullable
    @SneakyThrows(Throwable.class)
    private Object invokeJdbcConnection(final InvocationOnMock invocation) {
        if (Object.class == invocation.getMethod().getDeclaringClass()) {
            return Answers.RETURNS_DEFAULTS.answer(invocation);
        }
        switch (invocation.getMethod().getName()) {
            case "commit":
            case "setAutoCommit":
            case "setManagedEnvironment":
            case "setManagedConnection":
                return null;
            case "rollback":
                if (0 == invocation.getArguments().length) {
                    return null;
                }
                break;
            default:
                break;
        }
        try {
            return Connection.class.getMethod(invocation.getMethod().getName(), invocation.getMethod().getParameterTypes()).invoke(pooledConnection, invocation.getArguments());
        } catch (final InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("statementUnwrapTypes")
    void assertStatementUnwrapDriverType(final Class<? extends Statement> requestedType) {
        withJdbcConnections(jdbcConnections -> {
            FBPreparedStatement statement = mock(FBPreparedStatement.class);
            when(statement.isWrapperFor(requestedType)).thenReturn(true);
            when(statement.unwrap(requestedType)).thenAnswer(invocation -> statement);
            when(pooledConnection.prepareStatement("foo_sql")).thenReturn(statement);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            PreparedStatement statementView = connection.prepareStatement("foo_sql");
            assertTrue(statementView.isWrapperFor(requestedType));
            Statement actual = statementView.unwrap(requestedType);
            assertThat(actual, isA(requestedType));
            assertNull(currentTransaction.get());
        });
    }
    
    private static Stream<Class<? extends Statement>> statementUnwrapTypes() {
        return Stream.of(FirebirdPreparedStatement.class, FirebirdStatement.class, FBPreparedStatement.class);
    }
    
    @Test
    void assertStatementUnwrapStandardInterfaceKeepsConnectionView() {
        withJdbcConnections(jdbcConnections -> {
            PreparedStatement statement = mock(PreparedStatement.class);
            when(statement.unwrap(PreparedStatement.class)).thenReturn(statement);
            when(pooledConnection.prepareStatement("foo_sql")).thenReturn(statement);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            PreparedStatement statementView = connection.prepareStatement("foo_sql");
            PreparedStatement actual = statementView.unwrap(PreparedStatement.class);
            assertThat(actual.getConnection(), sameInstance(connection));
            assertNull(currentTransaction.get());
        });
    }
    
    @Test
    void assertStatementUnwrapUnsupportedInterface() {
        withJdbcConnections(jdbcConnections -> {
            PreparedStatement statement = mock(PreparedStatement.class);
            SQLException expected = new SQLException("foo_unwrap");
            when(statement.unwrap(Runnable.class)).thenThrow(expected);
            when(pooledConnection.prepareStatement("foo_sql")).thenReturn(statement);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            PreparedStatement statementView = connection.prepareStatement("foo_sql");
            assertFalse(statementView.isWrapperFor(Runnable.class));
            SQLException actual = assertThrows(SQLException.class, () -> statementView.unwrap(Runnable.class));
            assertThat(actual, sameInstance(expected));
            assertNull(currentTransaction.get());
        });
    }
    
    @Test
    void assertCreateTransactionReusesHandle() {
        withJdbcConnections(jdbcConnections -> {
            Connection first = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            Connection actual = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            assertThat(actual, sameInstance(first));
            assertTrue(actual.equals(actual));
            assertFalse(actual.equals(attachment));
            assertThat(actual.hashCode(), is(System.identityHashCode(actual)));
            assertThat(actual.toString(), is("Firebird JDBC transaction view"));
            assertThat(jdbcConnections.constructed().size(), is(1));
            verify(database).startTransaction(any());
        });
    }
    
    @Test
    void assertCreateTransactionAfterAttachmentClosed() {
        withJdbcConnections(jdbcConnections -> {
            FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            attachment.close();
            SQLException actual = assertThrows(SQLException.class, () -> FirebirdTransactionConnection.createTransaction(attachment, 2, false, TransactionIsolationLevel.REPEATABLE_READ));
            assertThat(actual.getSQLState(), is("08003"));
            assertThat(jdbcConnections.constructed().size(), is(1));
            verify(database).startTransaction(any());
        });
    }
    
    @Test
    void assertCreateTransactionWithStartFailure() throws SQLException {
        SQLException expected = new SQLException("foo_unavailable", "08001");
        when(database.startTransaction(any())).thenThrow(expected).thenReturn(firstTransaction);
        withJdbcConnections(jdbcConnections -> {
            SQLException actual = assertThrows(SQLException.class, () -> FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ));
            assertThat(actual, sameInstance(expected));
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            connection.close();
            verify(database, times(2)).startTransaction(any());
            verify(firstTransaction).rollback();
        });
    }
    
    @Test
    void assertCloseTransactionRetainsSibling() {
        withJdbcConnections(jdbcConnections -> {
            Connection first = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            Connection sibling = FirebirdTransactionConnection.createTransaction(attachment, 2, false, TransactionIsolationLevel.READ_COMMITTED);
            FirebirdTransactionConnection.closeTransaction(attachment, 1);
            assertTrue(first.isClosed());
            assertFalse(sibling.isClosed());
            verify(firstTransaction).rollback();
            verify(secondTransaction, never()).rollback();
            FirebirdTransactionConnection.closeTransaction(attachment, 1);
            verify(firstTransaction).rollback();
        });
    }
    
    @Test
    void assertCloseTransactionWithoutAttachment() {
        withJdbcConnections(jdbcConnections -> {
            Connection actual = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            FirebirdTransactionConnection.closeTransaction(pooledConnection, 1);
            assertFalse(actual.isClosed());
            verify(firstTransaction, never()).rollback();
            verify(pooledConnection, never()).close();
        });
    }
    
    @Test
    void assertCloseTransactionWithUnownedProxy() {
        withJdbcConnections(jdbcConnections -> {
            Connection actual = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            Connection pooledProxy = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (proxy, method, args) -> method.invoke(pooledConnection, args));
            FirebirdTransactionConnection.closeTransaction(pooledProxy, 1);
            assertFalse(actual.isClosed());
            verify(firstTransaction, never()).rollback();
            verify(pooledConnection, never()).close();
        });
    }
    
    @ParameterizedTest
    @CsvSource({"READ_COMMITTED,true,15,2", "REPEATABLE_READ,false,2,4", "SERIALIZABLE,false,1,8"})
    void assertTransactionOptions(final TransactionIsolationLevel isolationLevel, final boolean readOnly, final int isolationArgument, final int jdbcIsolation) throws SQLException {
        withJdbcConnections(jdbcConnections -> {
            Connection actual = FirebirdTransactionConnection.createTransaction(attachment, 1, readOnly, isolationLevel);
            ArgumentCaptor<TransactionParameterBuffer> bufferCaptor = ArgumentCaptor.forClass(TransactionParameterBuffer.class);
            verify(database).startTransaction(bufferCaptor.capture());
            assertTrue(bufferCaptor.getValue().hasArgument(readOnly ? ISCConstants.isc_tpb_read : ISCConstants.isc_tpb_write));
            assertTrue(bufferCaptor.getValue().hasArgument(isolationArgument));
            assertThat(actual.getTransactionIsolation(), is(jdbcIsolation));
            assertTrue(actual.isReadOnly() == readOnly);
            assertFalse(actual.getAutoCommit());
            actual.setAutoCommit(false);
            actual.close();
        });
    }
    
    @Test
    void assertIndependentCommitAndRollback() throws SQLException {
        withJdbcConnections(jdbcConnections -> {
            FbTransaction parentTransaction = mock(FbTransaction.class);
            currentTransaction.set(parentTransaction);
            Connection firstConnection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            Connection actual = FirebirdTransactionConnection.createTransaction(attachment, 2, false, TransactionIsolationLevel.READ_COMMITTED);
            assertThat(jdbcConnections.constructed().size(), is(2));
            firstConnection.commit();
            assertThat(currentTransaction.get(), sameInstance(parentTransaction));
            verify(firstTransaction).commit();
            verify(secondTransaction, never()).commit();
            verify(secondTransaction, never()).rollback();
            actual.rollback();
            assertThat(currentTransaction.get(), sameInstance(parentTransaction));
            verify(parentTransaction, never()).commit();
            verify(parentTransaction, never()).rollback();
            verify(jdbcConnections.constructed().get(0)).commit();
            verify(jdbcConnections.constructed().get(1), never()).commit();
            verify(secondTransaction).rollback();
            verify(jdbcConnections.constructed().get(1)).rollback();
            firstConnection.close();
            actual.close();
            verify(jdbcConnections.constructed().get(0)).setManagedConnection(null);
            verify(jdbcConnections.constructed().get(1)).setManagedConnection(null);
            verify(firstTransaction, never()).rollback();
            verify(pooledConnection, never()).close();
            attachment.close();
            attachment.close();
            verify(pooledConnection).close();
        });
    }
    
    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void assertPrepareAndFetchUseTheirTransactions(final boolean parentTransactionPresent, final boolean resultMetadata) throws SQLException {
        withJdbcConnections(jdbcConnections -> {
            FbTransaction previousTransaction = parentTransactionPresent ? mock(FbTransaction.class) : null;
            currentTransaction.set(previousTransaction);
            when(database.startTransaction(any())).thenReturn(firstTransaction, metadataTransaction);
            PreparedStatement statement = mock(PreparedStatement.class);
            ResultSet resultSet = mock(ResultSet.class, Answers.RETURNS_DEEP_STUBS);
            Blob blob = mock(Blob.class);
            when(pooledConnection.prepareStatement("foo_sql")).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(metadataTransaction));
                return statement;
            });
            when(statement.executeQuery()).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(firstTransaction));
                return resultSet;
            });
            when(resultSet.next()).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(firstTransaction));
                return true;
            });
            when(resultSet.getBlob(1)).thenReturn(blob);
            when(blob.getBytes(1L, 2)).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(firstTransaction));
                return new byte[]{1, 2};
            });
            ResultSetMetaData metadata = mock(ResultSetMetaData.class);
            if (resultMetadata) {
                when(resultSet.getMetaData()).thenReturn(metadata);
            } else {
                when(statement.getMetaData()).thenReturn(metadata);
            }
            when(metadata.getPrecision(1)).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(metadataTransaction));
                return 10;
            });
            Connection actualConnection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            PreparedStatement actualStatement = actualConnection.prepareStatement("foo_sql");
            assertThat(actualStatement.getConnection(), sameInstance(actualConnection));
            ResultSet actualResultSet = actualStatement.executeQuery();
            assertTrue(actualResultSet.next());
            assertThat(actualResultSet.getBlob(1).getBytes(1L, 2), is(new byte[]{1, 2}));
            ResultSetMetaData actualMetadata = resultMetadata ? actualResultSet.getMetaData() : actualStatement.getMetaData();
            assertThat(actualMetadata.getPrecision(1), is(10));
            assertThat(currentTransaction.get(), sameInstance(previousTransaction));
            actualResultSet.close();
            actualStatement.close();
            actualConnection.close();
            attachment.close();
            verify(metadataTransaction).rollback();
        });
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("bufferedColumnArguments")
    void assertBufferedColumnReads(final String name, final int columnType, final Object expected) {
        withJdbcConnections(jdbcConnections -> {
            FbTransaction parentTransaction = mock(FbTransaction.class);
            currentTransaction.set(parentTransaction);
            ResultSet resultSet = mock(ResultSet.class);
            ResultSetMetaData metadata = mock(ResultSetMetaData.class);
            mockQueryResult(resultSet, metadata, columnType);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_SCROLL_INSENSITIVE);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getObject(1)).thenReturn(expected);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            assertTrue(rows.next());
            clearInvocations(firebirdConnection.getGDSHelper());
            Object actual = rows.getObject(1);
            assertThat(actual, is(expected));
            assertThat(rows.getObject(1), is(expected));
            assertThat(currentTransaction.get(), sameInstance(parentTransaction));
            verify(firebirdConnection.getGDSHelper(), never()).setCurrentTransaction(nullable(FbTransaction.class));
            verify(metadata).getColumnType(1);
        });
    }
    
    private void mockQueryResult(final ResultSet resultSet, final ResultSetMetaData metadata, final int... columnTypes) throws SQLException {
        PreparedStatement statement = mock(PreparedStatement.class);
        when(pooledConnection.prepareStatement("foo_sql")).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(metadata);
        when(metadata.getColumnCount()).thenReturn(columnTypes.length);
        for (int index = 0; index < columnTypes.length; index++) {
            when(metadata.getColumnType(index + 1)).thenReturn(columnTypes[index]);
        }
    }
    
    private static Stream<Arguments> bufferedColumnArguments() {
        return Stream.of(
                Arguments.of("smallint", Types.SMALLINT, 1),
                Arguments.of("integer", Types.INTEGER, 1),
                Arguments.of("bigint", Types.BIGINT, 1L),
                Arguments.of("numeric", Types.NUMERIC, new BigDecimal("1")),
                Arguments.of("decimal", Types.DECIMAL, new BigDecimal("1")),
                Arguments.of("float", Types.FLOAT, 1.0D),
                Arguments.of("double", Types.DOUBLE, 1.0D),
                Arguments.of("char", Types.CHAR, "foo_value"),
                Arguments.of("varchar", Types.VARCHAR, "foo_value"),
                Arguments.of("date", Types.DATE, new Date(0)),
                Arguments.of("time", Types.TIME, new Time(0)),
                Arguments.of("timestamp", Types.TIMESTAMP, new Timestamp(0)),
                Arguments.of("timestamp_tz", Types.TIMESTAMP_WITH_TIMEZONE, OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)),
                Arguments.of("time_tz", Types.TIME_WITH_TIMEZONE, OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC)),
                Arguments.of("binary", Types.BINARY, new byte[]{1}),
                Arguments.of("varbinary", Types.VARBINARY, new byte[]{1}),
                Arguments.of("row_id", Types.ROWID, new FBRowId(new byte[8])),
                Arguments.of("decfloat", JaybirdTypeCodes.DECFLOAT, Decimal64.valueOf(1)));
    }
    
    @Test
    void assertBufferedNullColumn() {
        withJdbcConnections(jdbcConnections -> {
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.NULL);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_SCROLL_INSENSITIVE);
            when(resultSet.next()).thenReturn(true);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            assertTrue(rows.next());
            clearInvocations(firebirdConnection.getGDSHelper());
            assertNull(rows.getObject(1));
            verify(firebirdConnection.getGDSHelper(), never()).setCurrentTransaction(nullable(FbTransaction.class));
        });
    }
    
    @Test
    void assertBufferedBooleanColumn() {
        withJdbcConnections(jdbcConnections -> {
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.BOOLEAN);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_SCROLL_INSENSITIVE);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getObject(1)).thenReturn(true);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            assertTrue(rows.next());
            clearInvocations(firebirdConnection.getGDSHelper());
            assertTrue((Boolean) rows.getObject(1));
            verify(firebirdConnection.getGDSHelper(), never()).setCurrentTransaction(nullable(FbTransaction.class));
        });
    }
    
    @Test
    void assertBufferedCursorReads() {
        withJdbcConnections(jdbcConnections -> {
            FbTransaction parentTransaction = mock(FbTransaction.class);
            currentTransaction.set(parentTransaction);
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
            when(resultSet.getConcurrency()).thenReturn(ResultSet.CONCUR_READ_ONLY);
            when(firstTransaction.getState()).thenReturn(TransactionState.ACTIVE);
            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getObject(1)).thenReturn(1);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            clearInvocations(firebirdConnection.getGDSHelper());
            assertTrue(rows.next());
            assertThat(rows.getObject(1), is(1));
            assertFalse(rows.next());
            assertThat(currentTransaction.get(), sameInstance(parentTransaction));
            verify(firebirdConnection.getGDSHelper(), never()).setCurrentTransaction(nullable(FbTransaction.class));
        });
    }
    
    @Test
    void assertReadOnlyScalarReadCanFinishDuringClose() {
        withJdbcConnections(jdbcConnections -> {
            FbTransaction parentTransaction = mock(FbTransaction.class);
            currentTransaction.set(parentTransaction);
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
            when(resultSet.getConcurrency()).thenReturn(ResultSet.CONCUR_READ_ONLY);
            when(firstTransaction.getState()).thenReturn(TransactionState.ACTIVE);
            when(resultSet.next()).thenReturn(true);
            CountDownLatch reading = new CountDownLatch(1);
            CountDownLatch complete = new CountDownLatch(1);
            when(resultSet.getObject(1)).thenAnswer(invocation -> {
                reading.countDown();
                assertTrue(complete.await(5, TimeUnit.SECONDS));
                return 1;
            });
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            Connection sibling = FirebirdTransactionConnection.createTransaction(attachment, 2, false, TransactionIsolationLevel.READ_COMMITTED);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            assertTrue(rows.next());
            ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                Future<Object> actual = executor.submit(() -> rows.getObject(1));
                assertTrue(reading.await(5, TimeUnit.SECONDS));
                Future<?> closing = executor.submit(() -> {
                    connection.close();
                    return null;
                });
                closing.get(5, TimeUnit.SECONDS);
                assertTrue(connection.isClosed());
                assertFalse(sibling.isClosed());
                complete.countDown();
                assertThat(actual.get(5, TimeUnit.SECONDS), is(1));
                SQLException actualException = assertThrows(SQLException.class, () -> rows.getObject(1));
                assertThat(actualException.getSQLState(), is("25000"));
                assertThat(currentTransaction.get(), sameInstance(parentTransaction));
                verify(resultSet).getObject(1);
            } finally {
                complete.countDown();
                executor.shutdownNow();
            }
        });
    }
    
    @Test
    void assertUpdatableCursorUsesTransaction() {
        withJdbcConnections(jdbcConnections -> {
            FbTransaction parentTransaction = mock(FbTransaction.class);
            currentTransaction.set(parentTransaction);
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
            when(resultSet.getConcurrency()).thenReturn(ResultSet.CONCUR_UPDATABLE);
            when(resultSet.next()).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(firstTransaction));
                return false;
            });
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            assertFalse(rows.next());
            assertThat(currentTransaction.get(), sameInstance(parentTransaction));
        });
    }
    
    @Test
    void assertCursorWithEndedDriverTransaction() {
        withJdbcConnections(jdbcConnections -> {
            FbTransaction parentTransaction = mock(FbTransaction.class);
            currentTransaction.set(parentTransaction);
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
            when(resultSet.getConcurrency()).thenReturn(ResultSet.CONCUR_READ_ONLY);
            when(firstTransaction.getState()).thenReturn(TransactionState.COMMITTED);
            when(resultSet.next()).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(firstTransaction));
                return false;
            });
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            assertFalse(rows.next());
            assertThat(currentTransaction.get(), sameInstance(parentTransaction));
        });
    }
    
    @Test
    void assertCursorAfterAutoCommitChange() {
        withJdbcConnections(jdbcConnections -> {
            FbTransaction parentTransaction = mock(FbTransaction.class);
            currentTransaction.set(parentTransaction);
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
            when(resultSet.getConcurrency()).thenReturn(ResultSet.CONCUR_READ_ONLY);
            when(resultSet.next()).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(firstTransaction));
                return false;
            });
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            when(jdbcConnections.constructed().get(0).getAutoCommit()).thenReturn(true);
            connection.setAutoCommit(true);
            assertFalse(rows.next());
            assertThat(currentTransaction.get(), sameInstance(parentTransaction));
        });
    }
    
    @Test
    void assertMixedColumnOwnership() {
        withJdbcConnections(jdbcConnections -> {
            FbTransaction parentTransaction = mock(FbTransaction.class);
            currentTransaction.set(parentTransaction);
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER, Types.LONGVARBINARY);
            Blob blob = mock(Blob.class);
            when(resultSet.next()).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(firstTransaction));
                return true;
            });
            when(resultSet.getObject(1)).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(parentTransaction));
                return 1;
            });
            when(resultSet.getObject(2)).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(firstTransaction));
                return blob;
            });
            when(blob.getBytes(1L, 1)).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(firstTransaction));
                return new byte[]{1};
            });
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            assertTrue(rows.next());
            assertThat(rows.getObject(1), is(1));
            Blob actual = (Blob) rows.getObject(2);
            assertThat(actual.getBytes(1L, 1), is(new byte[]{1}));
            assertThat(currentTransaction.get(), sameInstance(parentTransaction));
        });
    }
    
    @Test
    void assertTypedBufferedColumn() {
        withJdbcConnections(jdbcConnections -> {
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
            when(resultSet.getConcurrency()).thenReturn(ResultSet.CONCUR_READ_ONLY);
            when(firstTransaction.getState()).thenReturn(TransactionState.ACTIVE);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getObject(1, Integer.class)).thenReturn(1);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            assertTrue(rows.next());
            clearInvocations(firebirdConnection.getGDSHelper());
            assertThat(rows.getObject(1, Integer.class), is(1));
            verify(firebirdConnection.getGDSHelper(), never()).setCurrentTransaction(nullable(FbTransaction.class));
        });
    }
    
    @Test
    void assertBufferedIntegerGetter() {
        withJdbcConnections(jdbcConnections -> {
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_SCROLL_INSENSITIVE);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getInt(1)).thenReturn(1);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            assertTrue(rows.next());
            clearInvocations(firebirdConnection.getGDSHelper());
            assertThat(rows.getInt(1), is(1));
            verify(firebirdConnection.getGDSHelper(), never()).setCurrentTransaction(nullable(FbTransaction.class));
        });
    }
    
    @Test
    void assertNamedColumnUsesTransaction() {
        withJdbcConnections(jdbcConnections -> {
            FbTransaction parentTransaction = mock(FbTransaction.class);
            currentTransaction.set(parentTransaction);
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.VARCHAR);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_SCROLL_INSENSITIVE);
            when(resultSet.next()).thenReturn(true);
            when(resultSet.getObject("foo_column")).thenAnswer(invocation -> {
                assertThat(currentTransaction.get(), sameInstance(firstTransaction));
                return "foo_value";
            });
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            assertTrue(rows.next());
            assertThat(rows.getObject("foo_column"), is("foo_value"));
            assertThat(currentTransaction.get(), sameInstance(parentTransaction));
        });
    }
    
    @Test
    void assertBufferedColumnWithZeroIndex() {
        withJdbcConnections(jdbcConnections -> {
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_SCROLL_INSENSITIVE);
            ArrayIndexOutOfBoundsException expected = new ArrayIndexOutOfBoundsException("foo_invalid_column");
            when(resultSet.getObject(0)).thenThrow(expected);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            ArrayIndexOutOfBoundsException actual = assertThrows(ArrayIndexOutOfBoundsException.class, () -> rows.getObject(0));
            assertThat(actual, sameInstance(expected));
        });
    }
    
    @Test
    void assertBufferedColumnAfterLastIndex() {
        withJdbcConnections(jdbcConnections -> {
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_SCROLL_INSENSITIVE);
            SQLException expected = new SQLException("foo_invalid_column");
            when(resultSet.getObject(2)).thenThrow(expected);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            SQLException actual = assertThrows(SQLException.class, () -> rows.getObject(2));
            assertThat(actual, sameInstance(expected));
        });
    }
    
    @Test
    void assertClosedBufferedResultSetRetainsSibling() {
        withJdbcConnections(jdbcConnections -> {
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
            when(resultSet.getConcurrency()).thenReturn(ResultSet.CONCUR_READ_ONLY);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            Connection sibling = FirebirdTransactionConnection.createTransaction(attachment, 2, false, TransactionIsolationLevel.READ_COMMITTED);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            connection.close();
            SQLException actual = assertThrows(SQLException.class, () -> rows.getObject(1));
            assertThat(actual.getSQLState(), is("25000"));
            assertThrows(SQLException.class, rows::next);
            assertFalse(sibling.isClosed());
            verify(resultSet, never()).getObject(1);
            verify(resultSet, never()).next();
        });
    }
    
    @Test
    void assertMetadataTransactionIsReused() throws SQLException {
        withJdbcConnections(jdbcConnections -> {
            when(database.startTransaction(any())).thenReturn(firstTransaction, metadataTransaction);
            when(pooledConnection.prepareStatement("foo_sql")).thenReturn(mock(PreparedStatement.class));
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            connection.prepareStatement("foo_sql").close();
            connection.prepareStatement("foo_sql").close();
            verify(database, times(2)).startTransaction(any());
            attachment.close();
            verify(firstTransaction).rollback();
            verify(metadataTransaction).rollback();
        });
    }
    
    @Test
    void assertDelegateExceptionIsPreserved() throws SQLException {
        withJdbcConnections(jdbcConnections -> {
            FbTransaction parentTransaction = mock(FbTransaction.class);
            currentTransaction.set(parentTransaction);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            SQLException expected = new SQLException("foo_failure");
            when(pooledConnection.prepareStatement("foo_sql")).thenThrow(expected);
            SQLException actual = assertThrows(SQLException.class, () -> connection.prepareStatement("foo_sql"));
            assertThat(actual, sameInstance(expected));
            assertThat(currentTransaction.get(), sameInstance(parentTransaction));
            attachment.close();
        });
    }
    
    @Test
    void assertClosedTransactionCannotExecute() throws SQLException {
        withJdbcConnections(jdbcConnections -> {
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            connection.close();
            connection.close();
            assertTrue(connection.isClosed());
            assertThrows(SQLException.class, () -> connection.prepareStatement("foo_sql"));
            verify(firstTransaction).rollback();
            verify(pooledConnection, never()).close();
            attachment.close();
        });
    }
    
    @Test
    void assertCancellationDoesNotWaitForExecutionLock() throws Exception {
        withJdbcConnections(jdbcConnections -> {
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(pooledConnection.prepareStatement("foo_sql")).thenReturn(statement);
            PreparedStatement actualStatement = connection.prepareStatement("foo_sql");
            CountDownLatch executing = new CountDownLatch(1);
            CountDownLatch complete = new CountDownLatch(1);
            when(statement.executeQuery()).thenAnswer(invocation -> {
                executing.countDown();
                assertTrue(complete.await(10, TimeUnit.SECONDS));
                return null;
            });
            ExecutorService executor = Executors.newFixedThreadPool(2);
            try {
                Future<ResultSet> execution = executor.submit(() -> actualStatement.executeQuery());
                assertTrue(executing.await(5, TimeUnit.SECONDS));
                Future<?> actual = executor.submit(() -> {
                    actualStatement.cancel();
                    return null;
                });
                actual.get(5, TimeUnit.SECONDS);
                verify(statement).cancel();
                complete.countDown();
                execution.get(5, TimeUnit.SECONDS);
            } finally {
                complete.countDown();
                executor.shutdownNow();
                attachment.close();
            }
        });
    }
    
    @Test
    void assertAttachmentClosesStatementsBeforeReturningConnection() throws SQLException {
        withJdbcConnections(jdbcConnections -> {
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(pooledConnection.prepareStatement("foo_sql")).thenReturn(statement);
            PreparedStatement actual = connection.prepareStatement("foo_sql");
            attachment.close();
            InOrder inOrder = inOrder(statement, pooledConnection);
            inOrder.verify(statement).close();
            inOrder.verify(pooledConnection).close();
            actual.close();
            actual.cancel();
            assertTrue(actual.isClosed());
            verify(statement).close();
            verify(statement, never()).cancel();
        });
    }
    
    @Test
    void assertAttachmentCleanupContinuesAfterStatementCloseFailure() throws SQLException {
        withJdbcConnections(jdbcConnections -> {
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            FirebirdTransactionConnection.createTransaction(attachment, 2, false, TransactionIsolationLevel.READ_COMMITTED);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(pooledConnection.prepareStatement("foo_sql")).thenReturn(statement);
            connection.prepareStatement("foo_sql");
            SQLException expected = new SQLException("foo_statement_failure");
            doThrow(expected).when(statement).close();
            SQLException actual = assertThrows(SQLException.class, attachment::close);
            assertThat(actual.getNextException().getNextException(), sameInstance(expected));
            InOrder inOrder = inOrder(statement, firstTransaction, secondTransaction, metadataTransaction, database, pooledConnection);
            inOrder.verify(statement).close();
            inOrder.verify(firstTransaction).rollback();
            inOrder.verify(secondTransaction).rollback();
            inOrder.verify(metadataTransaction).rollback();
            inOrder.verify(database).close();
            inOrder.verify(pooledConnection).close();
            assertTrue(attachment.isClosed());
        });
    }
    
    @Test
    void assertAttachmentCleanupContinuesAfterMetadataRollbackFailure() throws SQLException {
        withJdbcConnections(jdbcConnections -> {
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            FirebirdTransactionConnection.createTransaction(attachment, 2, false, TransactionIsolationLevel.READ_COMMITTED);
            PreparedStatement statement = mock(PreparedStatement.class);
            when(pooledConnection.prepareStatement("foo_sql")).thenReturn(statement);
            connection.prepareStatement("foo_sql");
            SQLException expected = new SQLException("foo_metadata_failure");
            doThrow(expected).when(metadataTransaction).rollback();
            SQLException actual = assertThrows(SQLException.class, attachment::close);
            assertThat(actual.getNextException(), sameInstance(expected));
            InOrder inOrder = inOrder(statement, firstTransaction, secondTransaction, metadataTransaction, database, pooledConnection);
            inOrder.verify(statement).close();
            inOrder.verify(firstTransaction).rollback();
            inOrder.verify(secondTransaction).rollback();
            inOrder.verify(metadataTransaction).rollback();
            inOrder.verify(database).close();
            inOrder.verify(pooledConnection).close();
            assertTrue(attachment.isClosed());
        });
    }
    
    @Test
    void assertAttachmentCleanupContinuesAfterRollbackFailure() throws SQLException {
        withJdbcConnections(jdbcConnections -> {
            ResultSet resultSet = mock(ResultSet.class);
            mockQueryResult(resultSet, mock(ResultSetMetaData.class), Types.INTEGER);
            when(resultSet.getType()).thenReturn(ResultSet.TYPE_FORWARD_ONLY);
            when(resultSet.getConcurrency()).thenReturn(ResultSet.CONCUR_READ_ONLY);
            when(firstTransaction.getState()).thenReturn(TransactionState.ACTIVE);
            when(resultSet.next()).thenReturn(true);
            Connection connection = FirebirdTransactionConnection.createTransaction(attachment, 1, false, TransactionIsolationLevel.REPEATABLE_READ);
            FirebirdTransactionConnection.createTransaction(attachment, 2, false, TransactionIsolationLevel.READ_COMMITTED);
            ResultSet rows = connection.prepareStatement("foo_sql").executeQuery();
            assertTrue(rows.next());
            SQLException expected = new SQLException("foo_failure");
            doThrow(expected).when(firstTransaction).rollback();
            SQLException actual = assertThrows(SQLException.class, attachment::close);
            assertThat(actual.getNextException().getNextException(), sameInstance(expected));
            verify(secondTransaction).rollback();
            verify(metadataTransaction).rollback();
            verify(database).close();
            verify(pooledConnection).close();
            SQLException actualReadException = assertThrows(SQLException.class, () -> rows.getObject(1));
            assertThat(actualReadException.getSQLState(), is("25000"));
            verify(resultSet, never()).getObject(1);
        });
    }
}
