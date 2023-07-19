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

package org.apache.shardingsphere.data.pipeline.core.dumper;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.common.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.PrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.PrimaryKeyPositionFactory;
import org.apache.shardingsphere.data.pipeline.common.sqlbuilder.PipelineInventoryDumpSQLBuilder;
import org.apache.shardingsphere.data.pipeline.common.util.JDBCStreamQueryUtils;
import org.apache.shardingsphere.data.pipeline.common.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Inventory dumper.
 */
@Slf4j
public final class InventoryDumper extends AbstractLifecycleExecutor implements Dumper {
    
    @Getter(AccessLevel.PROTECTED)
    private final InventoryDumperConfiguration dumperConfig;
    
    private final PipelineChannel channel;
    
    private final DataSource dataSource;
    
    private final PipelineInventoryDumpSQLBuilder inventoryDumpSQLBuilder;
    
    private final ColumnValueReaderEngine columnValueReaderEngine;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    private final AtomicReference<Statement> dumpStatement = new AtomicReference<>();
    
    public InventoryDumper(final InventoryDumperConfiguration dumperConfig, final PipelineChannel channel, final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader) {
        this.dumperConfig = dumperConfig;
        this.channel = channel;
        this.dataSource = dataSource;
        DatabaseType databaseType = dumperConfig.getDataSourceConfig().getDatabaseType();
        inventoryDumpSQLBuilder = new PipelineInventoryDumpSQLBuilder(databaseType);
        columnValueReaderEngine = new ColumnValueReaderEngine(databaseType);
        this.metaDataLoader = metaDataLoader;
    }
    
