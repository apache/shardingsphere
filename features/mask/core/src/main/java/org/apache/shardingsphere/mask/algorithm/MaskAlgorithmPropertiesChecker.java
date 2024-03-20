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

package org.apache.shardingsphere.mask.algorithm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Properties;

/**
 * Mask algorithm properties checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaskAlgorithmPropertiesChecker {
    
    /**
     * Check single char configuration.
     * 
     * @param props props
     * @param singleCharConfigKey single char config key
     * @param algorithm mask algorithm
     */
    public static void checkSingleCharConfiguration(final Properties props, final String singleCharConfigKey, final MaskAlgorithm<?, ?> algorithm) {
        checkRequiredPropertyConfiguration(props, singleCharConfigKey, algorithm);
        ShardingSpherePreconditions.checkState(1 == props.getProperty(singleCharConfigKey).length(),
                () -> new AlgorithmInitializationException(algorithm, "%s's length must be one", singleCharConfigKey));
    }
    
    /**
     * Check at least one char config.
     * 
     * @param props props
     * @param atLeastOneCharConfigKey at least one char config key
     * @param algorithm mask algorithm
     */
    public static void checkAtLeastOneCharConfiguration(final Properties props, final String atLeastOneCharConfigKey, final MaskAlgorithm<?, ?> algorithm) {
        checkRequiredPropertyConfiguration(props, atLeastOneCharConfigKey, algorithm);
        ShardingSpherePreconditions.checkState(props.getProperty(atLeastOneCharConfigKey).length() > 0,
                () -> new AlgorithmInitializationException(algorithm, "%s's length must be at least one", atLeastOneCharConfigKey));
    }
    
    /**
     * Check required property configuration.
     * 
     * @param props props
     * @param positiveIntegerTypeConfigKey positive integer type config key
     * @param algorithm mask algorithm
     * @throws AlgorithmInitializationException algorithm initialization exception
     */
    public static void checkPositiveIntegerConfiguration(final Properties props, final String positiveIntegerTypeConfigKey, final MaskAlgorithm<?, ?> algorithm) {
        checkRequiredPropertyConfiguration(props, positiveIntegerTypeConfigKey, algorithm);
        try {
            int integerValue = Integer.parseInt(props.getProperty(positiveIntegerTypeConfigKey));
            ShardingSpherePreconditions.checkState(integerValue > 0, () -> new AlgorithmInitializationException(algorithm, "%s must be a positive integer.", positiveIntegerTypeConfigKey));
        } catch (final NumberFormatException ex) {
            throw new AlgorithmInitializationException(algorithm, "%s must be a valid integer number", positiveIntegerTypeConfigKey);
        }
    }
    
    private static void checkRequiredPropertyConfiguration(final Properties props, final String requiredPropertyConfigKey, final MaskAlgorithm<?, ?> algorithm) {
        ShardingSpherePreconditions.checkState(props.containsKey(requiredPropertyConfigKey), () -> new AlgorithmInitializationException(algorithm, "%s is required", requiredPropertyConfigKey));
    }
}
