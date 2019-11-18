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

package info.avalon566.shardingscaling.core.execute.executor.reader;

import info.avalon566.shardingscaling.core.config.JdbcDataSourceConfiguration;
import info.avalon566.shardingscaling.core.config.RdbmsConfiguration;
import info.avalon566.shardingscaling.core.exception.SyncRunException;
import info.avalon566.shardingscaling.core.execute.executor.AbstractSyncRunner;
import info.avalon566.shardingscaling.core.execute.executor.channel.Channel;
import info.avalon566.shardingscaling.core.execute.metadata.ColumnMetaData;
import info.avalon566.shardingscaling.core.execute.executor.record.Column;
import info.avalon566.shardingscaling.core.execute.executor.record.DataRecord;
import info.avalon566.shardingscaling.core.execute.executor.record.FinishedRecord;
import info.avalon566.shardingscaling.core.execute.executor.record.Record;
import info.avalon566.shardingscaling.core.execute.util.DataSourceFactory;
import info.avalon566.shardingscaling.core.execute.util.DbMetaDataUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * generic jdbc reader implement.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public abstract class AbstractJdbcReader extends AbstractSyncRunner implements JdbcReader {

    @Getter(AccessLevel.PROTECTED)
    private final RdbmsConfiguration rdbmsConfiguration;

    @Setter
    private Channel channel;

    public AbstractJdbcReader(final RdbmsConfiguration rdbmsConfiguration) {
        if (!JdbcDataSourceConfiguration.class.equals(rdbmsConfiguration.getDataSourceConfiguration().getClass())) {
            throw new UnsupportedOperationException("AbstractJdbcReader only support JdbcDataSourceConfiguration");
        }
        this.rdbmsConfiguration = rdbmsConfiguration;
    }

    @Override
    public final void run() {
        start();
        read(channel);
    }

    @Override
    public final void read(final Channel channel) {
        DataSource dataSource = DataSourceFactory.getDataSource(rdbmsConfiguration.getDataSourceConfiguration());
        try {
            Connection conn = dataSource.getConnection();
            String sql = String.format("select * from %s %s", rdbmsConfiguration.getTableName(), rdbmsConfiguration.getWhereCondition());
            PreparedStatement ps = conn.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            ps.setFetchDirection(ResultSet.FETCH_REVERSE);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            while (isRunning() && rs.next()) {
                DataRecord record = new DataRecord(new NopLogPosition(), metaData.getColumnCount());
                record.setType("bootstrap-insert");
                record.setFullTableName(String.format("%s.%s", conn.getCatalog(), rdbmsConfiguration.getTableName()));
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    if (Types.TIME == rs.getMetaData().getColumnType(i)
                            || Types.DATE == rs.getMetaData().getColumnType(i)
                            || Types.TIMESTAMP == rs.getMetaData().getColumnType(i)) {
                        // fix: jdbc Time objects represent a wall-clock time and not a duration as MySQL treats them
                        record.addColumn(new Column(rs.getString(i), true));
                    } else {
                        record.addColumn(new Column(rs.getObject(i), true));
                    }
                }
                pushRecord(record);
            }
        } catch (SQLException e) {
            throw new SyncRunException(e);
        } finally {
            pushRecord(new FinishedRecord(new NopLogPosition()));
        }
    }

    private void pushRecord(final Record record) {
        try {
            channel.pushRecord(record);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * generic jdbc reader split implement.
     */
    @Override
    public List<RdbmsConfiguration> split(final int concurrency) {
        DataSource dataSource = DataSourceFactory.getDataSource(rdbmsConfiguration.getDataSourceConfiguration());
        DbMetaDataUtil dbMetaDataUtil = new DbMetaDataUtil(dataSource);
        List<String> primaryKeys = dbMetaDataUtil.getPrimaryKeys(rdbmsConfiguration.getTableName());
        if (null == primaryKeys || 0 == primaryKeys.size()) {
            log.warn("{} 表主键不存在, 不支持并发执行", rdbmsConfiguration.getTableName());
            return Collections.singletonList(rdbmsConfiguration);
        }
        if (primaryKeys.size() > 1) {
            log.warn("{} 表为联合主键, 不支持并发执行", rdbmsConfiguration.getTableName());
            return Collections.singletonList(rdbmsConfiguration);
        }
        List<ColumnMetaData> metaData = dbMetaDataUtil.getColumnNames(rdbmsConfiguration.getTableName());
        int index = DbMetaDataUtil.findColumnIndex(metaData, primaryKeys.get(0));
        if (Types.INTEGER != metaData.get(index).getColumnType()) {
            log.warn("{} 主键不是整形,不支持并发执行", rdbmsConfiguration.getTableName());
            return Collections.singletonList(rdbmsConfiguration);
        }
        String pk = primaryKeys.get(0);
        try {
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement ps = connection.prepareStatement(String.format("select min(%s),max(%s) from %s limit 1", pk, pk, rdbmsConfiguration.getTableName()));
                ResultSet rs = ps.executeQuery();
                rs.next();
                int min = rs.getInt(1);
                int max = rs.getInt(2);
                int step = (max - min) / concurrency;
                List<RdbmsConfiguration> configs = new ArrayList<>(concurrency);
                for (int i = 0; i < concurrency; i++) {
                    RdbmsConfiguration tmp = RdbmsConfiguration.clone(rdbmsConfiguration);
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
