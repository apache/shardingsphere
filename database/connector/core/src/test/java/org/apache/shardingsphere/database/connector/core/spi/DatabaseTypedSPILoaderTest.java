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

package org.apache.shardingsphere.database.connector.core.spi;

import org.apache.shardingsphere.database.connector.core.spi.fixture.DatabaseTypedSPIFixture;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DatabaseTypedSPILoaderTest {
    
    @Test
    void assertFindServiceWithTrunkDatabaseType() {
        assertTrue(DatabaseTypedSPILoader.findService(DatabaseTypedSPIFixture.class, TypedSPILoader.getService(DatabaseType.class, "TRUNK")).isPresent());
    }
    
    @Test
    void assertFindServiceWithBranchDatabaseType() {
        assertTrue(DatabaseTypedSPILoader.findService(DatabaseTypedSPIFixture.class, TypedSPILoader.getService(DatabaseType.class, "BRANCH")).isPresent());
    }
    
    @Test
    void assertServiceNotFound() {
        assertFalse(DatabaseTypedSPILoader.findService(DatabaseTypedSPIFixture.class, mock(DatabaseType.class)).isPresent());
    }
    
    @Test
    void assertFindServiceWithTrunkDatabaseTypeAndProperties() {
        assertTrue(DatabaseTypedSPILoader.findService(DatabaseTypedSPIFixture.class, TypedSPILoader.getService(DatabaseType.class, "TRUNK"), new Properties()).isPresent());
    }
    
    @Test
    void assertFindServiceWithBranchDatabaseTypeAndProperties() {
        assertTrue(DatabaseTypedSPILoader.findService(DatabaseTypedSPIFixture.class, TypedSPILoader.getService(DatabaseType.class, "BRANCH"), new Properties()).isPresent());
    }
    
    @Test
    void assertServiceNotFoundWithProperties() {
        assertFalse(DatabaseTypedSPILoader.findService(DatabaseTypedSPIFixture.class, mock(DatabaseType.class), new Properties()).isPresent());
    }
    
    @Test
    void assertGetService() {
        assertDoesNotThrow(() -> DatabaseTypedSPILoader.getService(DatabaseTypedSPIFixture.class, TypedSPILoader.getService(DatabaseType.class, "TRUNK")));
    }
    
    @Test
    void assertGetServiceWithServiceProviderNotFoundException() {
        assertThrows(ServiceProviderNotFoundException.class, () -> DatabaseTypedSPILoader.getService(DatabaseTypedSPIFixture.class, mock(DatabaseType.class)));
    }
    
    @Test
    void assertGetServiceWithProperties() {
        assertDoesNotThrow(() -> DatabaseTypedSPILoader.getService(DatabaseTypedSPIFixture.class, TypedSPILoader.getService(DatabaseType.class, "TRUNK"), new Properties()));
    }
    
    @Test
    void assertGetServiceWithPropertiesAndThrowsServiceProviderNotFoundException() {
        assertThrows(ServiceProviderNotFoundException.class, () -> DatabaseTypedSPILoader.getService(DatabaseTypedSPIFixture.class, mock(DatabaseType.class), new Properties()));
    }
}