    @Override
    protected void runBlocking() {
        IngestPosition position = dumperConfig.getPosition();
        if (position instanceof FinishedPosition) {
            log.info("Ignored because of already finished.");
            return;
        }
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName())), dumperConfig.getActualTableName());
        try (Connection connection = dataSource.getConnection()) {
            dump(tableMetaData, connection);
        } catch (final SQLException ex) {
            log.error("Inventory dump, ex caught, msg={}.", ex.getMessage());
            throw new IngestException("Inventory dump failed on " + dumperConfig.getActualTableName(), ex);
        }
    }
    
    @SuppressWarnings("MagicConstant")
    private void dump(final PipelineTableMetaData tableMetaData, final Connection connection) throws SQLException {
        int batchSize = dumperConfig.getBatchSize();
        DatabaseType databaseType = dumperConfig.getDataSourceConfig().getDatabaseType();
        if (null != dumperConfig.getTransactionIsolation()) {
            connection.setTransactionIsolation(dumperConfig.getTransactionIsolation());
        }
        try (PreparedStatement preparedStatement = JDBCStreamQueryUtils.generateStreamQueryPreparedStatement(databaseType, connection, buildInventoryDumpSQL())) {
            dumpStatement.set(preparedStatement);
            if (!(databaseType instanceof MySQLDatabaseType)) {
                preparedStatement.setFetchSize(batchSize);
            }
            setParameters(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                int rowCount = 0;
                JobRateLimitAlgorithm rateLimitAlgorithm = dumperConfig.getRateLimitAlgorithm();
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                List<Record> dataRecords = new LinkedList<>();
                while (resultSet.next()) {
                    if (dataRecords.size() >= batchSize) {
                        channel.pushRecords(dataRecords);
                        dataRecords = new LinkedList<>();
                    }
                    dataRecords.add(loadDataRecord(resultSet, resultSetMetaData, tableMetaData));
                    ++rowCount;
                    if (!isRunning()) {
                        log.info("Broke because of inventory dump is not running.");
                        break;
                    }
                    if (null != rateLimitAlgorithm && 0 == rowCount % batchSize) {
                        rateLimitAlgorithm.intercept(JobOperationType.SELECT, 1);
                    }
                }
                dataRecords.add(new FinishedRecord(new FinishedPosition()));
                channel.pushRecords(dataRecords);
                dumpStatement.set(null);
                log.info("Inventory dump done, rowCount={}", rowCount);
            }
        }
    }
    
    private String buildInventoryDumpSQL() {
        if (!Strings.isNullOrEmpty(dumperConfig.getQuerySQL())) {
            return dumperConfig.getQuerySQL();
        }
        LogicTableName logicTableName = new LogicTableName(dumperConfig.getLogicTableName());
        String schemaName = dumperConfig.getSchemaName(logicTableName);
        if (!dumperConfig.hasUniqueKey()) {
            return inventoryDumpSQLBuilder.buildFetchAllSQL(schemaName, dumperConfig.getActualTableName());
        }
        PrimaryKeyPosition<?> primaryKeyPosition = (PrimaryKeyPosition<?>) dumperConfig.getPosition();
        PipelineColumnMetaData firstColumn = dumperConfig.getUniqueKeyColumns().get(0);
        List<String> columnNames = dumperConfig.getColumnNames(logicTableName).orElse(Collections.singletonList("*"));
        if (PipelineJdbcUtils.isIntegerColumn(firstColumn.getDataType()) || PipelineJdbcUtils.isStringColumn(firstColumn.getDataType())) {
            if (null != primaryKeyPosition.getBeginValue() && null != primaryKeyPosition.getEndValue()) {
                return inventoryDumpSQLBuilder.buildDivisibleSQL(schemaName, dumperConfig.getActualTableName(), columnNames, firstColumn.getName());
            }
            if (null != primaryKeyPosition.getBeginValue() && null == primaryKeyPosition.getEndValue()) {
                return inventoryDumpSQLBuilder.buildUnlimitedDivisibleSQL(schemaName, dumperConfig.getActualTableName(), columnNames, firstColumn.getName());
            }
        }
        return inventoryDumpSQLBuilder.buildIndivisibleSQL(schemaName, dumperConfig.getActualTableName(), columnNames, firstColumn.getName());
    }
    
    private void setParameters(final PreparedStatement preparedStatement) throws SQLException {
        if (!dumperConfig.hasUniqueKey()) {
            return;
        }
        PipelineColumnMetaData firstColumn = dumperConfig.getUniqueKeyColumns().get(0);
        PrimaryKeyPosition<?> position = (PrimaryKeyPosition<?>) dumperConfig.getPosition();
        if (PipelineJdbcUtils.isIntegerColumn(firstColumn.getDataType()) && null != position.getBeginValue() && null != position.getEndValue()) {
            preparedStatement.setObject(1, position.getBeginValue());
            preparedStatement.setObject(2, position.getEndValue());
            return;
        }
        if (PipelineJdbcUtils.isStringColumn(firstColumn.getDataType())) {
            if (null != position.getBeginValue()) {
                preparedStatement.setObject(1, position.getBeginValue());
            }
            if (null != position.getEndValue()) {
                preparedStatement.setObject(2, position.getEndValue());
            }
        }
    }
    
    private DataRecord loadDataRecord(final ResultSet resultSet, final ResultSetMetaData resultSetMetaData, final PipelineTableMetaData tableMetaData) throws SQLException {
        int columnCount = resultSetMetaData.getColumnCount();
        DataRecord result = new DataRecord(IngestDataChangeType.INSERT, dumperConfig.getLogicTableName(), newPosition(resultSet), columnCount);
        List<String> insertColumnNames = Optional.ofNullable(dumperConfig.getInsertColumnNames()).orElse(Collections.emptyList());
        ShardingSpherePreconditions.checkState(insertColumnNames.isEmpty() || insertColumnNames.size() == resultSetMetaData.getColumnCount(),
                () -> new PipelineInvalidParameterException("Insert colum names count not equals ResultSet column count"));
        for (int i = 1; i <= columnCount; i++) {
            String columnName = insertColumnNames.isEmpty() ? resultSetMetaData.getColumnName(i) : insertColumnNames.get(i - 1);
            ShardingSpherePreconditions.checkNotNull(tableMetaData.getColumnMetaData(columnName), () -> new PipelineInvalidParameterException(String.format("Column name is %s", columnName)));
            boolean isUniqueKey = tableMetaData.getColumnMetaData(columnName).isUniqueKey();
            result.addColumn(new Column(columnName, columnValueReaderEngine.read(resultSet, resultSetMetaData, i), true, isUniqueKey));
        }
        return result;
    }
    
    private IngestPosition newPosition(final ResultSet resultSet) throws SQLException {
        return dumperConfig.hasUniqueKey()
                ? PrimaryKeyPositionFactory.newInstance(resultSet.getObject(dumperConfig.getUniqueKeyColumns().get(0).getName()), ((PrimaryKeyPosition<?>) dumperConfig.getPosition()).getEndValue())
                : new PlaceholderPosition();
    }
    
    @Override
    protected void doStop() {
        PipelineJdbcUtils.cancelStatement(dumpStatement.get());
    }
}
