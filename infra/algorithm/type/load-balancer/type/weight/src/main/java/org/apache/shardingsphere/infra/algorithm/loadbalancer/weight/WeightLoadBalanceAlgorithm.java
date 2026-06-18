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

package org.apache.shardingsphere.infra.algorithm.loadbalancer.weight;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Weight load balance algorithm.
 */
public final class WeightLoadBalanceAlgorithm implements LoadBalanceAlgorithm {
    
    private static final double ACCURACY_THRESHOLD = 0.0001;
    
    private final Map<String, double[]> weightMap = new ConcurrentHashMap<>();
    
    private final Map<String, Double> weightConfigMap = new HashMap<>();
    
    @Override
    public void init(final Properties props) {
        Collection<String> availableTargetNames = props.stringPropertyNames();
        ShardingSpherePreconditions.checkNotEmpty(availableTargetNames, () -> new AlgorithmInitializationException(this, "Available target is required."));
        for (String each : availableTargetNames) {
            String weight = props.getProperty(each);
            ShardingSpherePreconditions.checkNotNull(weight, () -> new AlgorithmInitializationException(this, "Weight of available target `%s` is required.", each));
            try {
                weightConfigMap.put(each, Double.parseDouble(weight));
            } catch (final NumberFormatException ex) {
                throw new AlgorithmInitializationException(this, "Weight `%s` of available target `%s` should be number.", weight, each);
            }
        }
    }
    
    @Override
    public void check(final String databaseName, final Collection<String> configuredTargetNames) {
        weightConfigMap.keySet().forEach(each -> ShardingSpherePreconditions.checkContains(configuredTargetNames, each,
                () -> new AlgorithmInitializationException(this, "Target `%s` is required in database `%s`.", each, databaseName)));
    }
    
    @HighFrequencyInvocation
    @Override
    public String getTargetName(final String groupName, final List<String> availableTargetNames) {
        double[] weight = weightMap.containsKey(groupName) && weightMap.get(groupName).length == availableTargetNames.size() ? weightMap.get(groupName) : initWeight(availableTargetNames);
        weightMap.put(groupName, weight);
        return getAvailableTargetName(availableTargetNames, weight);
    }
    
    @HighFrequencyInvocation
    private String getAvailableTargetName(final List<String> availableTargetNames, final double[] weight) {
        double randomWeight = ThreadLocalRandom.current().nextDouble(0D, 1D);
        int index = Arrays.binarySearch(weight, randomWeight);
        if (index < 0) {
            index = -index - 1;
            return index < weight.length && randomWeight < weight[index] ? availableTargetNames.get(index) : availableTargetNames.get(availableTargetNames.size() - 1);
        }
        return availableTargetNames.get(index);
    }
    
    @HighFrequencyInvocation
    private double[] initWeight(final List<String> availableTargetNames) {
        double[] result = getWeights(availableTargetNames);
        Preconditions.checkState(!(0 != result.length && Math.abs(result[result.length - 1] - 1.0D) >= ACCURACY_THRESHOLD),
                "The cumulative weight is calculated incorrectly, and the sum of the probabilities is not equal to 1");
        return result;
    }
    
    @HighFrequencyInvocation
    private double[] getWeights(final List<String> availableTargetNames) {
        double[] exactWeights = new double[availableTargetNames.size()];
        int index = 0;
        double sum = 0D;
        for (String each : availableTargetNames) {
            double weight = getWeightValue(each);
            exactWeights[index++] = weight;
            sum += weight;
        }
        for (int i = 0; i < index; i++) {
            if (exactWeights[i] <= 0D) {
                continue;
            }
            exactWeights[i] = exactWeights[i] / sum;
        }
        return calculateWeight(exactWeights);
    }
    
    @HighFrequencyInvocation
    private double[] calculateWeight(final double[] exactWeights) {
        double[] result = new double[exactWeights.length];
        double randomRange = 0D;
        for (int i = 0; i < result.length; i++) {
            result[i] = randomRange + exactWeights[i];
            randomRange += exactWeights[i];
        }
        return result;
    }
    
    @HighFrequencyInvocation
    private double getWeightValue(final String readDataSourceName) {
        double result = weightConfigMap.get(readDataSourceName);
        if (Double.isInfinite(result)) {
            result = 10000.0D;
        }
        if (Double.isNaN(result)) {
            result = 1.0D;
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "WEIGHT";
    }
}
