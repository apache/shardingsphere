/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.spring.boot.type;

import io.shardingsphere.api.ConfigMapContext;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.MasterSlaveDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootMasterSlaveTest.class)
@SpringBootApplication
@ActiveProfiles("masterslave")
public class SpringBootMasterSlaveTest {
    
    @Resource
    private DataSource dataSource;
    
    @BeforeClass
    public static void setUp() {
        ConfigMapContext.getInstance().getConfigMap().clear();
    }
    
    @Test
    public void assertWithMasterSlaveDataSource() {
        assertTrue(dataSource instanceof MasterSlaveDataSource);
        for (DataSource each : ((MasterSlaveDataSource) dataSource).getDataSourceMap().values()) {
            assertThat(((BasicDataSource) each).getMaxTotal(), is(100));
        }
        Map<String, Object> configMap = new ConcurrentHashMap<>();
        configMap.put("key1", "value1");
        configMap.put("key2", "value1");
        configMap.put("username", "root");
        assertThat(ConfigMapContext.getInstance().getConfigMap(), is(configMap));
    }
}
