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

package org.apache.shardingsphere.readwritesplitting.api.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionalReadQueryStrategy;

/**
 * Readwrite-splitting data source rule configuration.
 */
@RequiredArgsConstructor
@Getter
public final class ReadwriteSplittingDataSourceRuleConfiguration {
    
    private final String name;
    
    private final StaticReadwriteSplittingStrategyConfiguration staticStrategy;
    
    private final TransactionalReadQueryStrategy transactionalReadQueryStrategy;
    
    private final String loadBalancerName;
    
    /**
     * Will remove soon.
     * 
     * @param name name
     * @param staticStrategy static strategy
     * @param loadBalancerName load balancer name
     * @deprecated will remove soon
     */
    @Deprecated
    public ReadwriteSplittingDataSourceRuleConfiguration(final String name, final StaticReadwriteSplittingStrategyConfiguration staticStrategy, final String loadBalancerName) {
        this(name, staticStrategy, TransactionalReadQueryStrategy.DYNAMIC, loadBalancerName);
    }
}
