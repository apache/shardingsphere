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

package org.apache.shardingsphere.infra.metadata.statistics.collector.opengauss;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.collector.DialectDatabaseStatisticsCollector;
import org.apache.shardingsphere.infra.metadata.statistics.collector.postgresql.PostgreSQLStatisticsCollector;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OpenGaussStatisticsCollectorTest {
    
    private final DialectDatabaseStatisticsCollector collector = DatabaseTypedSPILoader.getService(
            DialectDatabaseStatisticsCollector.class, TypedSPILoader.getService(DatabaseType.class, "openGauss"));
    
    @Mock
    private PostgreSQLStatisticsCollector delegate;
    
    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        Plugins.getMemberAccessor().set(OpenGaussStatisticsCollector.class.getDeclaredField("delegate"), collector, delegate);
    }
    
    @Test
    void assertCollectRowColumnValuesWithTables() throws SQLException {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        collector.collectRowColumnValues("foo_db", "information_schema", "tables", metaData);
        verify(delegate).collectRowColumnValues("foo_db", "information_schema", "tables", metaData);
    }
    
    @Test
    void assertIsStatisticsTables() {
        collector.isStatisticsTables(Collections.emptyMap());
        verify(delegate).isStatisticsTables(Collections.emptyMap());
    }
}
