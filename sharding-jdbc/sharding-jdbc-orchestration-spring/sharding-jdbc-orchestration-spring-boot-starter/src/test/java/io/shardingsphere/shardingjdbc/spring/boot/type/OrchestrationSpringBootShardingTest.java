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
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.shardingjdbc.jdbc.core.ShardingContext;
import io.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import io.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;
import io.shardingsphere.shardingjdbc.spring.boot.util.EmbedTestingServer;
import lombok.SneakyThrows;
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
        ConfigMapContext.getInstance().getConfigMap().clear();
    }
    
    @Test
    public void assertWithShardingDataSource() {
        assertTrue(dataSource instanceof OrchestrationShardingDataSource);
        ShardingDataSource shardingDataSource = getFieldValue("dataSource", OrchestrationShardingDataSource.class, dataSource);
        ShardingContext shardingContext = getFieldValue("shardingContext", ShardingDataSource.class, shardingDataSource);
        for (DataSource each : shardingDataSource.getDataSourceMap().values()) {
            assertThat(((BasicDataSource) each).getMaxTotal(), is(16));
        }
        assertTrue(shardingContext.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.SQL_SHOW));
        Map<String, Object> configMap = new ConcurrentHashMap<>();
        configMap.put("key1", "value1");
        assertThat(ConfigMapContext.getInstance().getConfigMap(), is(configMap));
        ShardingProperties shardingProperties = shardingContext.getShardingProperties();
        assertTrue((Boolean) shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW));
        assertThat((Integer) shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE), is(100));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private <T> T getFieldValue(final String fieldName, final Class<?> fieldClass, final Object target) {
        Field field = fieldClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }
}
