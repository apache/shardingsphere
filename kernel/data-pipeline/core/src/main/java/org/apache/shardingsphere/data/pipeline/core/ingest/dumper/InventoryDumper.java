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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPositionFactory;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.job.JobOperationType;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.util.JDBCStreamQueryUtil;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.ColumnValueReader;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.util.spi.PipelineTypedSPILoader;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Inventory dumper.
 */
@Slf4j
public final class InventoryDumper extends AbstractLifecycleExecutor implements Dumper {
    
    @Getter(AccessLevel.PROTECTED)
    private final InventoryDumperConfiguration dumperConfig;
    
    private final PipelineChannel channel;
    
    private final DataSource dataSource;
    
    private final PipelineSQLBuilder sqlBuilder;
    
    private final ColumnValueReader columnValueReader;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    private volatile Statement dumpStatement;
    
    public InventoryDumper(final InventoryDumperConfiguration dumperConfig, final PipelineChannel channel, final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader) {
        ShardingSpherePreconditions.checkState(StandardPipelineDataSourceConfiguration.class.equals(dumperConfig.getDataSourceConfig().getClass()),
                () -> new UnsupportedSQLOperationException("AbstractInventoryDumper only support StandardPipelineDataSourceConfiguration"));
        this.dumperConfig = dumperConfig;
        this.channel = channel;
        this.dataSource = dataSource;
        String databaseType = dumperConfig.getDataSourceConfig().getDatabaseType().getType();
        sqlBuilder = PipelineTypedSPILoader.getDatabaseTypedService(PipelineSQLBuilder.class, databaseType);
        columnValueReader = PipelineTypedSPILoader.findDatabaseTypedService(ColumnValueReader.class, databaseType).orElseGet(() -> new BasicColumnValueReader(databaseType));
        this.metaDataLoader = metaDataLoader;
    }
    
    @Override
    protected void runBlocking() {
        IngestPosition<?> position = dumperConfig.getPosition();
        if (position instanceof FinishedPosition) {
            log.info("Ignored because of already finished.");
            return;
        }
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName())), dumperConfig.getActualTableName());
        try (Connection connection = dataSource.getConnection()) {
            dump(tableMetaData, connection);
            log.info("Inventory dump done");
        } catch (final SQLException ex) {
            log.error("Inventory dump, ex caught, msg={}.", ex.getMessage());
            throw new IngestException("Inventory dump failed on " + dumperConfig.getActualTableName(), ex);
        } finally {
            channel.pushRecord(new FinishedRecord(new FinishedPosition()));
        }
    }
    
    private void dump(final PipelineTableMetaData tableMetaData, final Connection connection) throws SQLException {
        if (null != dumperConfig.getRateLimitAlgorithm()) {
            dumperConfig.getRateLimitAlgorithm().intercept(JobOperationType.SELECT, 1);
        }
        int batchSize = dumperConfig.getBatchSize();
        DatabaseType databaseType = dumperConfig.getDataSourceConfig().getDatabaseType();
        try (PreparedStatement preparedStatement = JDBCStreamQueryUtil.generateStreamQueryPreparedStatement(databaseType, connection, buildInventoryDumpSQL())) {
            dumpStatement = preparedStatement;
            if (!(databaseType instanceof MySQLDatabaseType)) {
                preparedStatement.setFetchSize(batchSize);
            }
            setParameters(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                while (resultSet.next()) {
                    channel.pushRecord(loadDataRecord(resultSet, resultSetMetaData, tableMetaData));
                    if (!isRunning()) {
                        log.info("Broke because of inventory dump is not running.");
                        break;
                    }
                }
                dumpStatement = null;
            }
        }
    }
    
    private String buildInventoryDumpSQL() {
        String schemaName = dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName()));
        if (!dumperConfig.hasUniqueKey()) {
            return sqlBuilder.buildNoUniqueKeyInventoryDumpSQL(schemaName, dumperConfig.getActualTableName());
        }
        PrimaryKeyPosition<?> position = (PrimaryKeyPosition<?>) dumperConfig.getPosition();
        PipelineColumnMetaData firstColumn = dumperConfig.getUniqueKeyColumns().get(0);
        if (PipelineJdbcUtils.isIntegerColumn(firstColumn.getDataType()) || PipelineJdbcUtils.isStringColumn(firstColumn.getDataType())) {
            if (null != position.getBeginValue() && null != position.getEndValue()) {
                return sqlBuilder.buildDivisibleInventoryDumpSQL(schemaName, dumperConfig.getActualTableName(), firstColumn.getName());
            }
            if (null != position.getBeginValue() && null == position.getEndValue()) {
                return sqlBuilder.buildDivisibleInventoryDumpSQLNoEnd(schemaName, dumperConfig.getActualTableName(), firstColumn.getName());
            }
        }
        return sqlBuilder.buildIndivisibleInventoryDumpSQL(schemaName, dumperConfig.getActualTableName(), firstColumn.getName());
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
        DataRecord result = new DataRecord(newPosition(resultSet), columnCount);
        result.setType(IngestDataChangeType.INSERT);
        result.setTableName(dumperConfig.getLogicTableName());
        for (int i = 1; i <= columnCount; i++) {
            result.addColumn(new Column(resultSetMetaData.getColumnName(i), columnValueReader.readValue(resultSet, resultSetMetaData, i), true, tableMetaData.getColumnMetaData(i).isUniqueKey()));
        }
        return result;
    }
    
    private IngestPosition<?> newPosition(final ResultSet resultSet) throws SQLException {
        return !dumperConfig.hasUniqueKey()
                ? new PlaceholderPosition()
                : PrimaryKeyPositionFactory.newInstance(resultSet.getObject(dumperConfig.getUniqueKeyColumns().get(0).getName()), ((PrimaryKeyPosition<?>) dumperConfig.getPosition()).getEndValue());
    }
    
    @Override
    protected void doStop() throws SQLException {
        cancelStatement(dumpStatement);
    }
}
