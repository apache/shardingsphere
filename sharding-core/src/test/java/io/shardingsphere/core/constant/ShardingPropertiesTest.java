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

package io.shardingsphere.core.constant;

import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingPropertiesTest {
    
    private final Properties prop = new Properties();
    
    private ShardingProperties shardingProperties;
    
    @Before
    public void setUp() {
        prop.put(ShardingPropertiesConstant.SQL_SHOW.getKey(), "true");
        prop.put(ShardingPropertiesConstant.EXECUTOR_SIZE.getKey(), "10");
        shardingProperties = new ShardingProperties(prop);
    }
    
    @Test
    public void assertGetValueForDefaultValue() {
        ShardingProperties shardingProperties = new ShardingProperties(new Properties());
        boolean actualSQLShow = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        assertThat(actualSQLShow, is(Boolean.valueOf(ShardingPropertiesConstant.SQL_SHOW.getDefaultValue())));
        int executorMaxSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        assertThat(executorMaxSize, is(Integer.valueOf(ShardingPropertiesConstant.EXECUTOR_SIZE.getDefaultValue())));
    }
    
    @Test
    public void assertGetValueForBoolean() {
        boolean showSql = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        assertTrue(showSql);
    }
    
    @Test
    public void assertGetValueForInteger() {
        int actualExecutorMaxSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        assertThat(actualExecutorMaxSize, is(10));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertValidateFailure() {
        Properties prop = new Properties();
        prop.put(ShardingPropertiesConstant.SQL_SHOW.getKey(), "error");
        prop.put(ShardingPropertiesConstant.EXECUTOR_SIZE.getKey(), "error");
        prop.put("other", "other");
        new ShardingProperties(prop);
    }
}
