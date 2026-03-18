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
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Properties;

/**
 * Mask algorithm properties checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaskAlgorithmPropertiesChecker {
    
    /**
     * Check single char.
     *
     * @param props properties to be checked
     * @param propKey properties key to be checked
     * @param algorithm mask algorithm
     */
    public static void checkSingleChar(final Properties props, final String propKey, final MaskAlgorithm<?, ?> algorithm) {
        checkRequired(props, propKey, algorithm);
        ShardingSpherePreconditions.checkState(1 == props.getProperty(propKey).length(), () -> new AlgorithmInitializationException(algorithm, "%s's length must be one", propKey));
    }
    
    /**
     * Check at least one char.
     *
     * @param props properties to be checked
     * @param propKey properties key to be checked
     * @param algorithm mask algorithm
     */
    public static void checkAtLeastOneChar(final Properties props, final String propKey, final MaskAlgorithm<?, ?> algorithm) {
        checkRequired(props, propKey, algorithm);
        ShardingSpherePreconditions.checkNotEmpty(props.getProperty(propKey), () -> new AlgorithmInitializationException(algorithm, "%s's length must be at least one", propKey));
    }
    
    /**
     * check positive integer.
     *
     * @param props properties to be checked
     * @param propKey properties key to be checked
     * @param algorithm mask algorithm
     * @throws AlgorithmInitializationException algorithm initialization exception
     */
    public static void checkPositiveInteger(final Properties props, final String propKey, final MaskAlgorithm<?, ?> algorithm) {
        checkRequired(props, propKey, algorithm);
        try {
            int integerValue = Integer.parseInt(props.getProperty(propKey));
            ShardingSpherePreconditions.checkState(integerValue > 0, () -> new AlgorithmInitializationException(algorithm, "%s must be a positive integer.", propKey));
        } catch (final NumberFormatException ex) {
            throw new AlgorithmInitializationException(algorithm, "%s must be a valid integer number", propKey);
        }
    }
    
    private static void checkRequired(final Properties props, final String requiredPropKey, final MaskAlgorithm<?, ?> algorithm) {
        ShardingSpherePreconditions.checkContainsKey(props, requiredPropKey, () -> new AlgorithmInitializationException(algorithm, "%s is required", requiredPropKey));
    }
}
