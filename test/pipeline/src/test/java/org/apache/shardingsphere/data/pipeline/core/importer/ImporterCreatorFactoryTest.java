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

package org.apache.shardingsphere.data.pipeline.core.importer;

import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.importer.Importer;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixtureImporter;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixturePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixturePipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.spi.importer.ImporterCreatorFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
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
    public void assertCreateImporter() {
        for (String each : Arrays.asList("MySQL", "PostgreSQL", "openGauss")) {
            Importer actual = ImporterCreatorFactory.getInstance(each).createImporter(createImporterConfiguration(each), dataSourceManager, channel, new FixturePipelineJobProgressListener());
            assertThat(actual, instanceOf(DefaultImporter.class));
        }
    }
    
    @Test
    public void assertCreateImporterForH2() {
        Importer actual = ImporterCreatorFactory.getInstance("H2").createImporter(createImporterConfiguration("H2"), dataSourceManager, channel, new FixturePipelineJobProgressListener());
        assertThat(actual, instanceOf(FixtureImporter.class));
    }
    
    private ImporterConfiguration createImporterConfiguration(final String databaseType) {
        Map<LogicTableName, Set<String>> shardingColumnsMap = Collections.singletonMap(new LogicTableName("t_order"), new HashSet<>(Arrays.asList("order_id", "user_id", "status")));
        PipelineDataSourceConfiguration dataSourceConfig = new FixturePipelineDataSourceConfiguration(DatabaseTypeFactory.getInstance(databaseType));
        return new ImporterConfiguration(dataSourceConfig, shardingColumnsMap, new TableNameSchemaNameMapping(Collections.emptyMap()), 1000, null, 3, 3);
    }
}
