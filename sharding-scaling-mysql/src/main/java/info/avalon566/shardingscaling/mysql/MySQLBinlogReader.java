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

package info.avalon566.shardingscaling.mysql;

import info.avalon566.shardingscaling.core.config.RdbmsConfiguration;
import info.avalon566.shardingscaling.core.sync.AbstractSyncRunner;
import info.avalon566.shardingscaling.core.sync.channel.Channel;
import info.avalon566.shardingscaling.core.sync.reader.LogPosition;
import info.avalon566.shardingscaling.core.sync.reader.LogReader;
import info.avalon566.shardingscaling.core.sync.record.Column;
import info.avalon566.shardingscaling.core.sync.record.DataRecord;
import info.avalon566.shardingscaling.core.sync.metadata.JdbcUri;
import info.avalon566.shardingscaling.core.sync.record.FinishedRecord;
import info.avalon566.shardingscaling.core.sync.record.Record;
import info.avalon566.shardingscaling.mysql.binlog.event.AbstractBinlogEvent;
import info.avalon566.shardingscaling.mysql.binlog.event.DeleteRowsEvent;
import info.avalon566.shardingscaling.mysql.binlog.event.UpdateRowsEvent;
import info.avalon566.shardingscaling.mysql.binlog.event.WriteRowsEvent;
import info.avalon566.shardingscaling.core.sync.util.DataSourceFactory;
import info.avalon566.shardingscaling.core.config.JdbcDataSourceConfiguration;
import info.avalon566.shardingscaling.mysql.binlog.MySQLConnector;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MySQL binlog reader.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public final class MySQLBinlogReader extends AbstractSyncRunner implements LogReader {

    private final BinlogPosition binlogPosition;

    private final RdbmsConfiguration rdbmsConfiguration;

    @Setter
    private Channel channel;

    public MySQLBinlogReader(final RdbmsConfiguration rdbmsConfiguration, final LogPosition binlogPosition) {
        this.binlogPosition = (BinlogPosition) binlogPosition;
        if (!JdbcDataSourceConfiguration.class.equals(rdbmsConfiguration.getDataSourceConfiguration().getClass())) {
            throw new UnsupportedOperationException("MySQLBinlogReader only support JdbcDataSourceConfiguration");
        }
        this.rdbmsConfiguration = rdbmsConfiguration;
    }

    @Override
    public void run() {
        start();
        read(channel);
    }

    @Override
    public LogPosition markPosition() {
        try {
            DataSource dataSource = DataSourceFactory.getDataSource(rdbmsConfiguration.getDataSourceConfiguration());
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement ps = connection.prepareStatement("show master status");
                ResultSet rs = ps.executeQuery();
                rs.next();
                BinlogPosition currentPosition = new BinlogPosition();
                currentPosition.setFilename(rs.getString(1));
                currentPosition.setPosition(rs.getLong(2));
                ps = connection.prepareStatement("show variables like 'server_id'");
                rs = ps.executeQuery();
                rs.next();
                currentPosition.setServerId(rs.getString(2));
                return currentPosition;
            }
        } catch (SQLException e) {
            throw new RuntimeException("markPosition error", e);
        }
    }

    @Override
    public void read(final Channel channel) {
        JdbcDataSourceConfiguration jdbcDataSourceConfiguration = (JdbcDataSourceConfiguration) rdbmsConfiguration.getDataSourceConfiguration();
        final JdbcUri uri = new JdbcUri(jdbcDataSourceConfiguration.getJdbcUrl());
        MySQLConnector client = new MySQLConnector(123456, uri.getHostname(), uri.getPort(), jdbcDataSourceConfiguration.getUsername(), jdbcDataSourceConfiguration.getPassword());
        client.connect();
        client.subscribe(binlogPosition.getFilename(), binlogPosition.getPosition());
        while (isRunning()) {
            AbstractBinlogEvent event = client.poll();
            if (null == event) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
                continue;
            }
            if (event instanceof WriteRowsEvent) {
                handleWriteRowsEvent(channel, uri, (WriteRowsEvent) event);
            } else if (event instanceof UpdateRowsEvent) {
                handleUpdateRowsEvent(channel, uri, (UpdateRowsEvent) event);
            } else if (event instanceof DeleteRowsEvent) {
                handleDeleteRowsEvent(channel, uri, (DeleteRowsEvent) event);
            }
        }
        pushRecord(channel, new FinishedRecord());
    }

    private void handleWriteRowsEvent(final Channel channel, final JdbcUri uri, final WriteRowsEvent event) {
        WriteRowsEvent wred = event;
        for (Serializable[] each : wred.getAfterColumns()) {
            if (filter(uri.getDatabase(), wred.getTableName())) {
                continue;
            }
            DataRecord record = new DataRecord(each.length);
            record.setFullTableName(wred.getTableName());
            record.setType("insert");
            for (int i = 0; i < each.length; i++) {
                record.addColumn(new Column(getColumnValue(record.getTableName(), i, each[i]), true));
            }
            pushRecord(channel, record);
        }
    }

    private void handleUpdateRowsEvent(final Channel channel, final JdbcUri uri, final UpdateRowsEvent event) {
        UpdateRowsEvent ured = event;
        for (int i = 0; i < ured.getBeforeColumns().size(); i++) {
            if (filter(uri.getDatabase(), event.getTableName())) {
                continue;
            }
            Serializable[] beforeValues = ured.getBeforeColumns().get(i);
            Serializable[] afterValues = ured.getAfterColumns().get(i);
            DataRecord record = new DataRecord(beforeValues.length);
            record.setFullTableName(event.getTableName());
            record.setType("update");
            for (int j = 0; j < beforeValues.length; j++) {
                Object oldValue = getColumnValue(record.getTableName(), j, beforeValues[j]);
                Object newValue = getColumnValue(record.getTableName(), j, afterValues[j]);
                record.addColumn(new Column(newValue, !newValue.equals(oldValue)));
            }
            pushRecord(channel, record);
        }
    }

    private void handleDeleteRowsEvent(final Channel channel, final JdbcUri uri, final DeleteRowsEvent event) {
        DeleteRowsEvent dred = event;
        for (Serializable[] each : dred.getBeforeColumns()) {
            if (filter(uri.getDatabase(), dred.getTableName())) {
                continue;
            }
            DataRecord record = new DataRecord(each.length);
            record.setFullTableName(dred.getTableName());
            record.setType("delete");
            for (int i = 0; i < each.length; i++) {
                record.addColumn(new Column(getColumnValue(record.getTableName(), i, each[i]), true));
            }
            pushRecord(channel, record);
        }
    }

    private void pushRecord(final Channel channel, final Record record) {
        try {
            channel.pushRecord(record);
        } catch (InterruptedException ignored) {
        }
    }
    
    private boolean filter(final String database, final String fullTableName) {
        return !fullTableName.startsWith(database + ".");
    }

    private Object getColumnValue(final String tableName, final int index, final Serializable data) {
        //var columns = dbMetaDataUtil.getColumnNames(tableName);
        return data;
    }
}
