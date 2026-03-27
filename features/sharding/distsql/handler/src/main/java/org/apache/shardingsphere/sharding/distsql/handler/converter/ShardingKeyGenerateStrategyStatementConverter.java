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

package org.apache.shardingsphere.sharding.distsql.handler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.keygen.KeyGenerateStrategiesConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.ColumnKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.infra.config.keygen.impl.SequenceKeyGenerateStrategiesRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.AbstractKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ColumnKeyGenerateStrategyDefinitionSegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.SequenceKeyGenerateStrategyDefinitionSegment;

/**
 * Sharding key generate strategy statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingKeyGenerateStrategyStatementConverter {
    
    /**
     * Create key generate strategies configuration.
     *
     * @param keyGenerateStrategySegment key generate strategy segment
     * @param keyGeneratorName key generator name
     * @return key generate strategies configuration
     */
    public static KeyGenerateStrategiesConfiguration createKeyGenerateStrategiesConfig(final AbstractKeyGenerateStrategyDefinitionSegment keyGenerateStrategySegment, final String keyGeneratorName) {
        return keyGenerateStrategySegment instanceof ColumnKeyGenerateStrategyDefinitionSegment
                ? new ColumnKeyGenerateStrategiesRuleConfiguration(keyGeneratorName, ((ColumnKeyGenerateStrategyDefinitionSegment) keyGenerateStrategySegment).getTableName(),
                ((ColumnKeyGenerateStrategyDefinitionSegment) keyGenerateStrategySegment).getColumnName())
                : new SequenceKeyGenerateStrategiesRuleConfiguration(keyGeneratorName, ((SequenceKeyGenerateStrategyDefinitionSegment) keyGenerateStrategySegment).getSequenceName());
    }
    
    /**
     * Get key generator name.
     *
     * @param strategyName strategy name
     * @param keyGenerateStrategySegment key generate strategy segment
     * @return key generator name
     * @throws IllegalArgumentException when both key generator name and algorithm segment are absent
     */
    public static String getKeyGeneratorName(final String strategyName, final AbstractKeyGenerateStrategyDefinitionSegment keyGenerateStrategySegment) {
        if (keyGenerateStrategySegment.getKeyGeneratorName().isPresent()) {
            return keyGenerateStrategySegment.getKeyGeneratorName().get();
        } else if (keyGenerateStrategySegment.getAlgorithmSegment().isPresent()) {
            return ShardingKeyGenerateStrategyStatementConverter.createKeyGeneratorName(strategyName, keyGenerateStrategySegment.getAlgorithmSegment().get());
        } else {
            throw new IllegalArgumentException("Either key generator name or algorithm segment must be provided.");
        }
    }
    
    private static String createKeyGeneratorName(final String strategyName, final AlgorithmSegment algorithmSegment) {
        return String.format("%s_%s", strategyName, algorithmSegment.getName()).toLowerCase();
    }
}
