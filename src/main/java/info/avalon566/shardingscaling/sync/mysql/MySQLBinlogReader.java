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

package info.avalon566.shardingscaling.sync.mysql;

import info.avalon566.shardingscaling.sync.core.AbstractRunner;
import info.avalon566.shardingscaling.sync.core.Channel;
import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import info.avalon566.shardingscaling.sync.core.Reader;
import info.avalon566.shardingscaling.sync.jdbc.Column;
import info.avalon566.shardingscaling.sync.jdbc.DataRecord;
import info.avalon566.shardingscaling.sync.jdbc.DbMetaDataUtil;
import info.avalon566.shardingscaling.sync.jdbc.JdbcUri;
import info.avalon566.shardingscaling.sync.mysql.binlog.MySQLConnector;
import info.avalon566.shardingscaling.sync.mysql.binlog.event.DeleteRowsEvent;
import info.avalon566.shardingscaling.sync.mysql.binlog.event.UpdateRowsEvent;
import info.avalon566.shardingscaling.sync.mysql.binlog.event.WriteRowsEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.io.Serializable;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL binlog reader.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public final class MySQLBinlogReader extends AbstractRunner implements Reader {

    private final Map<Long, String> tableMapCache = new HashMap<>();

    private final BinlogPosition binlogPosition = new BinlogPosition();

    private final RdbmsConfiguration rdbmsConfiguration;

    private final DbMetaDataUtil dbMetaDataUtil;

    @Setter
    private Channel channel;

    public MySQLBinlogReader(final RdbmsConfiguration rdbmsConfiguration) {
        this.rdbmsConfiguration = rdbmsConfiguration;
        this.dbMetaDataUtil = new DbMetaDataUtil(rdbmsConfiguration);
    }

    @Override
    public void run() {
        start();
        read(channel);
    }

    /**
     * mark binlog position.
     */
    public void markPosition() {
        try {
            try (var connection = DriverManager.getConnection(rdbmsConfiguration.getJdbcUrl(), rdbmsConfiguration.getUsername(), rdbmsConfiguration.getPassword())) {
                var ps = connection.prepareStatement("show master status");
                var rs = ps.executeQuery();
                rs.next();
                binlogPosition.setFilename(rs.getString(1));
                binlogPosition.setPosition(rs.getLong(2));
                ps = connection.prepareStatement("show variables like 'server_id'");
                rs = ps.executeQuery();
                rs.next();
                binlogPosition.setServerId(rs.getString(2));
            }
        } catch (SQLException e) {
            throw new RuntimeException("markPosition error", e);
        }
    }

    @Override
    public void read(final Channel channel) {
        final var uri = new JdbcUri(rdbmsConfiguration.getJdbcUrl());
        var client = new MySQLConnector(123456, uri.getHostname(), uri.getPort(), rdbmsConfiguration.getUsername(), rdbmsConfiguration.getPassword());
        client.connect();
        client.subscribe(binlogPosition.getFilename(), binlogPosition.getPosition());
        while (true) {
            var event = client.poll();
            if (null == event) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // ignore
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
    }

    private void handleWriteRowsEvent(final Channel channel, final JdbcUri uri, final WriteRowsEvent event) {
        var wred = event;
        for (Serializable[] row : wred.getAfterColumns()) {
            if (filter(uri.getDatabase(), wred.getTableName())) {
                continue;
            }
            var record = new DataRecord(row.length);
            record.setFullTableName(wred.getTableName());
            record.setType("insert");
            for (int i = 0; i < row.length; i++) {
                record.addColumn(new Column(getColumnValue(record.getTableName(), i, row[i]), true));
            }
            channel.pushRecord(record);
        }
    }

    private void handleUpdateRowsEvent(final Channel channel, final JdbcUri uri, final UpdateRowsEvent event) {
        var ured = event;
        for (int i = 0; i < ured.getBeforeColumns().size(); i++) {
            if (filter(uri.getDatabase(), event.getTableName())) {
                continue;
            }
            var beforeValues = ured.getBeforeColumns().get(i);
            var afterValues = ured.getAfterColumns().get(i);
            var record = new DataRecord(beforeValues.length);
            record.setFullTableName(event.getTableName());
            record.setType("update");
            for (int j = 0; j < beforeValues.length; j++) {
                var oldValue = getColumnValue(record.getTableName(), j, beforeValues[j]);
                var newValue = getColumnValue(record.getTableName(), j, afterValues[j]);
                record.addColumn(new Column(newValue, newValue.equals(oldValue)));
            }
            channel.pushRecord(record);
        }
    }

    private void handleDeleteRowsEvent(final Channel channel, final JdbcUri uri, final DeleteRowsEvent event) {
        var dred = event;
        for (Serializable[] row : dred.getBeforeColumns()) {
            if (filter(uri.getDatabase(), dred.getTableName())) {
                continue;
            }
            var record = new DataRecord(row.length);
            record.setFullTableName(dred.getTableName());
            record.setType("delete");
            for (int i = 0; i < row.length; i++) {
                record.addColumn(new Column(getColumnValue(record.getTableName(), i, row[i]), true));
            }
            channel.pushRecord(record);
        }
    }

    @Override
    public List<RdbmsConfiguration> split(final int concurrency) {
        return Arrays.asList(rdbmsConfiguration);
    }

    private boolean filter(final String database, final String fullTableName) {
        return !fullTableName.startsWith(database);
    }

    private Object getColumnValue(final String tableName, final int index, final Serializable data) {
        //var columns = dbMetaDataUtil.getColumNames(tableName);
        return data;
    }
}
