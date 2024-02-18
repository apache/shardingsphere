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

package org.apache.shardingsphere.driver.jdbc.adapter;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class WrapperAdapterTest {
    
    private ShardingSphereDataSource wrapperAdapter;
    
    @BeforeEach
    void setUp() throws SQLException {
        wrapperAdapter = new ShardingSphereDataSource(
                DefaultDatabase.LOGIC_NAME, null, Collections.singletonMap("ds", new MockedDataSource()), Collections.singletonList(mock(RuleConfiguration.class)), new Properties());
    }
    
    @Test
    void assertUnwrapSuccess() throws SQLException {
        assertThat(wrapperAdapter.unwrap(Object.class), is(wrapperAdapter));
    }
    
    @Test
    void assertUnwrapFailure() {
        assertThrows(SQLException.class, () -> wrapperAdapter.unwrap(String.class));
    }
    
    @Test
    void assertIsWrapperFor() {
        assertTrue(wrapperAdapter.isWrapperFor(Object.class));
    }
}
