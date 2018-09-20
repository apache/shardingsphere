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

package io.shardingsphere.jdbc.spring.boot.type;

import io.shardingsphere.core.api.ConfigMapContext;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.jdbc.core.ShardingContext;
import io.shardingsphere.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.jdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;
import io.shardingsphere.jdbc.spring.boot.util.EmbedTestingServer;
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
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
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
        assertTrue(dataSource instanceof OrchestrationShardingDataSource);
        Field dataSourceField = OrchestrationShardingDataSource.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        ShardingDataSource shardingDataSource = (ShardingDataSource) dataSourceField.get(dataSource);
        Field field = ShardingDataSource.class.getDeclaredField("shardingContext");
        field.setAccessible(true);
        ShardingContext shardingContext = (ShardingContext) field.get(shardingDataSource);
        for (DataSource each : shardingDataSource.getDataSourceMap().values()) {
            assertThat(((BasicDataSource) each).getMaxTotal(), is(16));
        }
        assertTrue(shardingContext.isShowSQL());
        Map<String, Object> configMap = new ConcurrentHashMap<>();
        configMap.put("key1", "value1");
        assertThat(ConfigMapContext.getInstance().getShardingConfig(), is(configMap));
        
        Field propertiesField = ShardingDataSource.class.getDeclaredField("shardingProperties");
        propertiesField.setAccessible(true);
        ShardingProperties shardingProperties = (ShardingProperties) propertiesField.get(shardingDataSource);
        assertTrue((Boolean) shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW));
        assertThat((Integer) shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE), is(100));
    }
}
