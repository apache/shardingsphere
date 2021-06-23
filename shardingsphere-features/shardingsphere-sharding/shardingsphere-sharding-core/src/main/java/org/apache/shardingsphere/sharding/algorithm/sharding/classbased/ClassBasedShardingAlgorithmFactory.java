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

package org.apache.shardingsphere.sharding.algorithm.sharding.classbased;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

/**
 * ShardingSphere class based algorithm factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassBasedShardingAlgorithmFactory {

    /**
     * Create sharding algorithm.
     *
     * @param shardingAlgorithmClassName sharding algorithm class name
     * @param superShardingAlgorithmClass sharding algorithm super class
     * @param <T> class generic type
     * @return sharding algorithm instance
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T extends ShardingAlgorithm> T newInstance(final String shardingAlgorithmClassName, final Class<T> superShardingAlgorithmClass) {
        Class<?> result = Class.forName(shardingAlgorithmClassName);
        if (!superShardingAlgorithmClass.isAssignableFrom(result)) {
            throw new ShardingSphereException("Class %s should be implement %s", shardingAlgorithmClassName, superShardingAlgorithmClass.getName());
        }
        return (T) result.newInstance();
    }
    
}
