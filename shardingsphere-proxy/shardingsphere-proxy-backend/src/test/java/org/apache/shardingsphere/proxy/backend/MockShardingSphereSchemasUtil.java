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

package org.apache.shardingsphere.proxy.backend;

import lombok.SneakyThrows;
import org.apache.shardingsphere.proxy.backend.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.schema.ShardingSphereSchemas;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public final class MockShardingSphereSchemasUtil {
    
    /**
     * Set schemas for global registry.
     * 
     * @param prefix prefix of schema
     * @param size size of schemas
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static void setSchemas(final String prefix, final int size) {
        Field field = ShardingSphereSchemas.getInstance().getClass().getDeclaredField("schemas");
        field.setAccessible(true);
        field.set(ShardingSphereSchemas.getInstance(), mockSchemas(prefix, size));
    }
    
    private static Map<String, ShardingSphereSchema> mockSchemas(final String prefix, final int size) {
        Map<String, ShardingSphereSchema> result = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            result.put(prefix + "_" + i, mock(ShardingSphereSchema.class));
        }
        return result;
    }
}
