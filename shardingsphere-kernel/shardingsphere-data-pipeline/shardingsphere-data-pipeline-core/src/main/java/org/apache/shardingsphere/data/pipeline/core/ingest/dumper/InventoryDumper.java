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
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.exception.data.UnsupportedPipelineJobUniqueKeyDataTypeException;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.PipelineSQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.ColumnValueReader;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Optional;

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
    
    public InventoryDumper(final InventoryDumperConfiguration dumperConfig, final PipelineChannel channel, final DataSource dataSource, final PipelineTableMetaDataLoader metaDataLoader) {
        ShardingSpherePreconditions.checkState(StandardPipelineDataSourceConfiguration.class.equals(dumperConfig.getDataSourceConfig().getClass()),
                () -> new UnsupportedSQLOperationException("AbstractInventoryDumper only support StandardPipelineDataSourceConfiguration"));
        this.dumperConfig = dumperConfig;
        this.channel = channel;
        this.dataSource = dataSource;
        sqlBuilder = PipelineSQLBuilderFactory.getInstance(dumperConfig.getDataSourceConfig().getDatabaseType().getType());
        columnValueReader = ColumnValueReaderFactory.getInstance(dumperConfig.getDataSourceConfig().getDatabaseType().getType());
        this.metaDataLoader = metaDataLoader;
    }
    
    @Override
    protected void runBlocking() {
        String schemaName = dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName()));
        String firstSQL = sqlBuilder.buildInventoryDumpSQL(schemaName, dumperConfig.getActualTableName(), dumperConfig.getUniqueKey(), dumperConfig.getUniqueKeyDataType(), true);
        String laterSQL = sqlBuilder.buildInventoryDumpSQL(schemaName, dumperConfig.getActualTableName(), dumperConfig.getUniqueKey(), dumperConfig.getUniqueKeyDataType(), false);
        IngestPosition<?> position = dumperConfig.getPosition();
        log.info("Inventory dump, uniqueKeyDataType={}, firstSQL={}, laterSQL={}, position={}.", dumperConfig.getUniqueKeyDataType(), firstSQL, laterSQL, position);
        if (position instanceof FinishedPosition) {
            log.info("Ignored because of already finished.");
            return;
        }
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(dumperConfig.getSchemaName(new LogicTableName(dumperConfig.getLogicTableName())), dumperConfig.getActualTableName());
        Object beginUniqueKeyValue = ((PrimaryKeyPosition<?>) position).getBeginValue();
        int round = 1;
        try (Connection connection = dataSource.getConnection()) {
            Optional<Object> maxUniqueKeyValue;
            while ((maxUniqueKeyValue = dump(tableMetaData, connection, 1 == round ? firstSQL : laterSQL, beginUniqueKeyValue, round++)).isPresent()) {
                beginUniqueKeyValue = maxUniqueKeyValue.get();
                if (!isRunning()) {
                    log.info("Broke because of inventory dump is not running.");
                    break;
                }
            }
            log.info("Inventory dump done, round={}, maxUniqueKeyValue={}.", round, maxUniqueKeyValue);
        } catch (final SQLException ex) {
            log.error("Inventory dump, ex caught, msg={}.", ex.getMessage());
            throw new IngestException(ex);
        } finally {
            log.info("Inventory dump, before put FinishedRecord.");
            channel.pushRecord(new FinishedRecord(new FinishedPosition()));
        }
    }
    
    private Optional<Object> dump(final PipelineTableMetaData tableMetaData, final Connection connection, final String sql, final Object beginUniqueKeyValue, final int round) throws SQLException {
        if (null != dumperConfig.getRateLimitAlgorithm()) {
            dumperConfig.getRateLimitAlgorithm().intercept(JobOperationType.SELECT, 1);
        }
        int batchSize = dumperConfig.getBatchSize();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            setParameters(preparedStatement, batchSize, beginUniqueKeyValue);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int rowCount = 0;
                Object maxUniqueKeyValue = null;
                while (resultSet.next()) {
                    channel.pushRecord(loadDataRecord(resultSet, resultSetMetaData, tableMetaData));
                    maxUniqueKeyValue = columnValueReader.readValue(resultSet, resultSetMetaData, tableMetaData.getColumnMetaData(dumperConfig.getUniqueKey()).getOrdinalPosition());
                    rowCount++;
                    if (!isRunning()) {
                        log.info("Broke because of inventory dump is not running.");
                        break;
                    }
                }
                if (0 == round % 50) {
                    log.info("Dumping, round={}, rowCount={}, maxUniqueKeyValue={}.", round, rowCount, maxUniqueKeyValue);
                }
                return Optional.ofNullable(maxUniqueKeyValue);
            }
        }
    }
    
    private void setParameters(final PreparedStatement preparedStatement, final int batchSize, final Object beginUniqueKeyValue) throws SQLException {
        preparedStatement.setFetchSize(batchSize);
        if (PipelineJdbcUtils.isIntegerColumn(dumperConfig.getUniqueKeyDataType())) {
            preparedStatement.setObject(1, beginUniqueKeyValue);
            preparedStatement.setObject(2, ((PrimaryKeyPosition<?>) dumperConfig.getPosition()).getEndValue());
            preparedStatement.setInt(3, batchSize);
            return;
        }
        if (PipelineJdbcUtils.isStringColumn(dumperConfig.getUniqueKeyDataType())) {
            preparedStatement.setObject(1, beginUniqueKeyValue);
            preparedStatement.setInt(2, batchSize);
            return;
        }
        throw new UnsupportedPipelineJobUniqueKeyDataTypeException(dumperConfig.getUniqueKeyDataType());
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
        return null == dumperConfig.getUniqueKey()
                ? new PlaceholderPosition()
                : PrimaryKeyPositionFactory.newInstance(resultSet.getObject(dumperConfig.getUniqueKey()), ((PrimaryKeyPosition<?>) dumperConfig.getPosition()).getEndValue());
    }
    
    @Override
    protected void doStop() {
    }
}
