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

package org.apache.shardingsphere.sharding.algorithm.sharding;

/**
 * Sharding Algorithm Type.
 *
 * @see org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm
 * @see org.apache.shardingsphere.sharding.algorithm.sharding.complex.ComplexInlineShardingAlgorithm
 * @see org.apache.shardingsphere.sharding.algorithm.sharding.hint.HintInlineShardingAlgorithm
 * @see org.apache.shardingsphere.sharding.algorithm.sharding.classbased.ClassBasedShardingAlgorithm
 * @see org.apache.shardingsphere.sharding.algorithm.sharding.datetime.IntervalShardingAlgorithm
 * @see org.apache.shardingsphere.sharding.algorithm.sharding.datetime.AutoIntervalShardingAlgorithm
 * @see org.apache.shardingsphere.sharding.algorithm.sharding.mod.ModShardingAlgorithm
 * @see org.apache.shardingsphere.sharding.algorithm.sharding.mod.HashModShardingAlgorithm
 * @see org.apache.shardingsphere.sharding.algorithm.sharding.range.BoundaryBasedRangeShardingAlgorithm
 * @see org.apache.shardingsphere.sharding.algorithm.sharding.range.VolumeBasedRangeShardingAlgorithm
 */
public enum ShardingAlgorithmType {

    /**
     * Inline Sharding Algorithm.
     */
    INLINE,

    /**
     * Complex Inline Sharding Algorithm.
     */
    COMPLEX_INLINE,

    /**
     * Hint Inline Sharding Algorithm.
     */
    HINT_INLINE,

    /**
     * Class Based Sharding Algorithm.
     */
    CLASS_BASED,

    /**
     * Interval Sharding Algorithm.
     */
    INTERVAL,

    /**
     * Auto Interval Sharding Algorithm.
     */
    AUTO_INTERVAL,

    /**
     * Mod Sharding Algorithm.
     */
    MOD,

    /**
     * Hash Mod Sharding Algorithm.
     */
    HASH_MOD,

    /**
     * Boundary Range Sharding Algorithm.
     */
    BOUNDARY_RANGE,

    /**
     * Volume Range Sharding Algorithm.
     */
    VOLUME_RANGE,

}
