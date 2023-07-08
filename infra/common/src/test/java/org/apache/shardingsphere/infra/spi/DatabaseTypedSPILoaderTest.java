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

package org.apache.shardingsphere.infra.spi;

import org.apache.shardingsphere.infra.spi.fixture.DatabaseTypedSPIFixture;
import org.apache.shardingsphere.infra.util.spi.exception.ServiceProviderNotFoundServerException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseTypedSPILoaderTest {
    
    @Test
    void assertFindServiceWithTrunkDatabaseType() {
        assertTrue(DatabaseTypedSPILoader.findService(DatabaseTypedSPIFixture.class, "MySQL").isPresent());
    }
    
    @Test
    void assertFindServiceWithBranchDatabaseType() {
        assertTrue(DatabaseTypedSPILoader.findService(DatabaseTypedSPIFixture.class, "MariaDB").isPresent());
    }
    
    @Test
    void assertFindServiceWithUnknownDatabaseType() {
        assertFalse(DatabaseTypedSPILoader.findService(DatabaseTypedSPIFixture.class, "Unknown").isPresent());
    }
    
    @Test
    void assertGetExistedService() {
        assertDoesNotThrow(() -> DatabaseTypedSPILoader.getService(DatabaseTypedSPIFixture.class, "MySQL"));
    }
    
    @Test
    void assertGetNotExistedService() {
        assertThrows(ServiceProviderNotFoundServerException.class, () -> DatabaseTypedSPILoader.getService(DatabaseTypedSPIFixture.class, "Unknown"));
    }
}
