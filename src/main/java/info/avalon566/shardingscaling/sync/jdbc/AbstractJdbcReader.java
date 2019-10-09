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

package info.avalon566.shardingscaling.sync.jdbc;

import info.avalon566.shardingscaling.sync.core.AbstractRunner;
import info.avalon566.shardingscaling.sync.core.Channel;
import info.avalon566.shardingscaling.sync.core.FinishedRecord;
import info.avalon566.shardingscaling.sync.core.RdbmsConfiguration;
import info.avalon566.shardingscaling.sync.core.Reader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * generic jdbc reader implement.
 * @author avalon566
 */
@Slf4j
public abstract class AbstractJdbcReader extends AbstractRunner implements Reader {

    @Getter(AccessLevel.PROTECTED)
    private final RdbmsConfiguration rdbmsConfiguration;

    @Setter
    private Channel channel;

    public AbstractJdbcReader(final RdbmsConfiguration rdbmsConfiguration) {
        this.rdbmsConfiguration = rdbmsConfiguration;
    }

    @Override
    public final void run() {
        start();
        read(channel);
    }

    @Override
    public final void read(final Channel channel) {
        try {
            Connection conn = DriverManager.getConnection(
                    rdbmsConfiguration.getJdbcUrl(),
                    rdbmsConfiguration.getUsername(),
                    rdbmsConfiguration.getPassword());
            var sql = String.format("select * from %s %s", rdbmsConfiguration.getTableName(), rdbmsConfiguration.getWhereCondition());
            var ps = conn.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            ps.setFetchDirection(ResultSet.FETCH_REVERSE);
            var rs = ps.executeQuery();
            var metaData = rs.getMetaData();
            while (running && rs.next()) {
                var record = new DataRecord(metaData.getColumnCount());
                record.setType("bootstrap-insert");
                record.setFullTableName(String.format("%s.%s", conn.getCatalog(), rdbmsConfiguration.getTableName()));
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    record.addColumn(new Column(rs.getObject(i), true));
                }
                channel.pushRecord(record);
            }
            channel.pushRecord(new FinishedRecord());
        } catch (SQLException e) {
            // make sure writer thread can exit
            channel.pushRecord(new FinishedRecord());
            throw new RuntimeException(e);
        }
    }

    /**
     * generic jdbc reader split implement.
     */
    @Override
    public List<RdbmsConfiguration> split(final int concurrency) {
        var primaryKeys = new DbMetaDataUtil(rdbmsConfiguration).getPrimaryKeys(rdbmsConfiguration.getTableName());
        if (primaryKeys == null || primaryKeys.size() == 0) {
            log.warn("{} 表主键不存在, 不支持并发执行", rdbmsConfiguration.getTableName());
            return Arrays.asList(rdbmsConfiguration);
        }
        if (1 < primaryKeys.size()) {
            log.warn("{} 表为联合主键, 不支持并发执行", rdbmsConfiguration.getTableName());
            return Arrays.asList(rdbmsConfiguration);
        }
        var metaData = new DbMetaDataUtil(rdbmsConfiguration).getColumNames(rdbmsConfiguration.getTableName());
        var index = DbMetaDataUtil.findColumnIndex(metaData, primaryKeys.get(0));
        if (Types.INTEGER != metaData.get(index).getColumnType()) {
            log.warn("{} 主键不是整形,不支持并发执行", rdbmsConfiguration.getTableName());
            return Arrays.asList(rdbmsConfiguration);
        }
        var pk = primaryKeys.get(0);
        try {
            try (var connection = DriverManager.getConnection(rdbmsConfiguration.getJdbcUrl(), rdbmsConfiguration.getUsername(), rdbmsConfiguration.getPassword())) {
                var ps = connection.prepareStatement(String.format("select min(%s),max(%s) from %s limit 1", pk, pk, rdbmsConfiguration.getTableName()));
                var rs = ps.executeQuery();
                rs.next();
                var min = rs.getInt(1);
                var max = rs.getInt(2);
                var step = (max - min) / concurrency;
                var configs = new ArrayList<RdbmsConfiguration>(concurrency);
                for (int i = 0; i < concurrency; i++) {
                    var tmp = rdbmsConfiguration.clone();
                    if (i < concurrency - 1) {
                        tmp.setWhereCondition(String.format("where id between %d and %d", min, min + step));
                        min = min + step + 1;
                    } else {
                        tmp.setWhereCondition(String.format("where id between %d and %d", min, max));
                    }
                    configs.add(tmp);
                }
                return configs;
            }
        } catch (SQLException e) {
            throw new RuntimeException("getTableNames error", e);
        }
    }
}
