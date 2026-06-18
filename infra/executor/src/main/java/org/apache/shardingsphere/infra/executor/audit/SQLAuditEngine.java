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

package org.apache.shardingsphere.infra.executor.audit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * SQL audit engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLAuditEngine {
    
    /**
     * Audit SQL.
     *
     * @param queryContext query context
     * @param database database
     */
    @HighFrequencyInvocation
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void audit(final QueryContext queryContext, final ShardingSphereDatabase database) {
        RuleMetaData globalRuleMetaData = queryContext.getMetaData().getGlobalRuleMetaData();
        Collection<ShardingSphereRule> rules = new LinkedList<>(globalRuleMetaData.getRules());
        if (null != database) {
            rules.addAll(database.getRuleMetaData().getRules());
        }
        for (Entry<ShardingSphereRule, SQLAuditor> entry : OrderedSPILoader.getServices(SQLAuditor.class, rules).entrySet()) {
            entry.getValue().audit(queryContext, database, entry.getKey());
        }
    }
}
