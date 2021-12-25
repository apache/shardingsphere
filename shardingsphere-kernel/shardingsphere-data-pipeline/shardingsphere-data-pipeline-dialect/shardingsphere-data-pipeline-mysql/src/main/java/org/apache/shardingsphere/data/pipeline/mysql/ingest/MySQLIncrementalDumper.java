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
import com.zaxxer.hikari.HikariConfig;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.Channel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceFactory;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogPosition;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.WriteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.ConnectInfo;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.client.MySQLClient;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.metadata.MySQLColumnMetaData;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.metadata.MySQLColumnMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.column.value.ValueHandler;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.IncrementalDumper;
import org.apache.shardingsphere.infra.config.datasource.JdbcUri;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.impl.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * MySQL incremental dumper.
 */
@Slf4j
public final class MySQLIncrementalDumper extends AbstractLifecycleExecutor implements IncrementalDumper {
    
    private static final Map<String, ValueHandler> VALUE_HANDLER_MAP;
    
    private final BinlogPosition binlogPosition;
    
    private final DumperConfiguration dumperConfig;
    
    private final MySQLColumnMetaDataLoader columnMetaDataLoader;
    
    private final Random random = new SecureRandom();
    
    @Setter
    private Channel channel;
    
    static {
        ShardingSphereServiceLoader.register(ValueHandler.class);
        VALUE_HANDLER_MAP = ShardingSphereServiceLoader.getSingletonServiceInstances(ValueHandler.class)
                .stream().collect(Collectors.toMap(ValueHandler::getTypeName, v -> v));
    }
    
    public MySQLIncrementalDumper(final DumperConfiguration dumperConfig, final IngestPosition<BinlogPosition> binlogPosition) {
        this.binlogPosition = (BinlogPosition) binlogPosition;
        this.dumperConfig = dumperConfig;
        Preconditions.checkArgument(dumperConfig.getDataSourceConfig() instanceof StandardJDBCDataSourceConfiguration, "MySQLBinlogDumper only support StandardJDBCDataSourceConfiguration");
        columnMetaDataLoader = new MySQLColumnMetaDataLoader(new DataSourceFactory().newInstance(dumperConfig.getDataSourceConfig()));
    }
    
    @Override
    public void start() {
        super.start();
        dump();
    }
    
    private void dump() {
        HikariConfig hikariConfig = ((StandardJDBCDataSourceConfiguration) dumperConfig.getDataSourceConfig()).getHikariConfig();
        log.info("incremental dump, jdbcUrl={}", hikariConfig.getJdbcUrl());
        JdbcUri uri = new JdbcUri(hikariConfig.getJdbcUrl());
        MySQLClient client = new MySQLClient(new ConnectInfo(random.nextInt(), uri.getHostname(), uri.getPort(), hikariConfig.getUsername(), hikariConfig.getPassword()));
        client.connect();
        client.subscribe(binlogPosition.getFilename(), binlogPosition.getPosition());
        int eventCount = 0;
        while (isRunning()) {
            AbstractBinlogEvent event = client.poll();
            if (null != event) {
                handleEvent(uri, event);
                eventCount++;
            }
        }
        log.info("incremental dump, eventCount={}", eventCount);
        pushRecord(new FinishedRecord(new PlaceholderPosition()));
    }
    
    private void handleEvent(final JdbcUri uri, final AbstractBinlogEvent event) {
        if (event instanceof PlaceholderEvent || filter(uri.getDatabase(), (AbstractRowsEvent) event)) {
            createPlaceholderRecord(event);
            return;
        }
        if (event instanceof WriteRowsEvent) {
            handleWriteRowsEvent((WriteRowsEvent) event);
        } else if (event instanceof UpdateRowsEvent) {
            handleUpdateRowsEvent((UpdateRowsEvent) event);
        } else if (event instanceof DeleteRowsEvent) {
            handleDeleteRowsEvent((DeleteRowsEvent) event);
        }
    }
    
