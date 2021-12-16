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

package org.apache.shardingsphere.transaction.context;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.rule.builder.DefaultTransactionRuleConfigurationBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Transaction contexts builder.
 */
@RequiredArgsConstructor
public final class TransactionContextsBuilder {
    
    private final Map<String, ShardingSphereMetaData> metaDataMap;
    
    private final Collection<ShardingSphereRule> globalRules;
    
    /**
     * Build transaction contexts.
     * 
     * @return transaction contexts
     */
    public TransactionContexts build() {
        Map<String, ShardingSphereTransactionManagerEngine> engines = new HashMap<>(metaDataMap.keySet().size(), 1);
        TransactionRule transactionRule = getTransactionRule();
        for (String each : metaDataMap.keySet()) {
            ShardingSphereTransactionManagerEngine engine = new ShardingSphereTransactionManagerEngine();
            ShardingSphereResource resource = metaDataMap.get(each).getResource();
            engine.init(resource.getDatabaseType(), resource.getDataSources(), transactionRule);
            engines.put(each, engine);
        }
        return new TransactionContexts(engines);
    }
    
    private TransactionRule getTransactionRule() {
        Optional<TransactionRule> transactionRule = globalRules.stream().filter(each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        return transactionRule.orElseGet(() -> new TransactionRule(new DefaultTransactionRuleConfigurationBuilder().build()));
    }
}
