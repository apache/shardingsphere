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

import org.apache.shardingsphere.infra.context.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionReadQueryStrategyAware;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionReadQueryStrategyUtil;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round-robin read query load-balance algorithm.
 */
public final class RoundRobinReadQueryLoadBalanceAlgorithm implements ReadQueryLoadBalanceAlgorithm, TransactionReadQueryStrategyAware {
    
    private final AtomicInteger count = new AtomicInteger(0);
    
    private TransactionReadQueryStrategy transactionReadQueryStrategy;
    
    @Override
    public void init(final Properties props) {
        transactionReadQueryStrategy = props.containsKey(TRANSACTION_READ_QUERY_STRATEGY)
                ? TransactionReadQueryStrategy.valueOf(props.getProperty(TRANSACTION_READ_QUERY_STRATEGY))
                : TransactionReadQueryStrategy.FIXED_PRIMARY;
    }
    
    @Override
    public String getDataSource(final String name, final String writeDataSourceName, final List<String> readDataSourceNames, final TransactionConnectionContext context) {
        if (context.isInTransaction()) {
            return TransactionReadQueryStrategyUtil.routeInTransaction(name, writeDataSourceName, readDataSourceNames, context, transactionReadQueryStrategy, this);
        }
        return getDataSourceName(name, readDataSourceNames);
    }
    
    @Override
    public String getType() {
        return "ROUND_ROBIN";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
    
    @Override
    public String getDataSourceName(final String name, final List<String> readDataSourceNames) {
        return readDataSourceNames.get(Math.abs(count.getAndIncrement()) % readDataSourceNames.size());
    }
}
