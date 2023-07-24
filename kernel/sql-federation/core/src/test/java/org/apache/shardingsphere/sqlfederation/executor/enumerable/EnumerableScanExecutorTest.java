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

package org.apache.shardingsphere.sqlfederation.executor.enumerable;

import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.context.OptimizerContext;
import org.apache.shardingsphere.sqlfederation.executor.TableScanExecutorContext;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnumerableScanExecutorTest {
    
    @Test
    void assertExecuteWithStatistics() {
        OptimizerContext optimizerContext = mock(OptimizerContext.class, RETURNS_DEEP_STUBS);
        when(optimizerContext.getParserContext(any()).getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        TableScanExecutorContext executorContext = mock(TableScanExecutorContext.class);
        when(executorContext.getDatabaseName()).thenReturn("db");
        when(executorContext.getSchemaName()).thenReturn("pg_catalog");
        ShardingSphereStatistics statistics = mock(ShardingSphereStatistics.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabaseData databaseData = mock(ShardingSphereDatabaseData.class, RETURNS_DEEP_STUBS);
        when(statistics.getDatabaseData().get("db")).thenReturn(databaseData);
        ShardingSphereSchemaData schemaData = mock(ShardingSphereSchemaData.class, RETURNS_DEEP_STUBS);
        when(databaseData.getSchemaData().get("pg_catalog")).thenReturn(schemaData);
        ShardingSphereTableData tableData = mock(ShardingSphereTableData.class);
        when(tableData.getRows()).thenReturn(Collections.singletonList(new ShardingSphereRowData(Collections.singletonList(1))));
        when(schemaData.getTableData().get("test")).thenReturn(tableData);
        ShardingSphereTable shardingSphereTable = mock(ShardingSphereTable.class);
        when(shardingSphereTable.getName()).thenReturn("test");
        Enumerable<Object> enumerable = new EnumerableScanExecutor(null, null, null, optimizerContext, null, executorContext, statistics)
                .execute(shardingSphereTable, mock(EnumerableScanExecutorContext.class));
        try (Enumerator<Object> actual = enumerable.enumerator()) {
            actual.moveNext();
            Object row = actual.current();
            assertThat(row, instanceOf(Object[].class));
            assertThat(((Object[]) row)[0], is(1));
        }
    }
}
