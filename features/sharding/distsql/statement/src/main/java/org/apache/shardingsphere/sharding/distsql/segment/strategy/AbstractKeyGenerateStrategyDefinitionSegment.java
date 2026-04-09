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

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.segment.DistSQLSegment;

import java.util.Optional;

/**
 * Abstract key generate strategy definition segment.
 */
public abstract class AbstractKeyGenerateStrategyDefinitionSegment implements DistSQLSegment {
    
    private final String keyGeneratorName;
    
    private final AlgorithmSegment algorithmSegment;
    
    protected AbstractKeyGenerateStrategyDefinitionSegment(final String keyGeneratorName, final AlgorithmSegment algorithmSegment) {
        this.keyGeneratorName = keyGeneratorName;
        this.algorithmSegment = algorithmSegment;
    }
    
    /**
     * Get key generator name.
     *
     * @return generator name
     */
    public Optional<String> getKeyGeneratorName() {
        return Optional.ofNullable(keyGeneratorName);
    }
    
    /**
     * Get algorithm segment.
     *
     * @return algorithm segment
     */
    public Optional<AlgorithmSegment> getAlgorithmSegment() {
        return Optional.ofNullable(algorithmSegment);
    }
}
