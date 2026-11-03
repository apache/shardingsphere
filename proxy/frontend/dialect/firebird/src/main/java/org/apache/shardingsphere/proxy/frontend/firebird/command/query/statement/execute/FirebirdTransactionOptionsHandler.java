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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdErrorSegmentResolver;
import org.apache.shardingsphere.database.protocol.firebird.err.FirebirdStatusVector;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.LockMode;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.Option;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.Reservation;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.firebirdsql.gds.ClumpletReader;
import org.firebirdsql.gds.ClumpletReader.Kind;
import org.firebirdsql.gds.GDSExceptionHelper;
import org.firebirdsql.gds.GDSExceptionHelper.GDSMessage;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBSQLExceptionInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Applies a Firebird transaction's parameters to its backend connections.
 */
@RequiredArgsConstructor
final class FirebirdTransactionOptionsHandler {
    
    private static final int TPB_READ_CONSISTENCY = 22;
    
    private static final int TPB_AT_SNAPSHOT_NUMBER = 23;
    
    private static final int TPB_AUTO_RELEASE_TEMP_BLOBID = 24;
    
    private final FirebirdTransactionOptions options;
    
    private final Map<Connection, SavedParameters> previousParameters = new ConcurrentHashMap<>();
    
    /**
     * Start the backend transaction with the requested options.
     *
     * @param databaseName logical database name
     * @param dataSourceName storage unit name
     * @param connection backend connection
     * @throws SQLException SQL exception
     */
    void initialize(final String databaseName, final String dataSourceName, final Connection connection) throws SQLException {
        FBConnection firebird = connection.unwrap(FBConnection.class);
        previousParameters.put(connection, new SavedParameters(firebird, firebird.getManagedConnection().getTransactionParameters().deepCopy()));
        connection.setAutoCommit(false);
        firebird.setTransactionParameters(createParameters(databaseName, dataSourceName, firebird));
        firebird.getLocalTransaction().begin();
    }
    
    private TransactionParameterBuffer createParameters(final String databaseName, final String dataSourceName, final FBConnection connection) throws SQLException {
        TransactionParameterBuffer result = connection.createTransactionParameterBuffer();
        for (Option each : options.getOptions()) {
            addOption(result, each);
        }
        for (String each : options.getLockTimeouts()) {
            result.addArgument(ISCConstants.isc_tpb_lock_timeout, (int) parseNumber(each, 32767));
        }
        for (Reservation each : options.getReservations()) {
            addReservation(databaseName, dataSourceName, connection, result, each);
        }
        for (String each : options.getSnapshotNumbers()) {
            result.addArgument(TPB_AT_SNAPSHOT_NUMBER, parseNumber(each, Long.MAX_VALUE));
        }
        return result;
    }
    
    private void addOption(final TransactionParameterBuffer target, final Option option) {
        switch (option) {
            case READ_ONLY:
                target.addArgument(ISCConstants.isc_tpb_read);
                break;
            case READ_WRITE:
                target.addArgument(ISCConstants.isc_tpb_write);
                break;
            case WAIT:
                target.addArgument(ISCConstants.isc_tpb_wait);
                break;
            case NO_WAIT:
                target.addArgument(ISCConstants.isc_tpb_nowait);
                break;
            case SNAPSHOT:
            case REPEATABLE_READ:
                target.addArgument(ISCConstants.isc_tpb_concurrency);
                break;
            case SNAPSHOT_TABLE_STABILITY:
            case SERIALIZABLE:
                target.addArgument(ISCConstants.isc_tpb_consistency);
                break;
            case READ_COMMITTED:
            case READ_COMMITTED_NO_VERSION:
                target.addArgument(ISCConstants.isc_tpb_read_committed);
                target.addArgument(ISCConstants.isc_tpb_no_rec_version);
                break;
            case READ_COMMITTED_VERSION:
                target.addArgument(ISCConstants.isc_tpb_read_committed);
                target.addArgument(ISCConstants.isc_tpb_rec_version);
                break;
            case READ_COMMITTED_CONSISTENCY:
                target.addArgument(ISCConstants.isc_tpb_read_committed);
                target.addArgument(TPB_READ_CONSISTENCY);
                break;
            case NO_AUTO_UNDO:
                target.addArgument(ISCConstants.isc_tpb_no_auto_undo);
                break;
            case IGNORE_LIMBO:
                target.addArgument(ISCConstants.isc_tpb_ignore_limbo);
                break;
            case RESTART_REQUESTS:
                target.addArgument(ISCConstants.isc_tpb_restart_requests);
                break;
            case AUTO_COMMIT:
                target.addArgument(ISCConstants.isc_tpb_autocommit);
                break;
            case AUTO_RELEASE_TEMP_BLOBID:
                target.addArgument(TPB_AUTO_RELEASE_TEMP_BLOBID);
                break;
            case RESERVING:
            default:
                break;
        }
    }
    
