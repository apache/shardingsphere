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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Class based sharding strategy.
 */
@RequiredArgsConstructor
@Getter
public enum ClassBasedShardingAlgorithmStrategy {

    /**
     * The sharding strategy is standard.
     */
    STANDARD("standard"),

    /**
     * The sharding strategy is complex.
     */
    COMPLEX("complex"),

    /**
     * The sharding strategy is hint.
     */
    HINT("hint");

    private final String value;

    /**
     * Value of Class based sharding strategy.
     *
     * @param value value
     * @return Class based sharding strategy
     */
    public static ClassBasedShardingAlgorithmStrategy valueFrom(final String value) {
        for (ClassBasedShardingAlgorithmStrategy each : values()) {
            if (each.value.equals(value)) {
                return each;
            }
        }
        throw new IllegalArgumentException(String.format("Illegal class based sharding strategy value %d", value));
    }
}
