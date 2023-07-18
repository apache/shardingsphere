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

package org.apache.shardingsphere.infra.datasource.props;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.spi.DataSourceMetaData;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDriver;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypeFactory.class)
class DataSourcePropertiesValidatorTest {
    
    @BeforeAll
    static void setUp() throws ClassNotFoundException {
        Class.forName(MockedDriver.class.getName());
    }
    
    @Test
    void assertValidateSuccess() {
        when(DatabaseTypeFactory.get("mock:jdbc").getDataSourceMetaData("mock:jdbc", null)).thenReturn(mock(DataSourceMetaData.class, RETURNS_DEEP_STUBS));
        assertTrue(new DataSourcePropertiesValidator().validate(
                Collections.singletonMap("name", new DataSourceProperties(HikariDataSource.class.getName(), Collections.singletonMap("jdbcUrl", "mock:jdbc")))).isEmpty());
    }
    
    @Test
    void assertValidateFailed() {
        when(DatabaseTypeFactory.get("mock:jdbc:invalid").getDataSourceMetaData("mock:jdbc:invalid", null)).thenReturn(mock(DataSourceMetaData.class, RETURNS_DEEP_STUBS));
        Collection<String> actual = new DataSourcePropertiesValidator().validate(
                Collections.singletonMap("name", new DataSourceProperties(HikariDataSource.class.getName(), Collections.singletonMap("jdbcUrl", "mock:jdbc:invalid"))));
        assertThat(actual, is(Collections.singletonList("Invalid data source `name`, error message is: Invalid URL.")));
    }
}
