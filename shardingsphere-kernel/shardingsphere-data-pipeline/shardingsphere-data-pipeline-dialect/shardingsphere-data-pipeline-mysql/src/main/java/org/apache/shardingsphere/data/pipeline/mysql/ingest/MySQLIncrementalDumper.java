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
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlJdbcConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.AbstractIncrementalDumper;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogPosition;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.WriteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.ConnectInfo;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.MySQLClient;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.MySQLDataTypeHandler;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.MySQLDataTypeHandlerFactory;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * MySQL incremental dumper.
 */
@Slf4j
public final class MySQLIncrementalDumper extends AbstractIncrementalDumper<BinlogPosition> {
    
    private final BinlogPosition binlogPosition;
    
    private final DumperConfiguration dumperConfig;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    private final Random random = new SecureRandom();
    
    private final PipelineChannel channel;
    
    private final MySQLClient client;
    
    private final String catalog;
    
    public MySQLIncrementalDumper(final DumperConfiguration dumperConfig, final IngestPosition<BinlogPosition> binlogPosition,
                                  final PipelineChannel channel, final PipelineTableMetaDataLoader metaDataLoader) {
        super(dumperConfig, binlogPosition, channel, metaDataLoader);
        this.binlogPosition = (BinlogPosition) binlogPosition;
        this.dumperConfig = dumperConfig;
        Preconditions.checkArgument(dumperConfig.getDataSourceConfig() instanceof StandardPipelineDataSourceConfiguration, "MySQLBinlogDumper only support StandardPipelineDataSourceConfiguration");
        this.channel = channel;
        this.metaDataLoader = metaDataLoader;
        YamlJdbcConfiguration jdbcConfig = ((StandardPipelineDataSourceConfiguration) dumperConfig.getDataSourceConfig()).getJdbcConfig();
        log.info("incremental dump, jdbcUrl={}", jdbcConfig.getJdbcUrl());
        DataSourceMetaData metaData = DatabaseTypeFactory.getInstance("MySQL").getDataSourceMetaData(jdbcConfig.getJdbcUrl(), null);
        client = new MySQLClient(new ConnectInfo(random.nextInt(), metaData.getHostname(), metaData.getPort(), jdbcConfig.getUsername(), jdbcConfig.getPassword()));
        catalog = metaData.getCatalog();
    }
    
    @Override
    protected void runBlocking() {
        dump();
    }
    
    private void dump() {
        client.connect();
        client.subscribe(binlogPosition.getFilename(), binlogPosition.getPosition());
        int eventCount = 0;
        while (isRunning()) {
            AbstractBinlogEvent event = client.poll();
            if (null == event) {
                continue;
            }
            eventCount += handleEvent(event);
        }
        log.info("incremental dump, eventCount={}", eventCount);
        pushRecord(new FinishedRecord(new PlaceholderPosition()));
    }
    
    private int handleEvent(final AbstractBinlogEvent event) {
        if (event instanceof PlaceholderEvent || filter(catalog, (AbstractRowsEvent) event)) {
            createPlaceholderRecord(event);
            return 0;
        }
        if (event instanceof WriteRowsEvent) {
            PipelineTableMetaData tableMetaData = getPipelineTableMetaData(((WriteRowsEvent) event).getTableName());
            handleWriteRowsEvent((WriteRowsEvent) event, tableMetaData);
            return 1;
        } else if (event instanceof UpdateRowsEvent) {
            PipelineTableMetaData tableMetaData = getPipelineTableMetaData(((UpdateRowsEvent) event).getTableName());
            handleUpdateRowsEvent((UpdateRowsEvent) event, tableMetaData);
            return 1;
        } else if (event instanceof DeleteRowsEvent) {
            PipelineTableMetaData tableMetaData = getPipelineTableMetaData(((DeleteRowsEvent) event).getTableName());
            handleDeleteRowsEvent((DeleteRowsEvent) event, tableMetaData);
            return 1;
        } else {
            return 0;
        }
    }
    
