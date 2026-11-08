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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.dumper;

import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.CreateIncrementalDumperParameter;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.DialectIncrementalDumperCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.WALPosition;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenGaussIncrementalDumperCreatorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final DialectIncrementalDumperCreator creator = DatabaseTypedSPILoader.getService(DialectIncrementalDumperCreator.class, databaseType);
    
    @Test
    void assertCreate() {
        IncrementalDumperContext dumperContext = mock(IncrementalDumperContext.class, RETURNS_DEEP_STUBS);
        when(dumperContext.getCommonContext().getDataSourceConfig()).thenReturn(new StandardPipelineDataSourceConfiguration(Collections.singletonMap("url", "jdbc:mock://127.0.0.1/foo_ds")));
        WALPosition position = mock(WALPosition.class, RETURNS_DEEP_STUBS);
        CreateIncrementalDumperParameter param = new CreateIncrementalDumperParameter(dumperContext, position, null, null, null);
        assertThat(creator.create(param), isA(OpenGaussIncrementalDumper.class));
    }
}
