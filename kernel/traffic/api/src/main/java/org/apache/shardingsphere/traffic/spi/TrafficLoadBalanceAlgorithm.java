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

package org.apache.shardingsphere.traffic.spi;

import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.algorithm.ShardingSphereAlgorithm;

import java.util.List;

/**
 * Traffic load balance algorithm.
 */
public interface TrafficLoadBalanceAlgorithm extends ShardingSphereAlgorithm {
    
    /**
     * Get instance definition.
     * 
     * @param name traffic strategy name
     * @param instances instance collection
     * @return instance definition
     */
    InstanceMetaData getInstanceId(String name, List<InstanceMetaData> instances);
}
