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

package org.apache.shardingsphere.infra.algorithm.load.balancer.weight;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.algorithm.load.balancer.core.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.algorithm.load.balancer.core.exception.InvalidAvailableTargetWeightException;
import org.apache.shardingsphere.infra.algorithm.load.balancer.core.exception.LoadBalanceAlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.util.Arrays;
import java.util.Collection;
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
    
    private Properties props;
    
    @Getter
    private Collection<String> availableTargetNames;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        availableTargetNames = props.stringPropertyNames();
        ShardingSpherePreconditions.checkState(!availableTargetNames.isEmpty(), () -> new LoadBalanceAlgorithmInitializationException(getType(), "Available target is required"));
        for (String each : availableTargetNames) {
            String weight = props.getProperty(each);
            ShardingSpherePreconditions.checkNotNull(weight,
                    () -> new LoadBalanceAlgorithmInitializationException(getType(), String.format("Available target `%s` access weight is not configured.", each)));
            try {
                Double.parseDouble(weight);
            } catch (final NumberFormatException ex) {
                throw new InvalidAvailableTargetWeightException(weight);
            }
        }
    }
    
    @Override
    public String getAvailableTargetName(final String groupName, final List<String> availableTargetNames) {
        double[] weight = weightMap.containsKey(groupName) && weightMap.get(groupName).length == availableTargetNames.size() ? weightMap.get(groupName) : initWeight(availableTargetNames);
        weightMap.put(groupName, weight);
        return getAvailableTargetName(availableTargetNames, weight);
    }
    
    private String getAvailableTargetName(final List<String> availableTargetNames, final double[] weight) {
        double randomWeight = ThreadLocalRandom.current().nextDouble(0, 1);
        int index = Arrays.binarySearch(weight, randomWeight);
        if (index < 0) {
            index = -index - 1;
            return index < weight.length && randomWeight < weight[index] ? availableTargetNames.get(index) : availableTargetNames.get(availableTargetNames.size() - 1);
        }
        return availableTargetNames.get(index);
    }
    
    private double[] initWeight(final List<String> availableTargetNames) {
        double[] result = getWeights(availableTargetNames);
        Preconditions.checkState(!(0 != result.length && Math.abs(result[result.length - 1] - 1.0D) >= ACCURACY_THRESHOLD),
                "The cumulative weight is calculated incorrectly, and the sum of the probabilities is not equal to 1");
        return result;
    }
    
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
            if (exactWeights[i] <= 0) {
                continue;
            }
            exactWeights[i] = exactWeights[i] / sum;
        }
        return calculateWeight(exactWeights);
    }
    
    private double[] calculateWeight(final double[] exactWeights) {
        double[] result = new double[exactWeights.length];
        double randomRange = 0D;
        for (int i = 0; i < result.length; i++) {
            result[i] = randomRange + exactWeights[i];
            randomRange += exactWeights[i];
        }
        return result;
    }
    
    private double getWeightValue(final String readDataSourceName) {
        Object weightObject = props.get(readDataSourceName);
        double result = Double.parseDouble(weightObject.toString());
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
