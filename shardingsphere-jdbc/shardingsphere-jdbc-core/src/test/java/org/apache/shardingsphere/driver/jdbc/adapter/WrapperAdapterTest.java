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
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class WrapperAdapterTest {
    
    private ShardingSphereDataSource wrapperAdapter;
    
    @Before
    public void setUp() throws SQLException {
        wrapperAdapter = new ShardingSphereDataSource(
                DefaultSchema.LOGIC_NAME, null, Collections.singletonMap("ds", new MockedDataSource()), Collections.singletonList(mock(RuleConfiguration.class)), new Properties());
    }
    
    @Test
    public void assertUnwrapSuccess() throws SQLException {
        assertThat(wrapperAdapter.unwrap(Object.class), is(wrapperAdapter));
    }
    
    @Test(expected = SQLException.class)
    public void assertUnwrapFailure() throws SQLException {
        wrapperAdapter.unwrap(String.class);
    }
    
    @Test
    public void assertIsWrapperFor() {
        assertTrue(wrapperAdapter.isWrapperFor(Object.class));
    }
}
