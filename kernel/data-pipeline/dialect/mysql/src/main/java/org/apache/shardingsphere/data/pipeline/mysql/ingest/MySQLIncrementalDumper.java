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

package org.apache.shardingsphere.data.pipeline.mysql.ingest;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.execute.AbstractPipelineLifecycleRunnable;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveIdentifier;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineJdbcUtils;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogPosition;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.WriteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.ConnectInfo;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.MySQLClient;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.MySQLDataTypeHandler;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.string.MySQLBinaryString;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionProperties;
import org.apache.shardingsphere.infra.database.core.connector.ConnectionPropertiesParser;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * MySQL incremental dumper.
 */
@HighFrequencyInvocation
@Slf4j
public final class MySQLIncrementalDumper extends AbstractPipelineLifecycleRunnable implements IncrementalDumper {
    
    private final IncrementalDumperContext dumperContext;
    
    private final BinlogPosition binlogPosition;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    private final PipelineChannel channel;
    
    private final MySQLClient client;
    
    private final String catalog;
    
    public MySQLIncrementalDumper(final IncrementalDumperContext dumperContext, final IngestPosition binlogPosition,
                                  final PipelineChannel channel, final PipelineTableMetaDataLoader metaDataLoader) {
        Preconditions.checkArgument(dumperContext.getCommonContext().getDataSourceConfig() instanceof StandardPipelineDataSourceConfiguration,
                "MySQLBinlogDumper only support StandardPipelineDataSourceConfiguration");
        this.dumperContext = dumperContext;
        this.binlogPosition = (BinlogPosition) binlogPosition;
        this.channel = channel;
        this.metaDataLoader = metaDataLoader;
        StandardPipelineDataSourceConfiguration pipelineDataSourceConfig = (StandardPipelineDataSourceConfiguration) dumperContext.getCommonContext().getDataSourceConfig();
        ConnectionPropertiesParser parser = DatabaseTypedSPILoader.getService(ConnectionPropertiesParser.class, TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        ConnectionProperties connectionProps = parser.parse(pipelineDataSourceConfig.getUrl(), null, null);
        ConnectInfo connectInfo = new ConnectInfo(
                generateServerId(), connectionProps.getHostname(), connectionProps.getPort(), pipelineDataSourceConfig.getUsername(), pipelineDataSourceConfig.getPassword());
        log.info("incremental dump, jdbcUrl={}, serverId={}, hostname={}, port={}", pipelineDataSourceConfig.getUrl(), connectInfo.getServerId(), connectInfo.getHost(), connectInfo.getPort());
        client = new MySQLClient(connectInfo, dumperContext.isDecodeWithTX());
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
            List<AbstractBinlogEvent> events = client.poll();
            if (events.isEmpty()) {
                continue;
            }
            handleEvents(events);
        }
    }
    
    private void handleEvents(final List<AbstractBinlogEvent> events) {
        List<Record> dataRecords = new LinkedList<>();
        for (AbstractBinlogEvent each : events) {
            List<? extends Record> records = handleEvent(each);
            dataRecords.addAll(records);
        }
        if (dataRecords.isEmpty()) {
            return;
        }
        channel.push(dataRecords);
    }
    
    private List<? extends Record> handleEvent(final AbstractBinlogEvent event) {
        if (!(event instanceof AbstractRowsEvent)) {
            return Collections.singletonList(createPlaceholderRecord(event));
        }
        AbstractRowsEvent rowsEvent = (AbstractRowsEvent) event;
        if (!rowsEvent.getDatabaseName().equals(catalog) || !dumperContext.getCommonContext().getTableNameMapper().containsTable(rowsEvent.getTableName())) {
            return Collections.singletonList(createPlaceholderRecord(event));
        }
        PipelineTableMetaData tableMetaData = getPipelineTableMetaData(rowsEvent.getTableName());
        if (event instanceof WriteRowsEvent) {
            return handleWriteRowsEvent((WriteRowsEvent) event, tableMetaData);
        }
        if (event instanceof UpdateRowsEvent) {
            return handleUpdateRowsEvent((UpdateRowsEvent) event, tableMetaData);
        }
        if (event instanceof DeleteRowsEvent) {
            return handleDeleteRowsEvent((DeleteRowsEvent) event, tableMetaData);
        }
        return Collections.emptyList();
    }
    
