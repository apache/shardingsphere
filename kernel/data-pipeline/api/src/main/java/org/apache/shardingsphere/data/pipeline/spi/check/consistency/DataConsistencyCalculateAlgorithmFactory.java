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

package org.apache.shardingsphere.data.pipeline.spi.check.consistency;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.Properties;

/**
 * Data consistency calculate algorithm factory.
 */
public final class DataConsistencyCalculateAlgorithmFactory {
    
    static {
        ShardingSphereServiceLoader.register(DataConsistencyCalculateAlgorithm.class);
    }
    
    /**
     * Create new instance of data consistency calculate algorithm.
     *
     * @param type algorithm type
     * @param props properties
     * @return created instance
     */
    public static DataConsistencyCalculateAlgorithm newInstance(final String type, final Properties props) {
        return ShardingSphereAlgorithmFactory.createAlgorithm(new AlgorithmConfiguration(type, props), DataConsistencyCalculateAlgorithm.class);
    }
    
    /**
     * Get all data consistency calculate algorithm instances.
     *
     * @return all data consistency calculate algorithm instances
     */
    public static Collection<DataConsistencyCalculateAlgorithm> getAllInstances() {
        return ShardingSphereServiceLoader.getServiceInstances(DataConsistencyCalculateAlgorithm.class);
    }
}