    private long parseNumber(final String value, final long maximum) throws SQLException {
        try {
            long result = Long.parseLong(value);
            ShardingSpherePreconditions.checkState(result >= 0 && result <= maximum, () -> new SQLException("Invalid transaction option value", "42000"));
            return result;
        } catch (final NumberFormatException ex) {
            throw new SQLException("Invalid transaction option value", "42000", ex);
        }
    }
    
    private void addReservation(final String databaseName, final String dataSourceName, final FBConnection connection,
                                final TransactionParameterBuffer target, final Reservation reservation) throws SQLException {
        LockMode mode = reservation.getLockMode();
        boolean write = LockMode.WRITE == mode || LockMode.SHARED_WRITE == mode || LockMode.PROTECTED_WRITE == mode;
        boolean explicitlyShared = LockMode.SHARED_READ == mode || LockMode.SHARED_WRITE == mode;
        boolean protectedLock = LockMode.PROTECTED_READ == mode || LockMode.PROTECTED_WRITE == mode
                || !explicitlyShared && (options.getOptions().contains(Option.SNAPSHOT_TABLE_STABILITY) || options.getOptions().contains(Option.SERIALIZABLE));
        for (SimpleTableSegment each : reservation.getTables()) {
            addTableReservation(databaseName, dataSourceName, connection, target, each, write, protectedLock);
        }
    }
    
    private void addTableReservation(final String databaseName, final String dataSourceName, final FBConnection connection, final TransactionParameterBuffer target,
                                     final SimpleTableSegment table, final boolean write, final boolean protectedLock) throws SQLException {
        for (String each : getActualTables(databaseName, dataSourceName, table)) {
            target.addArgument(write ? ISCConstants.isc_tpb_lock_write : ISCConstants.isc_tpb_lock_read, each, connection.getFbDatabase().getEncoding());
            target.addArgument(protectedLock ? ISCConstants.isc_tpb_protected : ISCConstants.isc_tpb_shared);
        }
    }
    
