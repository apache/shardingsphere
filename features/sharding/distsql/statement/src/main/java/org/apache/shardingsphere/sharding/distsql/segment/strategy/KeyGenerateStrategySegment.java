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

package org.apache.shardingsphere.sharding.distsql.segment.strategy;

import lombok.Getter;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.segment.DistSQLSegment;

import java.util.Optional;

/**
 * Key generate strategy segment.
 */
@Getter
public final class KeyGenerateStrategySegment implements DistSQLSegment {
    
    private final String keyGenerateColumn;
    
    private final AlgorithmSegment keyGenerateAlgorithmSegment;
    
    private final String keyGeneratorName;
    
    public KeyGenerateStrategySegment(final String keyGenerateColumn, final AlgorithmSegment keyGenerateAlgorithmSegment) {
        this(keyGenerateColumn, keyGenerateAlgorithmSegment, null);
    }
    
    public KeyGenerateStrategySegment(final String keyGenerateColumn, final String keyGeneratorName) {
        this(keyGenerateColumn, null, keyGeneratorName);
    }
    
    public KeyGenerateStrategySegment(final String keyGenerateColumn, final AlgorithmSegment keyGenerateAlgorithmSegment, final String keyGeneratorName) {
        this.keyGenerateColumn = keyGenerateColumn;
        this.keyGenerateAlgorithmSegment = keyGenerateAlgorithmSegment;
        this.keyGeneratorName = keyGeneratorName;
    }
    
    /**
     * Get algorithm segment.
     *
     * @return algorithm segment
     */
    public Optional<AlgorithmSegment> getAlgorithmSegment() {
        return Optional.ofNullable(keyGenerateAlgorithmSegment);
    }
    
    /**
     * Get key generator name.
     *
     * @return key generator name
     */
    public Optional<String> getKeyGeneratorName() {
        return Optional.ofNullable(keyGeneratorName);
    }
}
