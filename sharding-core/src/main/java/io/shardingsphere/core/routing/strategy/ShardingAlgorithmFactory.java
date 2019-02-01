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

package io.shardingsphere.core.routing.strategy;

import io.shardingsphere.core.exception.ShardingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding algorithm factory.
 * 
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingAlgorithmFactory {
    
    /**
     * Create sharding algorithm.
     * 
     * @param shardingAlgorithmClassName sharding algorithm class name
     * @param superShardingAlgorithmClass sharding algorithm super class
     * @param <T> class generic type
     * @return sharding algorithm instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends ShardingAlgorithm> T newInstance(final String shardingAlgorithmClassName, final Class<T> superShardingAlgorithmClass) {
        try {
            Class<?> result = Class.forName(shardingAlgorithmClassName);
            if (!superShardingAlgorithmClass.isAssignableFrom(result)) {
                throw new ShardingException("Class %s should be implement %s", shardingAlgorithmClassName, superShardingAlgorithmClass.getName());
            }
            return (T) result.newInstance();
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingException(ex);
        }
    }
}
