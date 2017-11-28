/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.spring.boot.type;

import io.shardingjdbc.core.api.ConfigMapContext;
import io.shardingjdbc.core.constant.ShardingProperties;
import io.shardingjdbc.core.constant.ShardingPropertiesConstant;
import io.shardingjdbc.core.jdbc.core.ShardingContext;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.spring.boot.util.EmbedTestingServer;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OrchestrationSpringBootShardingTest.class)
@SpringBootApplication
@ActiveProfiles("sharding")
public class OrchestrationSpringBootShardingTest {
    
    @Resource
    private DataSource dataSource;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertWithShardingDataSource() throws NoSuchFieldException, IllegalAccessException {
        assertTrue(dataSource instanceof ShardingDataSource);
        Field field = ShardingDataSource.class.getDeclaredField("shardingContext");
        field.setAccessible(true);
        ShardingContext shardingContext = (ShardingContext) field.get(dataSource);
        for (DataSource each : shardingContext.getShardingRule().getDataSourceMap().values()) {
            assertThat(((BasicDataSource) each).getMaxActive(), is(16));
        }
        assertTrue(shardingContext.isShowSQL());
        Map<String, Object> configMap = new ConcurrentHashMap<>();
        configMap.put("key1", "value1");
        assertThat(ConfigMapContext.getInstance().getShardingConfig(), is(configMap));
        
        Field propertiesField = ShardingDataSource.class.getDeclaredField("shardingProperties");
        propertiesField.setAccessible(true);
        ShardingProperties shardingProperties = (ShardingProperties) propertiesField.get(dataSource);
        assertTrue((Boolean) shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW));
        assertThat((Integer) shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE), is(100));
    }
}
