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

package org.apache.shardingsphere.spring.boot;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.spring.boot.fixture.TestJndiInitialContextFactory;
import org.apache.shardingsphere.jdbc.test.MockedDataSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.sql.DataSource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootJNDIDataSourceTest.class)
@SpringBootApplication
@ActiveProfiles("jndi")
public class SpringBootJNDIDataSourceTest {
    
    @Resource
    private ShardingSphereDataSource dataSource;
    
    @BeforeClass
    public static void setUp() {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestJndiInitialContextFactory.class.getName());
        TestJndiInitialContextFactory.bind("java:comp/env/jdbc/jndi0", new MockedDataSource());
        TestJndiInitialContextFactory.bind("java:comp/env/jdbc/jndi1", new MockedDataSource());
    }
    
    @Test
    public void assertDatasourceMap() {
        Map<String, DataSource> dataSourceMap = dataSource.getDataSourceMap();
        assertThat(dataSourceMap.size(), is(2));
        assertTrue(dataSourceMap.containsKey("jndi0"));
        assertTrue(dataSourceMap.containsKey("jndi1"));
    }
}
