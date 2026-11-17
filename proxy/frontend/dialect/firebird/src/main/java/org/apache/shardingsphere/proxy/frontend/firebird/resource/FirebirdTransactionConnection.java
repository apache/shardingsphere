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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.util.TransactionUtils;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.TransactionState;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.JaybirdTypeCodes;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * JDBC transaction views sharing one Firebird attachment.
 */
@HighFrequencyInvocation
public final class FirebirdTransactionConnection implements InvocationHandler {
    
    private final Attachment attachment;
    
    private final int transactionId;
    
    private final FbTransaction transaction;
    
    private final boolean readOnly;
    
    private final TransactionIsolationLevel isolationLevel;
    
    private final FBConnection jdbcConnection;
    
    private final Connection connection;
    
    private final Set<Statement> statements = Collections.newSetFromMap(new IdentityHashMap<>());
    
    private volatile boolean active = true;
    
    private boolean closed;
    
    private FirebirdTransactionConnection(final Attachment attachment, final int transactionId, final boolean readOnly, final TransactionIsolationLevel isolationLevel) throws SQLException {
        this.attachment = attachment;
        this.transactionId = transactionId;
        this.readOnly = readOnly;
        this.isolationLevel = isolationLevel;
        jdbcConnection = new FBConnection(attachment.firebirdConnection.getManagedConnection());
        jdbcConnection.setManagedEnvironment(false);
        jdbcConnection.setAutoCommit(false);
        transaction = attachment.startTransaction(readOnly, isolationLevel);
        connection = (Connection) Proxy.newProxyInstance(FirebirdTransactionConnection.class.getClassLoader(), new Class<?>[]{Connection.class}, this);
    }
    
    /**
     * Wrap a pooled connection with attachment ownership.
     *
     * @param connection pooled connection or an existing attachment view
     * @return attachment owner
     * @throws SQLException SQL exception
     */
    public static Connection createAttachment(final Connection connection) throws SQLException {
        if (Proxy.isProxyClass(connection.getClass()) && Proxy.getInvocationHandler(connection) instanceof Attachment) {
            return connection;
        }
        return (Connection) Proxy.newProxyInstance(FirebirdTransactionConnection.class.getClassLoader(), new Class<?>[]{Connection.class}, new Attachment(connection));
    }
    
    /**
     * Get or start an independent transaction on an owned attachment.
     *
     * @param attachmentConnection connection returned by {@link #createAttachment(Connection)}
     * @param transactionId frontend transaction handle
     * @param readOnly read only flag
     * @param isolationLevel transaction isolation level
     * @return transaction connection owned by the attachment until closed
     * @throws SQLException SQL exception
     */
    public static Connection createTransaction(final Connection attachmentConnection, final int transactionId, final boolean readOnly,
                                               final TransactionIsolationLevel isolationLevel) throws SQLException {
        Attachment attachment = (Attachment) Proxy.getInvocationHandler(attachmentConnection);
        synchronized (attachment) {
            ShardingSpherePreconditions.checkState(!attachment.closed, () -> new SQLException("Firebird attachment is closed", "08003"));
            FirebirdTransactionConnection result = attachment.transactions.get(transactionId);
            if (null == result) {
                result = new FirebirdTransactionConnection(attachment, transactionId, readOnly, isolationLevel);
                attachment.transactions.put(transactionId, result);
            }
            return result.connection;
        }
    }
    
    /**
     * Close a transaction retained by an attachment, including a snapshot not yet used for SQL.
     *
     * @param connection attachment owner or a pooled connection without transaction views
     * @param transactionId frontend transaction handle
     * @throws SQLException SQL exception
     */
    public static void closeTransaction(final Connection connection, final int transactionId) throws SQLException {
        if (!Proxy.isProxyClass(connection.getClass()) || !(Proxy.getInvocationHandler(connection) instanceof Attachment)) {
            return;
        }
        Attachment attachment = (Attachment) Proxy.getInvocationHandler(connection);
        synchronized (attachment) {
            FirebirdTransactionConnection transaction = attachment.transactions.get(transactionId);
            if (null != transaction) {
                transaction.close();
            }
        }
    }
    
