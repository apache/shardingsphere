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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.dumper;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.execute.AbstractPipelineLifecycleRunnable;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.data.MySQLBinlogDataHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.MySQLBaseBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLBaseRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLDeleteRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLUpdateRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLWriteRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.position.MySQLBinlogPosition;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.ConnectInfo;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.MySQLBinlogClient;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionPropertiesParser;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * MySQL incremental dumper.
 */
@HighFrequencyInvocation
@Slf4j
public final class MySQLIncrementalDumper extends AbstractPipelineLifecycleRunnable implements IncrementalDumper {
    
    private final IncrementalDumperContext dumperContext;
    
    private final MySQLBinlogPosition binlogPosition;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    private final PipelineChannel channel;
    
    private final MySQLBinlogClient client;
    
    private final String catalog;
    
    public MySQLIncrementalDumper(final IncrementalDumperContext dumperContext, final IngestPosition binlogPosition, final PipelineChannel channel, final PipelineTableMetaDataLoader metaDataLoader) {
        this.dumperContext = dumperContext;
        this.binlogPosition = (MySQLBinlogPosition) binlogPosition;
        this.channel = channel;
        this.metaDataLoader = metaDataLoader;
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = (StandardPipelineDataSourceConfiguration) dumperContext.getCommonContext().getDataSourceConfig();
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ConnectionProperties connectionProps = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, databaseType).parse(pipelineDataSourceConfig.getUrl(), null, null);
        ConnectInfo connectInfo = new ConnectInfo(
                generateServerId(), connectionProps.getHostname(), connectionProps.getPort(), pipelineDataSourceConfig.getUsername(), pipelineDataSourceConfig.getPassword());
        log.info("incremental dump, jdbcUrl={}, serverId={}, hostname={}, port={}", pipelineDataSourceConfig.getUrl(), connectInfo.getServerId(), connectInfo.getHost(), connectInfo.getPort());
        client = new MySQLBinlogClient(connectInfo, dumperContext.isDecodeWithTX());
        catalog = connectionProps.getCatalog();
    }
    
    private int generateServerId() {
        int result = hashCode();
        return Integer.MIN_VALUE == result ? Integer.MAX_VALUE : Math.abs(result);
    }
    
    @Override
    protected void runBlocking() {
        client.connect();
        client.subscribe(binlogPosition.getFilename(), binlogPosition.getPosition());
        while (isRunning()) {
            handleEvents(client.poll());
        }
    }
    
    private void handleEvents(final List<MySQLBaseBinlogEvent> events) {
        List<Record> dataRecords = new LinkedList<>();
        for (MySQLBaseBinlogEvent each : events) {
            dataRecords.addAll(handleEvent(each));
        }
        if (!dataRecords.isEmpty()) {
            channel.push(dataRecords);
        }
    }
    
    private List<? extends Record> handleEvent(final MySQLBaseBinlogEvent event) {
        if (!(event instanceof MySQLBaseRowsBinlogEvent)) {
            return Collections.singletonList(createPlaceholderRecord(event));
        }
        MySQLBaseRowsBinlogEvent rowsEvent = (MySQLBaseRowsBinlogEvent) event;
        if (!rowsEvent.getDatabaseName().equals(catalog) || !dumperContext.getCommonContext().getTableNameMapper().containsTable(rowsEvent.getTableName())) {
            return Collections.singletonList(createPlaceholderRecord(event));
        }
        PipelineTableMetaData tableMetaData = getPipelineTableMetaData(rowsEvent.getTableName());
        if (event instanceof MySQLWriteRowsBinlogEvent) {
            return handleWriteRowsEvent((MySQLWriteRowsBinlogEvent) event, tableMetaData);
        }
        if (event instanceof MySQLUpdateRowsBinlogEvent) {
            return handleUpdateRowsEvent((MySQLUpdateRowsBinlogEvent) event, tableMetaData);
        }
        if (event instanceof MySQLDeleteRowsBinlogEvent) {
            return handleDeleteRowsEvent((MySQLDeleteRowsBinlogEvent) event, tableMetaData);
        }
        return Collections.emptyList();
    }
    
    private PlaceholderRecord createPlaceholderRecord(final MySQLBaseBinlogEvent event) {
        PlaceholderRecord result = new PlaceholderRecord(new MySQLBinlogPosition(event.getFileName(), event.getPosition()));
        result.setCommitTime(event.getTimestamp() * 1000L);
        return result;
    }
    
    private PipelineTableMetaData getPipelineTableMetaData(final String actualTableName) {
        ShardingSphereIdentifier logicTableName = dumperContext.getCommonContext().getTableNameMapper().getLogicTableName(actualTableName);
        return metaDataLoader.getTableMetaData(dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(logicTableName), actualTableName);
    }
    
    private List<DataRecord> handleWriteRowsEvent(final MySQLWriteRowsBinlogEvent event, final PipelineTableMetaData tableMetaData) {
        List<DataRecord> result = new LinkedList<>();
        for (Serializable[] each : event.getAfterRows()) {
            DataRecord dataRecord = createDataRecord(PipelineSQLOperationType.INSERT, event, each.length);
            for (int i = 0; i < each.length; i++) {
                PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(i + 1);
                dataRecord.addColumn(new NormalColumn(columnMetaData.getName(), MySQLBinlogDataHandler.handle(columnMetaData, each[i]), true, columnMetaData.isUniqueKey()));
            }
            result.add(dataRecord);
        }
        return result;
    }
    
    private List<DataRecord> handleUpdateRowsEvent(final MySQLUpdateRowsBinlogEvent event, final PipelineTableMetaData tableMetaData) {
        List<DataRecord> result = new LinkedList<>();
        for (int i = 0; i < event.getBeforeRows().size(); i++) {
            Serializable[] beforeValues = event.getBeforeRows().get(i);
            Serializable[] afterValues = event.getAfterRows().get(i);
            DataRecord dataRecord = createDataRecord(PipelineSQLOperationType.UPDATE, event, beforeValues.length);
            for (int j = 0; j < beforeValues.length; j++) {
                PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(j + 1);
                Serializable oldValue = MySQLBinlogDataHandler.handle(columnMetaData, beforeValues[j]);
                Serializable newValue = MySQLBinlogDataHandler.handle(columnMetaData, afterValues[j]);
                boolean updated = !Objects.deepEquals(newValue, oldValue);
                dataRecord.addColumn(new NormalColumn(columnMetaData.getName(), oldValue, newValue, updated, columnMetaData.isUniqueKey()));
            }
            result.add(dataRecord);
        }
        return result;
    }
    
    private List<DataRecord> handleDeleteRowsEvent(final MySQLDeleteRowsBinlogEvent event, final PipelineTableMetaData tableMetaData) {
        List<DataRecord> result = new LinkedList<>();
        for (Serializable[] each : event.getBeforeRows()) {
            DataRecord dataRecord = createDataRecord(PipelineSQLOperationType.DELETE, event, each.length);
            for (int i = 0, length = each.length; i < length; i++) {
                PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(i + 1);
                dataRecord.addColumn(new NormalColumn(columnMetaData.getName(), MySQLBinlogDataHandler.handle(columnMetaData, each[i]), null, true, columnMetaData.isUniqueKey()));
            }
            result.add(dataRecord);
        }
        return result;
    }
    
    private DataRecord createDataRecord(final PipelineSQLOperationType type, final MySQLBaseRowsBinlogEvent rowsEvent, final int columnCount) {
        String tableName = dumperContext.getCommonContext().getTableNameMapper().getLogicTableName(rowsEvent.getTableName()).toString();
        IngestPosition binlogPosition = new MySQLBinlogPosition(rowsEvent.getFileName(), rowsEvent.getPosition());
        DataRecord result = new DataRecord(type, tableName, binlogPosition, columnCount);
        result.setActualTableName(rowsEvent.getTableName());
        result.setCommitTime(rowsEvent.getTimestamp() * 1000L);
        return result;
    }
    
    @Override
    protected void doStop() {
        if (null != client) {
            client.closeChannel();
        }
    }
}
