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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental;

import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IncrementalDumperCreatorTest {
    
    @Test
    void assertCreateWithStandardPipelineDataSourceConfiguration() {
        StandardPipelineDataSourceConfiguration dataSourceConfig = mock(StandardPipelineDataSourceConfiguration.class);
        when(dataSourceConfig.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        CreateIncrementalDumperParameter param = mock(CreateIncrementalDumperParameter.class, RETURNS_DEEP_STUBS);
        when(param.getContext().getCommonContext().getDataSourceConfig()).thenReturn(dataSourceConfig);
        assertDoesNotThrow(() -> IncrementalDumperCreator.create(param));
    }
    
    @Test
    void assertCreateWithShardingSpherePipelineDataSourceConfiguration() {
        CreateIncrementalDumperParameter param = mock(CreateIncrementalDumperParameter.class, RETURNS_DEEP_STUBS);
        when(param.getContext().getCommonContext().getDataSourceConfig()).thenReturn(mock(ShardingSpherePipelineDataSourceConfiguration.class));
        assertThrows(UnsupportedSQLOperationException.class, () -> IncrementalDumperCreator.create(param));
    }
}
