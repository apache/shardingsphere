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

package org.apache.shardingsphere.readwritesplitting.algorithm;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Weight replica load-balance algorithm.
 */
@Getter
@Setter
public final class WeightReplicaLoadBalanceAlgorithm implements ReplicaLoadBalanceAlgorithm {
    
    private static final double ACCURACY_THRESHOLD = 0.0001;
    
    private static final ConcurrentHashMap<String, double[]> WEIGHT_MAP = new ConcurrentHashMap<>();
    
    private Properties props = new Properties();

    @Override
    public String getType() {
        return "WEIGHT";
    }

    @Override
    public String getDataSource(final String name, final String writeDataSourceName, final List<String> readDataSourceNames) {
        double[] weight = WEIGHT_MAP.containsKey(name) ? WEIGHT_MAP.get(name) : initWeight(readDataSourceNames);
        WEIGHT_MAP.putIfAbsent(name, weight);
        return getDataSourceName(readDataSourceNames, weight);
    }

    private String getDataSourceName(final List<String> readDataSourceNames, final double[] weight) {
        double randomWeight = ThreadLocalRandom.current().nextDouble(0, 1);
        int index = Arrays.binarySearch(weight, randomWeight);
        if (index < 0) {
            index = -index - 1;
            return index < weight.length && randomWeight < weight[index] ? readDataSourceNames.get(index) : readDataSourceNames.get(readDataSourceNames.size() - 1);
        } else {
            return readDataSourceNames.get(index);
        }
    }

    private double[] initWeight(final List<String> readDataSourceNames) {
        double[] weights = getWeights(readDataSourceNames);
        if (weights.length != 0 && Math.abs(weights[weights.length - 1] - 1.0D) >= ACCURACY_THRESHOLD) {
            throw new IllegalStateException("The cumulative weight is calculated incorrectly, and the sum of the probabilities is not equal to 1.");
        }
        return weights;
    }
    
    private double[] getWeights(final List<String> readDataSourceNames) {
        double[] exactWeights = new double[readDataSourceNames.size()];
        int index = 0;
        double sum = 0D;
        for (String readDataSourceName : readDataSourceNames) {
            double weight = getWeightValue(readDataSourceName);
            exactWeights[index++] = weight;
            sum += weight;
        }
        for (int i = 0; i < index; i++) {
            if (exactWeights[i] <= 0) {
                continue;
            }
            exactWeights[i] = exactWeights[i] / sum;
        }
        return calcWeight(exactWeights);
    }
    
    private double[] calcWeight(final double[] exactWeights) {
        double[] weights = new double[exactWeights.length];
        double randomRange = 0D;
        for (int i = 0; i < weights.length; i++) {
            weights[i] = randomRange + exactWeights[i];
            randomRange += exactWeights[i];
        }
        return weights;
    }
    
    private double getWeightValue(final String readDataSourceName) {
        Object weightObject = props.get(readDataSourceName);
        if (weightObject == null) {
            throw new IllegalStateException("Read database access weight is not configured：" + readDataSourceName);
        }
        double weight;
        try {
            weight = Double.parseDouble(weightObject.toString());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Read database weight configuration error, configuration parameters:" + weightObject.toString());
        }
        if (Double.isInfinite(weight)) {
            weight = 10000.0D;
        }
        if (Double.isNaN(weight)) {
            weight = 1.0D;
        }
        return weight;
    }
}
