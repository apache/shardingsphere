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

import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.InventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixtureInventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.SimpleMemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DefaultInventoryDumper;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.MySQLInventoryDumper;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLInventoryDumper;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.InventoryDumperCreatorFactory;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class InventoryDumperCreatorFactoryTest {
    
    @Test
    public void assertInventoryDumperCreatorForMySQL() {
        InventoryDumper actual = createInventoryDumper("MySQL");
        assertThat(actual, instanceOf(MySQLInventoryDumper.class));
    }
    
    @Test
    public void assertInventoryDumperCreatorForPostgreSQL() {
        InventoryDumper actual = createInventoryDumper("PostgreSQL");
        assertThat(actual, instanceOf(PostgreSQLInventoryDumper.class));
    }
    
    @Test
    public void assertInventoryDumperCreatorForOpenGauss() {
        InventoryDumper actual = createInventoryDumper("openGauss");
        assertThat(actual, instanceOf(PostgreSQLInventoryDumper.class));
    }
    
    @Test
    public void assertInventoryDumperCreatorForOracle() {
        InventoryDumper actual = createInventoryDumper("Oracle");
        assertThat(actual, instanceOf(DefaultInventoryDumper.class));
    }
    
    @Test
    public void assertInventoryDumperCreatorForFixture() {
        InventoryDumper actual = createInventoryDumper("Fixture");
        assertThat(actual, instanceOf(FixtureInventoryDumper.class));
    }
    
    private InventoryDumper createInventoryDumper(final String databaseType) {
        PipelineDataSourceWrapper dataSource = null;
        return InventoryDumperCreatorFactory.getInstance(databaseType)
                .createInventoryDumper(mockInventoryDumperConfiguration(), new SimpleMemoryPipelineChannel(100), dataSource, new StandardPipelineTableMetaDataLoader(dataSource));
    }
    
    private InventoryDumperConfiguration mockInventoryDumperConfiguration() {
        DumperConfiguration dumperConfig = mockDumperConfiguration();
        InventoryDumperConfiguration result = new InventoryDumperConfiguration(dumperConfig);
        result.setActualTableName("t_order");
        result.setLogicTableName("t_order");
        return result;
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfig(new StandardPipelineDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL", "root", "root"));
        return result;
    }
}
