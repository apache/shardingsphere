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

package org.apache.shardingsphere.readwritesplitting.transaction;

import org.apache.shardingsphere.infra.context.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionReadQueryStrategyAware;

import java.util.List;

/**
 * Transaction read query strategy util.
 */
public class TransactionReadQueryStrategyUtil {
    
    /**
     * Get data source name in transaction.
     *
     * @param name read query logic data source name
     * @param writeDataSourceName name of write data source
     * @param readDataSourceNames names of read data sources
     * @param context transaction context
     * @param readQueryStrategy read query strategy in transaction
     * @param readQueryStrategyAware transaction read query strategy aware
     * @return name of selected data source
     */
    public static String routeInTransaction(final String name, final String writeDataSourceName, final List<String> readDataSourceNames, final TransactionConnectionContext context,
                                            final TransactionReadQueryStrategy readQueryStrategy, final TransactionReadQueryStrategyAware readQueryStrategyAware) {
        switch (readQueryStrategy) {
            case FIXED_REPLICA:
                if (null == context.getReadWriteSplitReplicaRoute()) {
                    context.setReadWriteSplitReplicaRoute(readQueryStrategyAware.getDataSourceName(name, readDataSourceNames));
                }
                return context.getReadWriteSplitReplicaRoute();
            case DYNAMIC_REPLICA:
                return readQueryStrategyAware.getDataSourceName(name, readDataSourceNames);
            case FIXED_PRIMARY:
            default:
                return writeDataSourceName;
        }
    }
}
