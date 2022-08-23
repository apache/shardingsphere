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

package org.apache.shardingsphere.data.pipeline.core.dumper;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixtureIncrementalDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.SimpleMemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.IncrementalDumperCreatorFactory;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.MySQLIncrementalDumper;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogPosition;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.OpenGaussWalDumper;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLWalDumper;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WalPosition;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.IncrementalDumper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public final class IncrementalDumperCreatorFactoryTest {
    
    private PipelineDataSourceWrapper dataSource;
    
    @Mock
    private WalPosition walPosition;
    
    @Before
    public void setUp() {
        PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
        DumperConfiguration dumperConfig = mockDumperConfiguration();
        dataSource = dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig());
    }
    
    @Test
    public void assertIncrementalDumperCreatorForMysql() {
        IncrementalDumper actual = IncrementalDumperCreatorFactory.getInstance("MySQL")
                .createIncrementalDumper(mockDumperConfiguration(), new BinlogPosition("binlog-000001", 4L), new SimpleMemoryPipelineChannel(100), new PipelineTableMetaDataLoader(dataSource));
        assertThat(actual, instanceOf(MySQLIncrementalDumper.class));
    }
    
    @Test
    public void assertIncrementalDumperCreatorForPostgreSQL() {
        IncrementalDumper actual = IncrementalDumperCreatorFactory.getInstance("PostgreSQL")
                .createIncrementalDumper(mockDumperConfiguration(), walPosition, new SimpleMemoryPipelineChannel(100), new PipelineTableMetaDataLoader(dataSource));
        assertThat(actual, instanceOf(PostgreSQLWalDumper.class));
    }
    
    @Test
    public void assertIncrementalDumperCreatorForOpenGauss() {
        IncrementalDumper actual = IncrementalDumperCreatorFactory.getInstance("openGauss")
                .createIncrementalDumper(mockDumperConfiguration(), walPosition, new SimpleMemoryPipelineChannel(100), new PipelineTableMetaDataLoader(dataSource));
        assertThat(actual, instanceOf(OpenGaussWalDumper.class));
    }
    
    @Test
    public void assertIncrementalDumperCreatorForFixture() {
        IncrementalDumper actual = IncrementalDumperCreatorFactory.getInstance("Fixture")
                .createIncrementalDumper(mockDumperConfiguration(), walPosition, new SimpleMemoryPipelineChannel(100), new PipelineTableMetaDataLoader(dataSource));
        assertThat(actual, instanceOf(FixtureIncrementalDumper.class));
    }
    
    @Test
    public void assertIncrementalDumperCreatorForH2() {
        IncrementalDumper actual = IncrementalDumperCreatorFactory.getInstance("H2")
                .createIncrementalDumper(mockDumperConfiguration(), walPosition, new SimpleMemoryPipelineChannel(100), new PipelineTableMetaDataLoader(dataSource));
        assertThat(actual, instanceOf(FixtureIncrementalDumper.class));
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfig(new StandardPipelineDataSourceConfiguration("jdbc:mysql://127.0.0.1:3306/ds_0", "root", "root"));
        result.setTableNameMap(Collections.singletonMap(new ActualTableName("t_order"), new LogicTableName("t_order")));
        result.setTableNameSchemaNameMapping(new TableNameSchemaNameMapping(Collections.emptyMap()));
        return result;
    }
}
