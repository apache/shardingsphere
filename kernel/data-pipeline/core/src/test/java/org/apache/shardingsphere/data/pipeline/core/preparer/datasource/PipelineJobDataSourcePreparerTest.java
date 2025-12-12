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

package org.apache.shardingsphere.data.pipeline.core.preparer.datasource;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.CreateTableConfiguration;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.PrepareTargetSchemasParameter;
import org.apache.shardingsphere.data.pipeline.core.preparer.datasource.param.PrepareTargetTablesParameter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PipelineJobDataSourcePreparerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertPrepareTargetSchemasWithSchemaNotAvailable() {
        PrepareTargetSchemasParameter parameter = new PrepareTargetSchemasParameter(databaseType, null, null);
        assertDoesNotThrow(() -> new PipelineJobDataSourcePreparer(databaseType).prepareTargetSchemas(parameter));
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    void assertPrepareTargetTables() {
        CreateTableConfiguration createTableConfig = mock(CreateTableConfiguration.class, RETURNS_DEEP_STUBS);
        when(createTableConfig.getSourceDataSourceConfig().getDatabaseType()).thenReturn(databaseType);
        PipelineDataSourceManager pipelineDataSourceManager = mock(PipelineDataSourceManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereConnection connection = mock(ShardingSphereConnection.class, RETURNS_DEEP_STUBS);
        when(pipelineDataSourceManager.getDataSource(any()).getConnection()).thenReturn(connection);
        when(connection.getContextManager().getMetaDataContexts().getMetaData()).thenReturn(mock(ShardingSphereMetaData.class));
        PrepareTargetTablesParameter parameter = new PrepareTargetTablesParameter(Collections.singleton(createTableConfig), pipelineDataSourceManager, mock(SQLParserEngine.class), "foo_db");
        assertDoesNotThrow(() -> new PipelineJobDataSourcePreparer(databaseType).prepareTargetTables(parameter));
    }
}