    private Collection<String> getActualTables(final String databaseName, final String dataSourceName, final SimpleTableSegment table) {
        IdentifierValue identifier = table.getTableName().getIdentifier();
        String name = QuoteCharacter.NONE == identifier.getQuoteCharacter() ? identifier.getValue().toUpperCase(Locale.ROOT) : identifier.getValue().replace("\"\"", "\"");
        ShardingSphereDatabase database = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName);
        Set<String> result = new LinkedHashSet<>();
        boolean mapped = false;
        for (DataNodeRuleAttribute each : database.getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)) {
            mapped |= collectActualTables(each, name, dataSourceName, result);
        }
        if (!mapped) {
            result.add(name);
        }
        return result;
    }
    
    private boolean collectActualTables(final DataNodeRuleAttribute rule, final String name, final String dataSourceName, final Collection<String> target) {
        Collection<DataNode> dataNodes = rule.getDataNodesByTableName(name);
        for (DataNode each : dataNodes) {
            if (dataSourceName.equals(each.getDataSourceName())) {
                target.add(each.getTableName());
            }
        }
        return !dataNodes.isEmpty();
    }
    
    static boolean hasMultiplePhysicalDatabases(final Collection<StorageUnit> storageUnits) throws SQLException {
        if (storageUnits.size() < 2) {
            return false;
        }
        Iterator<StorageUnit> iterator = storageUnits.iterator();
        byte[] first = getPhysicalDatabaseIdentity(iterator.next());
        while (iterator.hasNext()) {
            if (!Arrays.equals(first, getPhysicalDatabaseIdentity(iterator.next()))) {
                return true;
            }
        }
        return false;
    }
    
    private static byte[] getPhysicalDatabaseIdentity(final StorageUnit storageUnit) throws SQLException {
        try (Connection connection = storageUnit.getDataSource().getConnection()) {
            byte[] response = connection.unwrap(FBConnection.class).getFbDatabase().getDatabaseInfo(
                    new byte[]{(byte) ISCConstants.fb_info_db_file_id, ISCConstants.isc_info_db_id, ISCConstants.isc_info_end}, 1024);
            ClumpletReader reader = new ClumpletReader(Kind.InfoResponse, response);
            if (!reader.find(ISCConstants.fb_info_db_file_id) || 0 == reader.getClumpLength() || !reader.find(ISCConstants.isc_info_db_id) || 0 == reader.getClumpLength()) {
                throw new SQLException("Could not identify the physical database for SNAPSHOT AT NUMBER", "HY000");
            }
            return response;
        }
    }
    
    static Optional<FirebirdStatusVector> resolveBackendFailure(final SQLException error, final Collection<String> snapshotNumbers) {
        if (!(error.getCause() instanceof FBSQLExceptionInfo)) {
            return Optional.empty();
        }
        SQLException detail = (SQLException) error.getCause();
        if (error.getErrorCode() != detail.getErrorCode()) {
            return Optional.empty();
        }
        if (ISCConstants.isc_bad_tpb_content == detail.getErrorCode() && null == detail.getNextException() && null != detail.getMessage()
                && detail.getMessage().endsWith("; isc_tpb_at_snapshot_number")
                && snapshotNumbers.stream().anyMatch(each -> each.chars().allMatch(character -> '0' == character))) {
            return Optional.of(FirebirdStatusVector.fromSegments(Collections.singletonList(FirebirdStatusVector.Segment.withArguments(
                    ISCConstants.isc_bad_tpb_content, Collections.singletonList(FirebirdStatusVector.Argument.ofString("isc_tpb_at_snapshot_number")), null))));
        }
        List<FirebirdStatusVector.Segment> segments = new ArrayList<>();
        for (SQLException each = detail; null != each; each = each.getNextException()) {
            if (!(each instanceof FBSQLExceptionInfo)) {
                return Optional.empty();
            }
            Optional<FirebirdStatusVector.Segment> reservationError = resolveReservationError(each);
            if (reservationError.isPresent()) {
                segments.add(reservationError.get());
            } else {
                segments.addAll(FirebirdErrorSegmentResolver.resolve(each.getErrorCode(), each.getMessage(), each.getSQLState()));
            }
        }
        return Optional.of(FirebirdStatusVector.fromSegments(segments));
    }
    
    private static Optional<FirebirdStatusVector.Segment> resolveReservationError(final SQLException error) {
        if (ISCConstants.isc_tpb_reserv_relnotfound != error.getErrorCode()) {
            return Optional.empty();
        }
        String marker = "" + (char) 1 + (char) 2;
        for (String each : Arrays.asList("isc_tpb_lock_read", "isc_tpb_lock_write")) {
            GDSMessage template = GDSExceptionHelper.getMessage(error.getErrorCode());
            template.setParameters(Arrays.asList(marker, each));
            String rendered = template.toString();
            int markerIndex = rendered.indexOf(marker);
            if (markerIndex < 0) {
                continue;
            }
            String prefix = rendered.substring(0, markerIndex);
            String suffix = rendered.substring(markerIndex + marker.length());
            String message = error.getMessage();
            if (message.length() >= prefix.length() + suffix.length() && message.startsWith(prefix) && message.endsWith(suffix)) {
                String table = message.substring(prefix.length(), message.length() - suffix.length());
                return Optional.of(FirebirdStatusVector.Segment.withArguments(error.getErrorCode(),
                        Arrays.asList(FirebirdStatusVector.Argument.ofString(table), FirebirdStatusVector.Argument.ofString(each)), null));
            }
        }
        return Optional.empty();
    }
    
    /**
     * Restore the connection's parameters before returning it to the pool.
     *
     * @param connection backend connection
     * @throws SQLException if the transaction or parameters cannot be restored
     */
    void release(final Connection connection) throws SQLException {
        SavedParameters previous = previousParameters.remove(connection);
        if (null == previous || previous.connection.isClosed()) {
            return;
        }
        try {
            if (previous.connection.getLocalTransaction().inTransaction()) {
                connection.rollback();
            }
            previous.connection.setTransactionParameters(previous.parameters);
        } catch (final SQLException ex) {
            abortOnFailure(previous.connection, ex);
            throw ex;
        }
    }
    
    private void abortOnFailure(final FBConnection connection, final SQLException cause) {
        try {
            connection.getFbDatabase().cancelOperation(ISCConstants.fb_cancel_abort);
        } catch (final SQLException ex) {
            cause.addSuppressed(ex);
        }
    }
    
    @RequiredArgsConstructor
    private static final class SavedParameters {
        
        private final FBConnection connection;
        
        private final TransactionParameterBuffer parameters;
    }
}