    private void close() throws SQLException {
        if (closed || attachment.closed) {
            return;
        }
        Collection<SQLException> exceptions = new LinkedList<>();
        for (Statement each : new ArrayList<>(statements)) {
            try {
                each.close();
                statements.remove(each);
            } catch (final SQLException ex) {
                exceptions.add(ex);
            }
        }
        try {
            finish(false);
        } catch (final SQLException ex) {
            exceptions.add(ex);
        }
        if (!active) {
            jdbcConnection.setManagedConnection(null);
        }
        if (!exceptions.isEmpty()) {
            SQLException result = new SQLException("Failed to close Firebird transaction resources");
            exceptions.forEach(result::setNextException);
            throw result;
        }
        closed = true;
        attachment.transactions.remove(transactionId);
    }
    
    private void finish(final boolean commit) throws SQLException {
        synchronized (attachment.cancellationLock) {
            if (!active) {
                return;
            }
            ShardingSpherePreconditions.checkState(!attachment.closed, () -> new SQLException("Firebird attachment is closed", "08003"));
            FbTransaction previousTransaction = attachment.firebirdConnection.getGDSHelper().getCurrentTransaction();
            attachment.firebirdConnection.getGDSHelper().setCurrentTransaction(transaction);
            try {
                if (commit) {
                    transaction.commit();
                } else {
                    transaction.rollback();
                }
                active = false;
                clearJdbcTransactionState(commit);
            } finally {
                attachment.firebirdConnection.getGDSHelper().setCurrentTransaction(previousTransaction);
            }
        }
    }
    
