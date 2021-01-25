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

package org.apache.shardingsphere.scaling.mysql.component;

import com.google.common.base.Preconditions;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceFactory;
import org.apache.shardingsphere.scaling.core.execute.executor.AbstractScalingExecutor;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.Channel;
import org.apache.shardingsphere.scaling.core.execute.executor.dumper.LogDumper;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.PlaceholderRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.metadata.JdbcUri;
import org.apache.shardingsphere.scaling.core.metadata.MetaDataManager;
import org.apache.shardingsphere.scaling.mysql.binlog.BinlogPosition;
import org.apache.shardingsphere.scaling.mysql.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.AbstractRowsEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.WriteRowsEvent;
import org.apache.shardingsphere.scaling.mysql.client.ConnectInfo;
import org.apache.shardingsphere.scaling.mysql.client.MySQLClient;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * MySQL binlog dumper.
 */
@Slf4j
public final class MySQLBinlogDumper extends AbstractScalingExecutor implements LogDumper {
    
    private final BinlogPosition binlogPosition;
    
    private final DumperConfiguration dumperConfig;
    
    private final MetaDataManager metaDataManager;
    
    private final Random random = new SecureRandom();
    
    @Setter
    private Channel channel;
    
    public MySQLBinlogDumper(final DumperConfiguration dumperConfig, final Position<BinlogPosition> binlogPosition) {
        this.binlogPosition = (BinlogPosition) binlogPosition;
        this.dumperConfig = dumperConfig;
        Preconditions.checkArgument(dumperConfig.getDataSourceConfig() instanceof StandardJDBCDataSourceConfiguration, "MySQLBinlogDumper only support StandardJDBCDataSourceConfiguration");
        metaDataManager = new MetaDataManager(new DataSourceFactory().newInstance(dumperConfig.getDataSourceConfig()));
    }
    
    @Override
    public void start() {
        super.start();
        dump();
    }
    
    private void dump() {
        StandardJDBCDataSourceConfiguration jdbcDataSourceConfig = (StandardJDBCDataSourceConfiguration) dumperConfig.getDataSourceConfig();
        JdbcUri uri = new JdbcUri(jdbcDataSourceConfig.getJdbcUrl());
        MySQLClient client = new MySQLClient(new ConnectInfo(random.nextInt(), uri.getHostname(), uri.getPort(), jdbcDataSourceConfig.getUsername(), jdbcDataSourceConfig.getPassword()));
        client.connect();
        client.subscribe(binlogPosition.getFilename(), binlogPosition.getPosition());
        while (isRunning()) {
            AbstractBinlogEvent event = client.poll();
            if (null != event) {
                handleEvent(uri, event);
            }
        }
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
        TableMetaData tableMetaData = metaDataManager.getTableMetaData(event.getTableName());
        for (Serializable[] each : event.getAfterRows()) {
            DataRecord record = createDataRecord(event, each.length);
            record.setType(ScalingConstant.INSERT);
            for (int i = 0; i < each.length; i++) {
                record.addColumn(new Column(tableMetaData.getColumnMetaData(i).getName(), each[i], true, tableMetaData.isPrimaryKey(i)));
            }
            pushRecord(record);
        }
    }
    
    private void handleUpdateRowsEvent(final UpdateRowsEvent event) {
        TableMetaData tableMetaData = metaDataManager.getTableMetaData(event.getTableName());
        for (int i = 0; i < event.getBeforeRows().size(); i++) {
            Serializable[] beforeValues = event.getBeforeRows().get(i);
            Serializable[] afterValues = event.getAfterRows().get(i);
            DataRecord record = createDataRecord(event, beforeValues.length);
            record.setType(ScalingConstant.UPDATE);
            for (int j = 0; j < beforeValues.length; j++) {
                Object oldValue = beforeValues[j];
                Object newValue = afterValues[j];
                boolean updated = !Objects.equals(newValue, oldValue);
                record.addColumn(new Column(tableMetaData.getColumnMetaData(j).getName(),
                        (tableMetaData.isPrimaryKey(j) && updated) ? oldValue : null,
                        newValue, updated, tableMetaData.isPrimaryKey(j)));
            }
            pushRecord(record);
        }
    }
    
    private void handleDeleteRowsEvent(final DeleteRowsEvent event) {
        TableMetaData tableMetaData = metaDataManager.getTableMetaData(event.getTableName());
        for (Serializable[] each : event.getBeforeRows()) {
            DataRecord record = createDataRecord(event, each.length);
            record.setType(ScalingConstant.DELETE);
            for (int i = 0; i < each.length; i++) {
                record.addColumn(new Column(tableMetaData.getColumnMetaData(i).getName(), each[i], true, tableMetaData.isPrimaryKey(i)));
            }
            pushRecord(record);
        }
    }
    
    private DataRecord createDataRecord(final AbstractRowsEvent rowsEvent, final int columnCount) {
        long delay = System.currentTimeMillis() - rowsEvent.getTimestamp() * 1000;
        DataRecord result = new DataRecord(new BinlogPosition(rowsEvent.getFileName(), rowsEvent.getPosition(), rowsEvent.getServerId(), delay), columnCount);
        result.setTableName(dumperConfig.getTableNameMap().get(rowsEvent.getTableName()));
        result.setCommitTime(rowsEvent.getTimestamp() * 1000);
        return result;
    }
    
    private void createPlaceholderRecord(final AbstractBinlogEvent event) {
        long delay = System.currentTimeMillis() - event.getTimestamp() * 1000;
        PlaceholderRecord record = new PlaceholderRecord(new BinlogPosition(event.getFileName(), event.getPosition(), event.getServerId(), delay));
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
