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

import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixtureIncrementalDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.SimpleMemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.MySQLIncrementalDumper;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.OpenGaussWalDumper;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.PostgreSQLWalDumper;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.IncrementalDumperCreatorFactory;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class IncrementalDumperCreatorFactoryTest {
    
    @Mock
    private IngestPosition<?> ingestPosition;
    
    @Test
    public void assertIncrementalDumperCreatorForMysql() {
        IncrementalDumper actual = createIncrementalDumper("MySQL");
        assertThat(actual, instanceOf(MySQLIncrementalDumper.class));
    }
    
    @Test
    public void assertIncrementalDumperCreatorForPostgreSQL() {
        IncrementalDumper actual = createIncrementalDumper("PostgreSQL");
        assertThat(actual, instanceOf(PostgreSQLWalDumper.class));
    }
    
    @Test
    public void assertIncrementalDumperCreatorForOpenGauss() {
        IncrementalDumper actual = createIncrementalDumper("openGauss");
        assertThat(actual, instanceOf(OpenGaussWalDumper.class));
    }
    
    @Test
    public void assertIncrementalDumperCreatorForFixture() {
        IncrementalDumper actual = createIncrementalDumper("Fixture");
        assertThat(actual, instanceOf(FixtureIncrementalDumper.class));
    }
    
    @Test
    public void assertIncrementalDumperCreatorForH2() {
        IncrementalDumper actual = createIncrementalDumper("H2");
        assertThat(actual, instanceOf(FixtureIncrementalDumper.class));
    }
    
    private IncrementalDumper createIncrementalDumper(final String databaseType) {
        return IncrementalDumperCreatorFactory.getInstance(databaseType)
                .createIncrementalDumper(mockDumperConfiguration(), ingestPosition, new SimpleMemoryPipelineChannel(100), new StandardPipelineTableMetaDataLoader(null));
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfig(new StandardPipelineDataSourceConfiguration("jdbc:mysql://127.0.0.1:3306/ds_0", "root", "root"));
        result.setTableNameMap(Collections.singletonMap(new ActualTableName("t_order"), new LogicTableName("t_order")));
        result.setTableNameSchemaNameMapping(new TableNameSchemaNameMapping(Collections.emptyMap()));
        return result;
    }
}
