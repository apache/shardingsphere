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

package org.apache.shardingsphere.shardingjdbc.spring.boot.type;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.ShardingRuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShadowDataSource;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootShadowShardingTest.class)
@SpringBootApplication
@ActiveProfiles("shadow-sharding")
public class SpringBootShadowShardingTest {
    
    @Resource
    private DataSource dataSource;
    
    @Test
    public void assertSqlShow() {
        assertTrue(((ShadowDataSource) dataSource).getRuntimeContext().getProperties().<Boolean>getValue(PropertiesConstant.SQL_SHOW));
    }
    
    @Test
    public void assertWithShadowDataSource() {
        assertTrue(dataSource instanceof ShadowDataSource);
        assertActualDatasource();
        assertShadowDatasource();
    }
    
    private void assertActualDatasource() {
        DataSource dataSource = ((ShadowDataSource) this.dataSource).getRuntimeContext().getActualDataSource();
        ShardingRuntimeContext runtimeContext = getFieldValue("runtimeContext", ShardingDataSource.class, dataSource);
        for (DataSource each : ((ShardingDataSource) dataSource).getDataSourceMap().values()) {
            assertThat(((BasicDataSource) each).getMaxTotal(), is(100));
        }
        assertTrue(runtimeContext.getProperties().<Boolean>getValue(PropertiesConstant.SQL_SHOW));
        ShardingSphereProperties properties = runtimeContext.getProperties();
        assertTrue((Boolean) properties.getValue(PropertiesConstant.SQL_SHOW));
        assertThat((Integer) properties.getValue(PropertiesConstant.EXECUTOR_SIZE), is(100));
    }
    
    private void assertShadowDatasource() {
        DataSource dataSource = ((ShadowDataSource) this.dataSource).getRuntimeContext().getShadowDataSource();
        ShardingRuntimeContext runtimeContext = getFieldValue("runtimeContext", ShardingDataSource.class, dataSource);
        for (DataSource each : ((ShardingDataSource) dataSource).getDataSourceMap().values()) {
            assertThat(((BasicDataSource) each).getMaxTotal(), is(10));
        }
        assertTrue(runtimeContext.getProperties().<Boolean>getValue(PropertiesConstant.SQL_SHOW));
        ShardingSphereProperties properties = runtimeContext.getProperties();
        assertTrue((Boolean) properties.getValue(PropertiesConstant.SQL_SHOW));
        assertThat((Integer) properties.getValue(PropertiesConstant.EXECUTOR_SIZE), is(100));
    }
    
    @Test
    public void assertWithShadowRule() {
        ShadowRule shadowRule = ((ShadowDataSource) dataSource).getRuntimeContext().getRule();
        assertThat(shadowRule.getColumn(), is("shadow"));
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private <T> T getFieldValue(final String fieldName, final Class<?> fieldClass, final Object target) {
        Field field = fieldClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }
}
