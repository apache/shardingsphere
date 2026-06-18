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

package org.apache.shardingsphere.database.connector.core.jdbcurl.judger;

import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class DatabaseInstanceJudgeEngineTest {
    
    @Mock
    private DatabaseType databaseType;
    
    private DatabaseInstanceJudgeEngine engine;
    
    @BeforeEach
    void setUp() {
        engine = new DatabaseInstanceJudgeEngine(databaseType);
    }
    
    @Test
    void assertIsInSameDatabaseInstanceWithDialectJudger() {
        DialectDatabaseInstanceJudger dialectDatabaseInstanceJudger = mock(DialectDatabaseInstanceJudger.class);
        when(DatabaseTypedSPILoader.findService(DialectDatabaseInstanceJudger.class, databaseType)).thenReturn(Optional.of(dialectDatabaseInstanceJudger));
        ConnectionProperties connectionProps1 = new ConnectionProperties("hostA", 3306, "catalog1", "schema1", new Properties());
        ConnectionProperties connectionProps2 = new ConnectionProperties("hostB", 3306, "catalog2", "schema2", new Properties());
        when(dialectDatabaseInstanceJudger.isInSameDatabaseInstance(connectionProps1, connectionProps2)).thenReturn(true);
        assertTrue(engine.isInSameDatabaseInstance(connectionProps1, connectionProps2));
    }
    
    @Test
    void assertIsInSameDatabaseInstanceWithDefaultJudger() {
        when(DatabaseTypedSPILoader.findService(DialectDatabaseInstanceJudger.class, databaseType)).thenReturn(Optional.empty());
        ConnectionProperties connectionProps1 = new ConnectionProperties("hostC", 5432, "catalog3", "schema3", new Properties());
        ConnectionProperties connectionProps2 = new ConnectionProperties("hostC", 5432, "catalog4", "schema4", new Properties());
        assertTrue(engine.isInSameDatabaseInstance(connectionProps1, connectionProps2));
    }
}
