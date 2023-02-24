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

package org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.context.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionReadQueryStrategyAware;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.exception.algorithm.InvalidReadDatabaseWeightException;
import org.apache.shardingsphere.readwritesplitting.exception.algorithm.MissingRequiredReadDatabaseWeightException;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionReadQueryStrategyUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Weight read query load-balance algorithm.
 */
public final class WeightReadQueryLoadBalanceAlgorithm implements ReadQueryLoadBalanceAlgorithm, TransactionReadQueryStrategyAware {
    
    private static final double ACCURACY_THRESHOLD = 0.0001;
    
    private final Map<String, double[]> weightMap = new ConcurrentHashMap<>();
    
    private Properties props;
    
    private TransactionReadQueryStrategy transactionReadQueryStrategy;
    
    @Getter
    private Collection<String> dataSourceNames;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        if (props.containsKey(TRANSACTION_READ_QUERY_STRATEGY)) {
            transactionReadQueryStrategy = TransactionReadQueryStrategy.valueOf(props.getProperty(TRANSACTION_READ_QUERY_STRATEGY));
            dataSourceNames = props.stringPropertyNames().stream().filter(each -> !each.equals(TRANSACTION_READ_QUERY_STRATEGY)).collect(Collectors.toList());
        } else {
            transactionReadQueryStrategy = TransactionReadQueryStrategy.FIXED_PRIMARY;
            dataSourceNames = props.stringPropertyNames();
        }
    }
    
    @Override
    public String getDataSource(final String name, final String writeDataSourceName, final List<String> readDataSourceNames, final TransactionConnectionContext context) {
        if (context.isInTransaction()) {
            return TransactionReadQueryStrategyUtil.routeInTransaction(name, writeDataSourceName, readDataSourceNames, context, transactionReadQueryStrategy, this);
        }
        return getDataSourceName(name, readDataSourceNames);
    }
    
    @Override
    public String getDataSourceName(final String name, final List<String> readDataSourceNames) {
        double[] weight = weightMap.containsKey(name) && weightMap.get(name).length == readDataSourceNames.size() ? weightMap.get(name) : initWeight(readDataSourceNames);
        weightMap.put(name, weight);
        return getDataSourceName(readDataSourceNames, weight);
    }
    
    private String getDataSourceName(final List<String> readDataSourceNames, final double[] weight) {
        double randomWeight = ThreadLocalRandom.current().nextDouble(0, 1);
        int index = Arrays.binarySearch(weight, randomWeight);
        if (index < 0) {
            index = -index - 1;
            return index < weight.length && randomWeight < weight[index] ? readDataSourceNames.get(index) : readDataSourceNames.get(readDataSourceNames.size() - 1);
        }
        return readDataSourceNames.get(index);
    }
    
    private double[] initWeight(final List<String> readDataSourceNames) {
        double[] result = getWeights(readDataSourceNames);
        Preconditions.checkState(0 == result.length || !(Math.abs(result[result.length - 1] - 1.0D) >= ACCURACY_THRESHOLD),
                "The cumulative weight is calculated incorrectly, and the sum of the probabilities is not equal to 1");
        return result;
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
        ShardingSpherePreconditions.checkNotNull(weightObject, () -> new MissingRequiredReadDatabaseWeightException(getType(),
                String.format("Read database `%s` access weight is not configured", readDataSourceName)));
        double result;
        try {
            result = Double.parseDouble(weightObject.toString());
        } catch (final NumberFormatException ex) {
            throw new InvalidReadDatabaseWeightException(weightObject);
        }
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
