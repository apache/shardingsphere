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

package org.apache.shardingsphere.infra.config.datasource.pool.metadata.impl;

import org.apache.shardingsphere.infra.config.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.config.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DefaultDataSourcePoolCreatorTest {
    
    @Test
    public void assertCreateDataSourceConfiguration() {
        assertThat(new DataSourcePoolCreator("Default").createDataSourceProperties(createDataSource()), is(createDataSourceProperties()));
    }
    
    private DataSource createDataSource() {
        MockedDataSource result = new MockedDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    @Test
    public void assertCreateDataSource() {
        MockedDataSource actual = (MockedDataSource) new DataSourcePoolCreator("Default").createDataSource(createDataSourceProperties());
        assertThat(actual.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actual.getUrl(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
    }
    
    private DataSourceProperties createDataSourceProperties() {
        DataSourceProperties result = new DataSourceProperties(MockedDataSource.class.getCanonicalName());
        result.getProps().put("driverClassName", "org.h2.Driver");
        result.getProps().put("url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.getProps().put("username", "root");
        result.getProps().put("password", "root");
        return result;
    }
}
