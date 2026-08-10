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

package org.apache.shardingsphere.infra.metadata.statistics.builder.dialect;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.DialectStatisticsAppender;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OpenGaussStatisticsAppenderTest {
    
    private final DialectStatisticsAppender appender = DatabaseTypedSPILoader.getService(
            DialectStatisticsAppender.class, TypedSPILoader.getService(DatabaseType.class, "openGauss"));
    
    @Mock
    private PostgreSQLStatisticsAppender delegate;
    
    private PostgreSQLStatisticsAppender originalDelegate;
    
    @Mock
    private DatabaseStatistics databaseStatistics;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        originalDelegate = (PostgreSQLStatisticsAppender) Plugins.getMemberAccessor().get(OpenGaussStatisticsAppender.class.getDeclaredField("delegate"), appender);
        Plugins.getMemberAccessor().set(OpenGaussStatisticsAppender.class.getDeclaredField("delegate"), appender, delegate);
    }
    
    @AfterEach
    void tearDown() throws NoSuchFieldException, IllegalAccessException {
        Plugins.getMemberAccessor().set(OpenGaussStatisticsAppender.class.getDeclaredField("delegate"), appender, originalDelegate);
    }
    
    @Test
    void assertAppend() {
        appender.append(databaseStatistics, database);
        verify(delegate).append(databaseStatistics, database);
    }
}
