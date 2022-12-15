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

package org.apache.shardingsphere.test.it.data.pipeline.core.importer;

import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.importer.Importer;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.importer.DataSourceImporter;
import org.apache.shardingsphere.data.pipeline.core.importer.connector.DataSourceImporterConnector;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterCreatorFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.test.it.data.pipeline.core.fixture.FixtureImporter;
import org.apache.shardingsphere.test.it.data.pipeline.core.fixture.FixtureInventoryIncrementalJobItemContext;
import org.apache.shardingsphere.test.it.data.pipeline.core.fixture.FixturePipelineDataSourceConfiguration;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ImporterCreatorFactoryTest {
    
    @Mock
    private PipelineDataSourceManager dataSourceManager;
    
    @Mock
    private PipelineChannel channel;
    
    @Test
    public void assertCreateDataSourceImporter() {
        Importer actual = ImporterCreatorFactory.getInstance("DataSource").createImporter(createImporterConfiguration(), new DataSourceImporterConnector(dataSourceManager), channel,
                new FixtureInventoryIncrementalJobItemContext());
        assertThat(actual, instanceOf(DataSourceImporter.class));
    }
    
    @Test
    public void assertCreateFixtureImporter() {
        Importer actual = ImporterCreatorFactory.getInstance("FIXTURE").createImporter(createImporterConfiguration(), new DataSourceImporterConnector(dataSourceManager), channel,
                new FixtureInventoryIncrementalJobItemContext());
        assertThat(actual, instanceOf(FixtureImporter.class));
    }
    
    private ImporterConfiguration createImporterConfiguration() {
        Map<LogicTableName, Set<String>> shardingColumnsMap = Collections.singletonMap(new LogicTableName("t_order"), new HashSet<>(Arrays.asList("order_id", "user_id", "status")));
        PipelineDataSourceConfiguration dataSourceConfig = new FixturePipelineDataSourceConfiguration(DatabaseTypeFactory.getInstance("H2"));
        return new ImporterConfiguration(dataSourceConfig, shardingColumnsMap, new TableNameSchemaNameMapping(Collections.emptyMap()), 1000, null, 3, 3);
    }
}
