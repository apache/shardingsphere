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

import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ImporterConfigurationTest {
    
    @Test
    void assertGetShardingColumns() {
        ImporterConfiguration importerConfig = new ImporterConfiguration(
                mock(PipelineDataSourceConfiguration.class), Collections.singletonMap(new ShardingSphereIdentifier("foo_tbl"), Collections.singleton("foo_col")),
                mock(TableAndSchemaNameMapper.class), 1, mock(JobRateLimitAlgorithm.class), 1, 1);
        assertThat(importerConfig.getShardingColumns("foo_tbl"), is(Collections.singleton("foo_col")));
    }
    
    @Test
    void assertFindSchemaName() {
        PipelineDataSourceConfiguration dataSourceConfig = mock(PipelineDataSourceConfiguration.class);
        when(dataSourceConfig.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        ImporterConfiguration importerConfig = new ImporterConfiguration(dataSourceConfig, Collections.emptyMap(), mock(TableAndSchemaNameMapper.class), 1, mock(JobRateLimitAlgorithm.class), 1, 1);
        assertFalse(importerConfig.findSchemaName("foo_schema").isPresent());
    }
}
