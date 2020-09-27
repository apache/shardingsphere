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

package org.apache.shardingsphere.spring.boot.datasource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.spring.boot.datasource.prop.impl.CommonDbcp2DataSourcePropertiesSetter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class Dbcp2DataSourcePropertiesSetterTest {
    
    private final CommonDbcp2DataSourcePropertiesSetter dbcp2DataSourcePropertiesSetter = new CommonDbcp2DataSourcePropertiesSetter();
    
    private final BasicDataSource dataSource = new BasicDataSource();
    
    private Environment environment;
    
    @Before
    public void setUp() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.shardingsphere.datasource.primary_ds.type", "org.apache.commons.dbcp2.BasicDataSource");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.primary_ds.connection-properties.test", "test");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.primary_ds.connection-properties.xxx", "yyy");
        environment = mockEnvironment;
    }
    
    @Test
    public void assertPropertiesSet() {
        dbcp2DataSourcePropertiesSetter.propertiesSet(environment, "spring.shardingsphere.datasource.", "primary_ds", dataSource);
        Properties connectionProperties = (Properties) ReflectionTestUtils.getField(dataSource, "connectionProperties");
        assertThat(connectionProperties.getProperty("test"), is("test"));
        assertThat(connectionProperties.getProperty("xxx"), is("yyy"));
    }
    
    @Test
    public void assertGetType() {
        assertThat(dbcp2DataSourcePropertiesSetter.getType(), is("org.apache.commons.dbcp2.BasicDataSource"));
    }
}
