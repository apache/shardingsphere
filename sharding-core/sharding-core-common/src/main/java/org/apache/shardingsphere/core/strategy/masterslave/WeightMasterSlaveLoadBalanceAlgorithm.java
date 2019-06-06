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

package org.apache.shardingsphere.core.strategy.masterslave;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.spi.masterslave.MasterSlaveLoadBalanceAlgorithm;

import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Weight slave database load-balance algorithm.
 *
 * @author zhangyonglun
 */
@Getter
@Setter
public final class WeightMasterSlaveLoadBalanceAlgorithm implements MasterSlaveLoadBalanceAlgorithm {
    
    private static final String WEIGHT_KEY = "weight.values";
    
    private TreeMap<Integer, String> weightMap = new TreeMap<>();
    
    private Properties properties = new Properties();
    
    @Override
    public String getType() {
        return "WEIGHT";
    }
    
    @Override
    public String getDataSource(final String name, final String masterDataSourceName, final List<String> slaveDataSourceNames) {
        if (weightMap.isEmpty()) {
            String[] weightsString = properties.get(WEIGHT_KEY).toString().split(",");
            int total = 0;
            for (int i = 0; i < slaveDataSourceNames.size(); i++) {
                total += Integer.parseInt(weightsString[i]);
                weightMap.put(total, slaveDataSourceNames.get(i));
            }
        }
        SortedMap<Integer, String> tailMap = weightMap.tailMap(new Random().nextInt(weightMap.lastKey()), false);
        return weightMap.get(tailMap.firstKey());
    }
}
