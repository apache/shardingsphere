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

package io.shardingsphere.shardingproxy.backend;

import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Mock global registry util.
 *
 * @author zhaojun
 */
public class MockGlobalRegistryUtil {
    
    /**
     * set logic schemas for global registry.
     * @param prefix prefix of schema
     * @param size size of schemas
     */
    @SneakyThrows
    public static void setLogicSchemas(final String prefix, final int size) {
        Field field = GlobalRegistry.getInstance().getClass().getDeclaredField("logicSchemas");
        field.setAccessible(true);
        field.set(GlobalRegistry.getInstance(), mockLogicSchemas(prefix, size));
    }
    
    private static Map<String, LogicSchema> mockLogicSchemas(final String prefix, final int size) {
        Map<String, LogicSchema> result = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            result.put(prefix + "_" + i, mock(LogicSchema.class));
        }
        return result;
    }
}
