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

package org.apache.shardingsphere.spring.boot.jndi;

import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.spring.boot.jndi.fixture.InitialDataSourceInitialContextFactory;
import org.apache.shardingsphere.test.mock.MockedDataSource;
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
import java.lang.reflect.Field;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InitialDataSourceInitialContextFactory.class.getName());
        InitialDataSourceInitialContextFactory.bind("java:comp/env/jdbc/ds0", new MockedDataSource());
        InitialDataSourceInitialContextFactory.bind("java:comp/env/jdbc/ds1", new MockedDataSource());
        InitialDataSourceInitialContextFactory.bind("java:comp/env/jdbc/write_ds", new MockedDataSource());
    }
    
    @Test
    public void assertDataSources() {
        Map<String, DataSource> dataSources = getContextManager(dataSource).getMetaDataContexts().getMetaData().getDatabase("foo_db").getResource().getDataSources();
        assertThat(dataSources.size(), is(3));
        assertTrue(dataSources.containsKey("read_ds_0"));
        assertTrue(dataSources.containsKey("read_ds_1"));
        assertTrue(dataSources.containsKey("write_ds"));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private ContextManager getContextManager(final ShardingSphereDataSource dataSource) {
        Field field = ShardingSphereDataSource.class.getDeclaredField("contextManager");
        field.setAccessible(true);
        return (ContextManager) field.get(dataSource);
    }
}