    private void clearJdbcTransactionState(final boolean commit) throws SQLException {
        if (commit) {
            jdbcConnection.commit();
        } else {
            jdbcConnection.rollback();
        }
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            return invokeObjectMethod(proxy, method, args);
        }
        synchronized (attachment) {
            switch (method.getName()) {
                case "close":
                    close();
                    return null;
                case "isClosed":
                    return closed || attachment.closed;
                case "commit":
                    finish(true);
                    return null;
                case "rollback":
                    if (null == args || 0 == args.length) {
                        finish(false);
                        return null;
                    }
                    break;
                case "isReadOnly":
                    return readOnly;
                case "getTransactionIsolation":
                    return TransactionUtils.getTransactionIsolationLevel(isolationLevel);
                default:
                    break;
            }
            return invokeJdbcObject(proxy, jdbcConnection, method, args);
        }
    }
    
    private static Object invokeObjectMethod(final Object proxy, final Method method, @Nullable final Object @Nullable [] args) {
        switch (method.getName()) {
            case "equals":
                return proxy == args[0];
            case "hashCode":
                return System.identityHashCode(proxy);
            default:
                return "Firebird JDBC transaction view";
        }
    }
    
    @Nullable
    private Object invokeJdbcObject(final Object proxy, final Object target, final Method method, @Nullable final Object @Nullable [] args) throws SQLException {
        if (target instanceof Statement && "cancel".equals(method.getName())) {
            synchronized (attachment.cancellationLock) {
                return attachment.closed || !active ? null : invokeDelegate(target, method, args);
            }
        }
        synchronized (attachment) {
            if (Object.class == method.getDeclaringClass()) {
                return invokeObjectMethod(proxy, method, args);
            }
            if ("getConnection".equals(method.getName())) {
                return connection;
            }
            boolean closing = "close".equals(method.getName()) || "isClosed".equals(method.getName());
            if (attachment.closed && closing) {
                return "isClosed".equals(method.getName()) ? true : null;
            }
            validateTransactionState(closing);
            boolean preparing = target instanceof Connection && ("prepareStatement".equals(method.getName()) || "prepareCall".equals(method.getName()));
            FbTransaction currentTransaction = active ? transaction : null;
            if (preparing || target instanceof ResultSetMetaData) {
                currentTransaction = attachment.getMetadataTransaction();
            }
            FbTransaction previousTransaction = attachment.firebirdConnection.getGDSHelper().getCurrentTransaction();
            attachment.firebirdConnection.getGDSHelper().setCurrentTransaction(currentTransaction);
            try {
                Object result = invokeDelegate(target, method, args);
                if (target instanceof Statement && "close".equals(method.getName())) {
                    statements.remove(target);
                }
                if ("unwrap".equals(method.getName())) {
                    return ((Class<?>) args[0]).isInstance(proxy) ? proxy : result;
                }
                return wrap(result);
            } finally {
                attachment.firebirdConnection.getGDSHelper().setCurrentTransaction(previousTransaction);
            }
        }
    }
    
    @Nullable
    @SneakyThrows(Throwable.class)
    private static Object invokeDelegate(final Object target, final Method method, @Nullable final Object @Nullable [] args) {
        try {
            return method.invoke(target, args);
        } catch (final InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
    
    private void validateTransactionState(final boolean closing) throws SQLException {
        ShardingSpherePreconditions.checkState(closing || active && !attachment.closed, () -> new SQLException("Firebird transaction is closed", "25000"));
    }
    
    @Nullable
    private Object wrap(@Nullable final Object target) throws SQLException {
        if (target instanceof Statement) {
            statements.add((Statement) target);
        }
        if (target instanceof CallableStatement) {
            return Proxy.newProxyInstance(FirebirdTransactionConnection.class.getClassLoader(), new Class<?>[]{CallableStatement.class},
                    (proxy, method, args) -> invokeJdbcObject(proxy, target, method, args));
        }
        if (target instanceof PreparedStatement) {
            return Proxy.newProxyInstance(FirebirdTransactionConnection.class.getClassLoader(), new Class<?>[]{PreparedStatement.class},
                    (proxy, method, args) -> invokeJdbcObject(proxy, target, method, args));
        }
        if (target instanceof Statement) {
            return Proxy.newProxyInstance(FirebirdTransactionConnection.class.getClassLoader(), new Class<?>[]{Statement.class},
                    (proxy, method, args) -> invokeJdbcObject(proxy, target, method, args));
        }
        if (target instanceof ResultSet) {
            return wrapResultSet((ResultSet) target);
        }
        if (target instanceof ResultSetMetaData) {
            return Proxy.newProxyInstance(FirebirdTransactionConnection.class.getClassLoader(), new Class<?>[]{ResultSetMetaData.class},
                    (proxy, method, args) -> invokeJdbcObject(proxy, target, method, args));
        }
        if (target instanceof Blob) {
            return Proxy.newProxyInstance(FirebirdTransactionConnection.class.getClassLoader(), new Class<?>[]{Blob.class}, (proxy, method, args) -> invokeJdbcObject(proxy, target, method, args));
        }
        if (target instanceof Clob) {
            return Proxy.newProxyInstance(FirebirdTransactionConnection.class.getClassLoader(), new Class<?>[]{Clob.class}, (proxy, method, args) -> invokeJdbcObject(proxy, target, method, args));
        }
        if (target instanceof DatabaseMetaData) {
            return Proxy.newProxyInstance(FirebirdTransactionConnection.class.getClassLoader(), new Class<?>[]{DatabaseMetaData.class},
                    (proxy, method, args) -> invokeJdbcObject(proxy, target, method, args));
        }
        return target;
    }
    
    private ResultSet wrapResultSet(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData metadata = resultSet.getMetaData();
        boolean[] bufferedColumns = new boolean[metadata.getColumnCount()];
        boolean bufferedColumnsOnly = true;
        for (int index = 0; index < bufferedColumns.length; index++) {
            bufferedColumns[index] = isBufferedType(metadata.getColumnType(index + 1));
            bufferedColumnsOnly &= bufferedColumns[index];
        }
        boolean bufferedCursor = bufferedColumnsOnly && ResultSet.TYPE_FORWARD_ONLY == resultSet.getType() && ResultSet.CONCUR_READ_ONLY == resultSet.getConcurrency();
        ResultSet delegate = (ResultSet) Proxy.newProxyInstance(FirebirdTransactionConnection.class.getClassLoader(), new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> invokeResultSetObject(proxy, resultSet, bufferedColumns, bufferedCursor, method, args));
        return bufferedCursor ? new BufferedResultSet(this, resultSet, delegate) : delegate;
    }
    
    private static boolean isBufferedType(final int columnType) {
        switch (columnType) {
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.BOOLEAN:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
            case Types.TIME_WITH_TIMEZONE:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.NULL:
            case Types.ROWID:
            case JaybirdTypeCodes.DECFLOAT:
                return true;
            default:
                return false;
        }
    }
    
    @Nullable
    private Object invokeResultSetObject(final Object proxy, final ResultSet resultSet, final boolean[] bufferedColumns, final boolean bufferedCursor, final Method method,
                                         @Nullable final Object @Nullable [] args) throws SQLException {
        if (bufferedCursor && "next".equals(method.getName())) {
            synchronized (attachment) {
                validateTransactionState(false);
                if (!jdbcConnection.getAutoCommit() && TransactionState.ACTIVE == transaction.getState()) {
                    return resultSet.next();
                }
            }
        }
        if (method.getName().startsWith("get") && method.getParameterCount() > 0 && args[0] instanceof Integer) {
            int columnIndex = (Integer) args[0];
            if (columnIndex > 0 && columnIndex <= bufferedColumns.length && bufferedColumns[columnIndex - 1]) {
                synchronized (attachment) {
                    validateTransactionState(false);
                    return "getObject".equals(method.getName()) && 1 == args.length ? resultSet.getObject(columnIndex) : invokeDelegate(resultSet, method, args);
                }
            }
        }
        return invokeJdbcObject(proxy, resultSet, method, args);
    }
    
    @HighFrequencyInvocation
    @RequiredArgsConstructor
    private static final class BufferedResultSet implements ResultSet {
        
        private final FirebirdTransactionConnection transactionConnection;
        
        private final ResultSet resultSet;
        
        @Delegate
        private final ResultSet delegate;
        
        @Override
        @Nullable
        public Object getObject(final int columnIndex) throws SQLException {
            transactionConnection.validateTransactionState(false);
            return resultSet.getObject(columnIndex);
        }
    }
    
    @HighFrequencyInvocation
    private static final class Attachment implements InvocationHandler {
        
        private final Connection connection;
        
        private final FBConnection firebirdConnection;
        
        private final Map<Integer, FirebirdTransactionConnection> transactions = new LinkedHashMap<>();
        
        private final Object cancellationLock = new Object();
        
        private FbTransaction metadataTransaction;
        
        private volatile boolean closed;
        
        private Attachment(final Connection connection) throws SQLException {
            this.connection = connection;
            firebirdConnection = connection.unwrap(FBConnection.class);
            connection.setAutoCommit(false);
        }
        
        private FbTransaction startTransaction(final boolean readOnly, final TransactionIsolationLevel isolationLevel) throws SQLException {
            TransactionParameterBuffer tpb = firebirdConnection.createTransactionParameterBuffer();
            tpb.addArgument(readOnly ? ISCConstants.isc_tpb_read : ISCConstants.isc_tpb_write);
            tpb.addArgument(ISCConstants.isc_tpb_wait);
            switch (isolationLevel) {
                case SERIALIZABLE:
                    tpb.addArgument(ISCConstants.isc_tpb_consistency);
                    break;
                case REPEATABLE_READ:
                    tpb.addArgument(ISCConstants.isc_tpb_concurrency);
                    break;
                default:
                    tpb.addArgument(ISCConstants.isc_tpb_read_committed);
                    tpb.addArgument(ISCConstants.isc_tpb_rec_version);
                    break;
            }
            return firebirdConnection.getFbDatabase().startTransaction(tpb);
        }
        
        private FbTransaction getMetadataTransaction() throws SQLException {
            if (null == metadataTransaction) {
                metadataTransaction = startTransaction(true, TransactionIsolationLevel.READ_COMMITTED);
            }
            return metadataTransaction;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            synchronized (this) {
                if (Object.class == method.getDeclaringClass()) {
                    return invokeObjectMethod(proxy, method, args);
                }
                if ("close".equals(method.getName())) {
                    synchronized (cancellationLock) {
                        close();
                    }
                    return null;
                }
                if ("isClosed".equals(method.getName())) {
                    return closed || connection.isClosed();
                }
                return invokeDelegate(connection, method, args);
            }
        }
        
        private void close() throws SQLException {
            if (closed) {
                return;
            }
            Collection<SQLException> exceptions = new ArrayList<>();
            for (FirebirdTransactionConnection each : new ArrayList<>(transactions.values())) {
                try {
                    each.close();
                } catch (final SQLException ex) {
                    exceptions.add(ex);
                }
            }
            if (null != metadataTransaction) {
                try {
                    metadataTransaction.rollback();
                } catch (final SQLException ex) {
                    exceptions.add(ex);
                }
            }
            try {
                if (!exceptions.isEmpty()) {
                    firebirdConnection.getFbDatabase().close();
                }
            } finally {
                closed = true;
                connection.close();
            }
            if (!exceptions.isEmpty()) {
                SQLException result = new SQLException("Failed to close Firebird attachment transactions");
                exceptions.forEach(result::setNextException);
                throw result;
            }
        }
    }
}
