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

package org.apache.shardingsphere.transaction.base.seata.at;

import io.seata.rm.datasource.DataSourceProxy;
import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SeataATShardingTransactionManagerTest {
    
    private static MockSeataServer mockSeataServer = new MockSeataServer();
    
    private final DataSource dataSource = getDataSource();
    
    private final SeataATShardingTransactionManager seataATShardingTransactionManager = new SeataATShardingTransactionManager();
    
    @BeforeClass
    @SneakyThrows
    public static void before() {
        
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                mockSeataServer.start();
            }
        });
        while (true) {
            if (mockSeataServer.getInitialized().get()) {
                return;
            }
        }
    }

    @AfterClass
    public static void after() {
        mockSeataServer.shutdown();
    }
    
    @Before
    public void setUp() {
        seataATShardingTransactionManager.init(DatabaseType.MySQL, getResourceDataSources());
    }
    
    private DataSource getDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setUrl("jdbc:h2:mem:demo_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        result.setUsername("sa");
        result.setPassword("");
        return result;
    }
    
    private Collection<ResourceDataSource> getResourceDataSources() {
        return Collections.singletonList(new ResourceDataSource("demo_ds", dataSource));
    }
    
    @Test
    public void assertInit() {
        Map<String, DataSource> actual = getShardingDataSourceMap();
        assertThat(actual.size(), is(1));
        assertThat(actual.get("demo_ds"), instanceOf(DataSourceProxy.class));
    }
    
    @SneakyThrows
    @SuppressWarnings("unchecked")
    private Map<String, DataSource> getShardingDataSourceMap() {
        Field field = seataATShardingTransactionManager.getClass().getDeclaredField("dataSourceMap");
        field.setAccessible(true);
        return (Map<String, DataSource>) field.get(seataATShardingTransactionManager);
    }
}