    private boolean filter(final String database, final AbstractRowsEvent event) {
        return !event.getDatabaseName().equals(database) || !dumperConfig.containsTable(event.getTableName());
    }
    
    private void handleWriteRowsEvent(final WriteRowsEvent event, final PipelineTableMetaData tableMetaData) {
        for (Serializable[] each : event.getAfterRows()) {
            DataRecord record = createDataRecord(event, each.length);
            record.setType(IngestDataChangeType.INSERT);
            for (int i = 0; i < each.length; i++) {
                PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(i + 1);
                record.addColumn(new Column(columnMetaData.getName(), handleValue(columnMetaData, each[i]), true, tableMetaData.getColumnMetaData(i + 1).isUniqueKey()));
            }
            pushRecord(record);
        }
    }
    
    private PipelineTableMetaData getPipelineTableMetaData(final String actualTableName) {
        return metaDataLoader.getTableMetaData(dumperConfig.getSchemaName(new ActualTableName(actualTableName)), actualTableName);
    }
    
    private void handleUpdateRowsEvent(final UpdateRowsEvent event, final PipelineTableMetaData tableMetaData) {
        for (int i = 0; i < event.getBeforeRows().size(); i++) {
            Serializable[] beforeValues = event.getBeforeRows().get(i);
            Serializable[] afterValues = event.getAfterRows().get(i);
            DataRecord record = createDataRecord(event, beforeValues.length);
            record.setType(IngestDataChangeType.UPDATE);
            for (int j = 0; j < beforeValues.length; j++) {
                Serializable oldValue = beforeValues[j];
                Serializable newValue = afterValues[j];
                boolean updated = !Objects.equals(newValue, oldValue);
                PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(j + 1);
                record.addColumn(new Column(columnMetaData.getName(),
                        (columnMetaData.isPrimaryKey() && updated) ? handleValue(columnMetaData, oldValue) : null,
                        handleValue(columnMetaData, newValue), updated, columnMetaData.isPrimaryKey()));
            }
            pushRecord(record);
        }
    }
    
    private void handleDeleteRowsEvent(final DeleteRowsEvent event, final PipelineTableMetaData tableMetaData) {
        for (Serializable[] each : event.getBeforeRows()) {
            DataRecord record = createDataRecord(event, each.length);
            record.setType(IngestDataChangeType.DELETE);
            for (int i = 0, length = each.length; i < length; i++) {
                PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(i + 1);
                record.addColumn(new Column(columnMetaData.getName(), handleValue(columnMetaData, each[i]), true, tableMetaData.getColumnMetaData(i + 1).isUniqueKey()));
            }
            pushRecord(record);
        }
    }
    
    private Serializable handleValue(final PipelineColumnMetaData columnMetaData, final Serializable value) {
        Optional<MySQLDataTypeHandler> dataTypeHandler = MySQLDataTypeHandlerFactory.findInstance(columnMetaData.getDataTypeName());
        return dataTypeHandler.isPresent() ? dataTypeHandler.get().handle(value) : value;
    }
    
    private DataRecord createDataRecord(final AbstractRowsEvent rowsEvent, final int columnCount) {
        DataRecord result = new DataRecord(new BinlogPosition(rowsEvent.getFileName(), rowsEvent.getPosition(), rowsEvent.getServerId()), columnCount);
        result.setTableName(dumperConfig.getLogicTableName(rowsEvent.getTableName()).getLowercase());
        result.setCommitTime(rowsEvent.getTimestamp() * 1000);
        return result;
    }
    
    private void createPlaceholderRecord(final AbstractBinlogEvent event) {
        PlaceholderRecord record = new PlaceholderRecord(new BinlogPosition(event.getFileName(), event.getPosition(), event.getServerId()));
        record.setCommitTime(event.getTimestamp() * 1000);
        pushRecord(record);
    }
    
    private void pushRecord(final Record record) {
        channel.pushRecord(record);
    }
    
    @Override
    protected void doStop() {
        if (null != client) {
            client.closeChannel();
        }
    }
}
