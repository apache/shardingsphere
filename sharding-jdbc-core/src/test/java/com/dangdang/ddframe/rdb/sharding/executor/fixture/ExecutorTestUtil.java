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

package com.dangdang.ddframe.rdb.sharding.executor.fixture;

import com.dangdang.ddframe.rdb.sharding.config.ShardingProperties;
import com.dangdang.ddframe.rdb.sharding.config.ShardingPropertiesConstant;
import com.dangdang.ddframe.rdb.sharding.executor.ExecutorExceptionHandler;
import com.dangdang.ddframe.rdb.sharding.metrics.MetricsContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutorTestUtil {
    
    public static ShardingProperties createShardingProperties() {
        Properties prop = new Properties();
        prop.setProperty(ShardingPropertiesConstant.METRICS_ENABLE.getKey(), Boolean.TRUE.toString());
        ShardingProperties result = new ShardingProperties(prop);
        MetricsContext.init(result);
        return result;
    }
    
    public static void clear() throws NoSuchFieldException, IllegalAccessException {
        Field field = ExecutorExceptionHandler.class.getDeclaredField("IS_EXCEPTION_THROWN");
        field.setAccessible(true);
        ((ThreadLocal) field.get(ExecutorExceptionHandler.class)).remove();
    }
}