    private PlaceholderRecord createPlaceholderRecord(final AbstractBinlogEvent event) {
        PlaceholderRecord result = new PlaceholderRecord(new BinlogPosition(event.getFileName(), event.getPosition(), event.getServerId()));
        result.setCommitTime(event.getTimestamp() * 1000L);
        return result;
    }
    
    private PipelineTableMetaData getPipelineTableMetaData(final String actualTableName) {
        CaseInsensitiveIdentifier logicTableName = dumperContext.getCommonContext().getTableNameMapper().getLogicTableName(actualTableName);
        return metaDataLoader.getTableMetaData(dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(logicTableName), actualTableName);
    }
    
    private List<DataRecord> handleWriteRowsEvent(final WriteRowsEvent event, final PipelineTableMetaData tableMetaData) {
        List<DataRecord> result = new LinkedList<>();
        for (Serializable[] each : event.getAfterRows()) {
            DataRecord dataRecord = createDataRecord(PipelineSQLOperationType.INSERT, event, each.length);
            for (int i = 0; i < each.length; i++) {
                PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(i + 1);
                dataRecord.addColumn(new Column(columnMetaData.getName(), handleValue(columnMetaData, each[i]), true, columnMetaData.isUniqueKey()));
            }
            result.add(dataRecord);
        }
        return result;
    }
    
    private List<DataRecord> handleUpdateRowsEvent(final UpdateRowsEvent event, final PipelineTableMetaData tableMetaData) {
        List<DataRecord> result = new LinkedList<>();
        for (int i = 0; i < event.getBeforeRows().size(); i++) {
            Serializable[] beforeValues = event.getBeforeRows().get(i);
            Serializable[] afterValues = event.getAfterRows().get(i);
            DataRecord dataRecord = createDataRecord(PipelineSQLOperationType.UPDATE, event, beforeValues.length);
            for (int j = 0; j < beforeValues.length; j++) {
                Serializable oldValue = beforeValues[j];
                Serializable newValue = afterValues[j];
                boolean updated = !Objects.deepEquals(newValue, oldValue);
                PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(j + 1);
                dataRecord.addColumn(new Column(columnMetaData.getName(),
                        handleValue(columnMetaData, oldValue),
                        handleValue(columnMetaData, newValue), updated, columnMetaData.isUniqueKey()));
            }
            result.add(dataRecord);
        }
        return result;
    }
    
    private List<DataRecord> handleDeleteRowsEvent(final DeleteRowsEvent event, final PipelineTableMetaData tableMetaData) {
        List<DataRecord> result = new LinkedList<>();
        for (Serializable[] each : event.getBeforeRows()) {
            DataRecord dataRecord = createDataRecord(PipelineSQLOperationType.DELETE, event, each.length);
            for (int i = 0, length = each.length; i < length; i++) {
                PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(i + 1);
                dataRecord.addColumn(new Column(columnMetaData.getName(), handleValue(columnMetaData, each[i]), null, true, columnMetaData.isUniqueKey()));
            }
            result.add(dataRecord);
        }
        return result;
    }
    
    private Serializable handleValue(final PipelineColumnMetaData columnMetaData, final Serializable value) {
        if (value instanceof MySQLBinaryString) {
            if (PipelineJdbcUtils.isBinaryColumn(columnMetaData.getDataType())) {
                return ((MySQLBinaryString) value).getBytes();
            }
            return new String(((MySQLBinaryString) value).getBytes(), Charset.defaultCharset());
        }
        Optional<MySQLDataTypeHandler> dataTypeHandler = TypedSPILoader.findService(MySQLDataTypeHandler.class, columnMetaData.getDataTypeName());
        return dataTypeHandler.isPresent() ? dataTypeHandler.get().handle(value) : value;
    }
    
    private DataRecord createDataRecord(final PipelineSQLOperationType type, final AbstractRowsEvent rowsEvent, final int columnCount) {
        String tableName = dumperContext.getCommonContext().getTableNameMapper().getLogicTableName(rowsEvent.getTableName()).toString();
        IngestPosition position = new BinlogPosition(rowsEvent.getFileName(), rowsEvent.getPosition(), rowsEvent.getServerId());
        DataRecord result = new DataRecord(type, tableName, position, columnCount);
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
