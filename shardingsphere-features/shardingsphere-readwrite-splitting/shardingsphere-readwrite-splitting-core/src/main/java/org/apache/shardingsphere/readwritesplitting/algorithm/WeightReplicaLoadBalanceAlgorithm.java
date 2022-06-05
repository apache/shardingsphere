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
public class WeightReplicaLoadBalanceAlgorithm implements ReplicaLoadBalanceAlgorithm {
    private final ConcurrentHashMap<String, Double[]> weightMap = new ConcurrentHashMap<>();

    private Properties props = new Properties();

    @Override
    public Properties getProps() {
        return this.props;
    }

    @Override
    public void setProps(final Properties props) {
        this.props = props;
    }

    @Override
    public String getType() {
        return "WEIGHT";
    }

    @Override
    public String getDataSource(final String name, final String writeDataSourceName, final List<String> readDataSourceNames) {
        Double[] weight = weightMap.containsKey(name) ? weightMap.get(name) : initWeight(readDataSourceNames);
        weightMap.putIfAbsent(name, weight);
        String dataSourceName = getDataSourceName(readDataSourceNames, weight);
        return dataSourceName;
    }

    private String getDataSourceName(final List<String> readDataSourceNames, final Double[] weight) {
        double randomWeight = ThreadLocalRandom.current().nextDouble(0, 1);
        int index = Arrays.binarySearch(weight, randomWeight);
        if (index < 0) {
            index = -index - 1;
            return index >= 0 && index < weight.length && randomWeight < weight[index] ? readDataSourceNames.get(index) : readDataSourceNames.get(readDataSourceNames.size() - 1);
        } else {
            return readDataSourceNames.get(index);
        }
    }

    private Double[] initWeight(final List<String> readDataSourceNames) {
        Double[] weights;
        Double[] exactWeights = new Double[readDataSourceNames.size()];
        Integer index = 0;
        Double sum = 0D;
        for (String readDataSourceName : readDataSourceNames) {
            Object weightObject = props.get(readDataSourceName);
            if (weightObject == null) {
                throw new IllegalStateException("Read database access weight is not configured：" + readDataSourceName);
            }
            Double weight;
            try {
                weight = Double.valueOf(weightObject.toString());
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Read database weight configuration error, configuration parameters:" + weightObject.toString());
            }
            if (Double.isInfinite(weight)) {
                weight = 10000.0D;
            }
            if (Double.isNaN(weight)) {
                weight = 1.0D;
            }
            exactWeights[index++] = weight;
            sum += weight;
        }
        for (int i = 0; i < index; i++) {
            if (exactWeights[i] <= 0) {
                continue;
            }
            exactWeights[i] = exactWeights[i] / sum;
        }
        weights = new Double[readDataSourceNames.size()];
        double randomRange = 0D;
        for (int i = 0; i < index; i++) {
            weights[i] = randomRange + exactWeights[i];
            randomRange += exactWeights[i];
        }
        if (index != 0 && Math.abs(weights[index - 1] - 1.0D) >= 0.0001) {
            throw new IllegalStateException("The cumulative weight is calculated incorrectly, and the sum of the probabilities is not equal to 1.");
        }
        return weights;
    }
}
