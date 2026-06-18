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

package org.apache.shardingsphere.infra.algorithm.loadbalancer.plugin;

import org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.plugin.PluginTypeAndClassMapper;

/**
 * Load balance algorithm type and class mapper.
 */
public final class LoadBalanceAlgorithmTypeAndClassMapper implements PluginTypeAndClassMapper {
    
    @Override
    public Class<LoadBalanceAlgorithm> getPluginClass() {
        return LoadBalanceAlgorithm.class;
    }
    
    @Override
    public String getType() {
        return "LOAD_BALANCE_ALGORITHM";
    }
}
