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

package org.apache.shardingsphere.data.pipeline.scenario.migration.metadata.processor;

import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.config.processor.JobConfigurationChangedProcessor;
import org.apache.shardingsphere.data.pipeline.core.preparer.incremental.IncrementalTaskPositionManager;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJob;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MigrationJobConfigurationChangedProcessorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Fixture");
    
    @SuppressWarnings("rawtypes")
    private final JobConfigurationChangedProcessor processor = TypedSPILoader.getService(JobConfigurationChangedProcessor.class, "MIGRATION");
    
    @SuppressWarnings("unchecked")
    @Test
    void assertCreateJob() {
        assertThat(processor.createJob(mock(MigrationJobConfiguration.class)), isA(MigrationJob.class));
    }
    
    @Test
    void assertClean() throws SQLException {
        JobConfiguration jobConfig = mock(JobConfiguration.class);
        when(jobConfig.getJobParameter()).thenReturn(createJobParameter());
        AtomicInteger constructionIndex = new AtomicInteger();
        try (
                MockedConstruction<IncrementalTaskPositionManager> mockedConstruction = mockConstruction(IncrementalTaskPositionManager.class,
                        (mock, context) -> {
                            if (1 == constructionIndex.getAndIncrement()) {
                                doThrow(SQLException.class).when(mock).destroyPosition(eq("job-branches"), any(PipelineDataSourceConfiguration.class));
                            }
                        })) {
            assertDoesNotThrow(() -> processor.clean(jobConfig));
            assertThat(mockedConstruction.constructed().size(), is(2));
            verify(mockedConstruction.constructed().get(0)).destroyPosition(eq("job-branches"), any(PipelineDataSourceConfiguration.class));
            verify(mockedConstruction.constructed().get(1)).destroyPosition(eq("job-branches"), any(PipelineDataSourceConfiguration.class));
        }
    }
    
    private String createJobParameter() {
        Map<String, PipelineDataSourceConfiguration> sources = new LinkedHashMap<>(2, 1F);
        sources.put("ds_0", createPipelineDataSourceConfiguration("source_db_0"));
        sources.put("ds_1", createPipelineDataSourceConfiguration("source_db_1"));
        PipelineDataSourceConfiguration target = createPipelineDataSourceConfiguration("target_db");
        JobDataNodeLine jobDataNodeLine = new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("t_order", Collections.singletonList(new DataNode("ds_0.t_order")))));
        MigrationJobConfiguration jobConfig = new MigrationJobConfiguration("job-branches", "logic_db", databaseType, databaseType, sources, target,
                Collections.singletonList("t_order"), Collections.singletonMap("t_order", "public"), jobDataNodeLine, Collections.singletonList(jobDataNodeLine), 1, 1);
        return YamlEngine.marshal(new YamlMigrationJobConfigurationSwapper().swapToYamlConfiguration(jobConfig));
    }
    
    private PipelineDataSourceConfiguration createPipelineDataSourceConfiguration(final String databaseName) {
        Map<String, Object> props = new LinkedHashMap<>(4, 1F);
        props.put("url", String.format("jdbc:mysql://localhost:3306/%s", databaseName));
        props.put("username", "root");
        props.put("password", "pwd");
        props.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        return new StandardPipelineDataSourceConfiguration(props);
    }
}
