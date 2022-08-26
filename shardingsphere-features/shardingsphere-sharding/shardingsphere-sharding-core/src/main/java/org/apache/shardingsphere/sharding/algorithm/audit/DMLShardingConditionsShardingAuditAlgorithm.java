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

package org.apache.shardingsphere.sharding.algorithm.audit;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.check.SQLCheckResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.ShardingConditionEngineFactory;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;

import java.util.List;
import java.util.Properties;

/**
 * DML sharding conditions sharding audit algorithm.
 */
public final class DMLShardingConditionsShardingAuditAlgorithm implements ShardingAuditAlgorithm {
    
    @Getter
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public SQLCheckResult check(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters, final Grantee grantee, final ShardingSphereDatabase database) {
        if (sqlStatementContext.getSqlStatement() instanceof DMLStatement) {
            ShardingRule rule = ((List<ShardingRule>) database.getRuleMetaData().findRules(ShardingRule.class)).get(0);
            if (rule.isAllBroadcastTables(sqlStatementContext.getTablesContext().getTableNames())
                    || sqlStatementContext.getTablesContext().getTableNames().stream().noneMatch(rule::isShardingTable)) {
                return new SQLCheckResult(true, "");
            }
            ShardingConditionEngine shardingConditionEngine = ShardingConditionEngineFactory.createShardingConditionEngine(sqlStatementContext, database, rule);
            if (shardingConditionEngine.createShardingConditions(sqlStatementContext, parameters).isEmpty()) {
                return new SQLCheckResult(false, "Not allow DML operation without sharding conditions");
            }
        }
        return new SQLCheckResult(true, "");
    }
    
    @Override
    public String getType() {
        return "DML_SHARDING_CONDITIONS";
    }
}
