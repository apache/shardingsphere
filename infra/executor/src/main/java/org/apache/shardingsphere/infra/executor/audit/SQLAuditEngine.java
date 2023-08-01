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
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPILoader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * SQL audit engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLAuditEngine {
    
    /**
     * Audit SQL.
     *
     * @param sqlStatementContext SQL statement context
     * @param params SQL parameters
     * @param globalRuleMetaData global rule meta data
     * @param database database
     * @param grantee grantee
     * @param hintValueContext hint value context
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void audit(final SQLStatementContext sqlStatementContext, final List<Object> params,
                             final ShardingSphereRuleMetaData globalRuleMetaData, final ShardingSphereDatabase database, final Grantee grantee, final HintValueContext hintValueContext) {
        Collection<ShardingSphereRule> rules = new LinkedList<>(globalRuleMetaData.getRules());
        if (null != database) {
            rules.addAll(database.getRuleMetaData().getRules());
        }
        for (Entry<ShardingSphereRule, SQLAuditor> entry : OrderedSPILoader.getServices(SQLAuditor.class, rules).entrySet()) {
            entry.getValue().audit(sqlStatementContext, params, grantee, globalRuleMetaData, database, entry.getKey(), hintValueContext);
        }
    }
}
