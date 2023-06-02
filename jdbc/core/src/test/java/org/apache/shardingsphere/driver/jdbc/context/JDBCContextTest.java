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

package org.apache.shardingsphere.driver.jdbc.context;

import org.apache.shardingsphere.driver.state.circuit.datasource.CircuitBreakerDataSource;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JDBCContextTest {
    
    @Test
    void assertGetCachedDatabaseMetaDataForNullValue() throws Exception {
        assertNull(new JDBCContext(Collections.emptyMap()).getCachedDatabaseMetaData());
    }
    
    @Test
    void assertGetCachedDatabaseMetaDataForSingleValue() throws SQLException {
        assertNotNull(new JDBCContext(Collections.singletonMap("foo_db", new CircuitBreakerDataSource())).getCachedDatabaseMetaData());
    }
    
    @Test
    void assertGetCachedDatabaseMetaDataAfterRefreshingExisting() throws SQLException {
        JDBCContext jdbcContext = new JDBCContext(Collections.singletonMap("foo_db", new CircuitBreakerDataSource()));
        jdbcContext.refreshCachedDatabaseMetaData(mock(DataSourceChangedEvent.class));
        assertNull(jdbcContext.getCachedDatabaseMetaData());
    }
}