    private boolean filter(final String database, final AbstractRowsEvent event) {
        return !event.getSchemaName().equals(database) || !dumperConfig.getTableNameMap().containsKey(event.getTableName());
    }
    
    private void handleWriteRowsEvent(final WriteRowsEvent event) {
        List<MySQLColumnMetaData> tableMetaData = columnMetaDataLoader.load(event.getTableName());
        for (Serializable[] each : event.getAfterRows()) {
            DataRecord record = createDataRecord(event, each.length);
            record.setType(IngestDataChangeType.INSERT);
            for (int i = 0; i < each.length; i++) {
                record.addColumn(new Column(tableMetaData.get(i).getName(), handleValue(tableMetaData.get(i), each[i]), true, tableMetaData.get(i).isPrimaryKey()));
            }
            pushRecord(record);
        }
    }
    
    private void handleUpdateRowsEvent(final UpdateRowsEvent event) {
        List<MySQLColumnMetaData> tableMetaData = columnMetaDataLoader.load(event.getTableName());
        for (int i = 0; i < event.getBeforeRows().size(); i++) {
            Serializable[] beforeValues = event.getBeforeRows().get(i);
            Serializable[] afterValues = event.getAfterRows().get(i);
            DataRecord record = createDataRecord(event, beforeValues.length);
            record.setType(IngestDataChangeType.UPDATE);
            for (int j = 0; j < beforeValues.length; j++) {
                Serializable oldValue = beforeValues[j];
                Serializable newValue = afterValues[j];
                boolean updated = !Objects.equals(newValue, oldValue);
                record.addColumn(new Column(tableMetaData.get(j).getName(),
                        (tableMetaData.get(j).isPrimaryKey() && updated) ? handleValue(tableMetaData.get(j), oldValue) : null,
                        handleValue(tableMetaData.get(j), newValue), updated, tableMetaData.get(j).isPrimaryKey()));
            }
            pushRecord(record);
        }
    }
    
    private void handleDeleteRowsEvent(final DeleteRowsEvent event) {
        List<MySQLColumnMetaData> tableMetaData = columnMetaDataLoader.load(event.getTableName());
        for (Serializable[] each : event.getBeforeRows()) {
            DataRecord record = createDataRecord(event, each.length);
            record.setType(IngestDataChangeType.DELETE);
            for (int i = 0; i < each.length; i++) {
                record.addColumn(new Column(tableMetaData.get(i).getName(), handleValue(tableMetaData.get(i), each[i]), true, tableMetaData.get(i).isPrimaryKey()));
            }
            pushRecord(record);
        }
    }
    
    private Serializable handleValue(final MySQLColumnMetaData columnMetaData, final Serializable value) {
        ValueHandler valueHandler = VALUE_HANDLER_MAP.get(columnMetaData.getDataTypeName());
        if (null != valueHandler) {
            return valueHandler.handle(value);
        }
        return value;
    }
    
    private DataRecord createDataRecord(final AbstractRowsEvent rowsEvent, final int columnCount) {
        DataRecord result = new DataRecord(new BinlogPosition(rowsEvent.getFileName(), rowsEvent.getPosition(), rowsEvent.getServerId()), columnCount);
        result.setTableName(dumperConfig.getTableNameMap().get(rowsEvent.getTableName()));
        result.setCommitTime(rowsEvent.getTimestamp() * 1000);
        return result;
    }
    
    private void createPlaceholderRecord(final AbstractBinlogEvent event) {
        PlaceholderRecord record = new PlaceholderRecord(new BinlogPosition(event.getFileName(), event.getPosition(), event.getServerId()));
        record.setCommitTime(event.getTimestamp() * 1000);
        pushRecord(record);
    }
    
    private void pushRecord(final Record record) {
        try {
            channel.pushRecord(record);
        } catch (final InterruptedException ignored) {
        }
    }
}
