/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one or more
 *  * contributor license agreements.  See the NOTICE file distributed with
 *  * this work for additional information regarding copyright ownership.
 *  * The ASF licenses this file to You under the Apache License, Version 2.0
 *  * (the "License"); you may not use this file except in compliance with
 *  * the License.  You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.apache.shardingsphere.shardingscaling.core.synctask;

import lombok.SneakyThrows;
import org.apache.shardingsphere.shardingscaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.JdbcDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingContext;
import org.apache.shardingsphere.shardingscaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.Event;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HistoryDataSyncTaskTest {

    private static String dataSourceUrl = "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";

    private static String userName = "root";

    private static String password = "password";

    private SyncConfiguration syncConfiguration;

    @Before
    public void setUp() {
        RdbmsConfiguration readerConfig = mockReaderConfig();
        RdbmsConfiguration writerConfig = mockWriterConfig();
        ScalingContext.getInstance().init(null, new ServerConfiguration(100, 1000, 3));
        syncConfiguration = new SyncConfiguration(3, readerConfig, writerConfig);
    }

    @Test
    public void assertGetProgress() {
        initTableData(syncConfiguration.getReaderConfiguration());
        HistoryDataSyncTask historyDataSyncTask = new HistoryDataSyncTask(syncConfiguration);
        historyDataSyncTask.start(new ReportCallback() {

            @Override
            public void onProcess(final Event event) {

            }
        });
        assertThat(((HistoryDataSyncTaskProgress) historyDataSyncTask.getProgress()).getEstimatedRows(), is(2L));
    }

    @SneakyThrows
    private void initTableData(final RdbmsConfiguration readerConfig) {
        DataSource dataSource = DataSourceFactory.getDataSource(readerConfig.getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id int primary key, user_id varchar(12))");
            statement.execute("INSERT INTO t_order (id, user_id) values (1, 'xxx'), (999, 'yyy')");
        }
    }

    private RdbmsConfiguration mockReaderConfig() {
        DataSourceConfiguration dataSourceConfiguration = new JdbcDataSourceConfiguration(dataSourceUrl, userName, password);
        RdbmsConfiguration result = new RdbmsConfiguration();
        result.setDataSourceConfiguration(dataSourceConfiguration);
        result.setTableName("t_order");
        return result;
    }

    private RdbmsConfiguration mockWriterConfig() {
        DataSourceConfiguration dataSourceConfiguration = new JdbcDataSourceConfiguration(dataSourceUrl, userName, password);
        RdbmsConfiguration result = new RdbmsConfiguration();
        result.setDataSourceConfiguration(dataSourceConfiguration);
        return result;
    }
}
