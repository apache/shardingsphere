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

package org.apache.shardingsphere.core.rewrite.statement;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptConditions;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptConditionOptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingConditionOptimizedStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collections;

/**
 * Rewrite statement factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RewriteStatementFactory {
    
    /**
     * Create new instance of rewrite statement.
     * 
     * @param shardingRule sharding rule
     * @param sqlRouteResult sql route result
     * @return rewrite statement
     */
    public static RewriteStatement newInstance(final ShardingRule shardingRule, final SQLRouteResult sqlRouteResult) {
        ShardingConditions shardingConditions = sqlRouteResult.getShardingStatement() instanceof ShardingConditionOptimizedStatement
                ? ((ShardingConditionOptimizedStatement) sqlRouteResult.getShardingStatement()).getShardingConditions() : new ShardingConditions(Collections.<ShardingCondition>emptyList());
        EncryptConditions encryptConditions = sqlRouteResult.getEncryptStatement() instanceof EncryptConditionOptimizedStatement
                ? ((EncryptConditionOptimizedStatement) sqlRouteResult.getEncryptStatement()).getEncryptConditions() : new EncryptConditions(Collections.<EncryptCondition>emptyList());
        return sqlRouteResult.getShardingStatement() instanceof InsertOptimizedStatement
                ? new InsertRewriteStatement((InsertOptimizedStatement) sqlRouteResult.getShardingStatement(), shardingConditions, encryptConditions, shardingRule.getEncryptRule())
                : new RewriteStatement(sqlRouteResult.getShardingStatement(), shardingConditions, encryptConditions);
    }
    
    /**
     * Create new instance of rewrite statement.
     * 
     * @param encryptRule encrypt rule
     * @param encryptStatement encrypt statement
     * @return rewrite statement
     */
    public static RewriteStatement newInstance(final EncryptRule encryptRule, final EncryptOptimizedStatement encryptStatement) {
        ShardingConditions shardingConditions = new ShardingConditions(Collections.<ShardingCondition>emptyList());
        EncryptConditions encryptConditions = encryptStatement instanceof EncryptConditionOptimizedStatement
                ? ((EncryptConditionOptimizedStatement) encryptStatement).getEncryptConditions() : new EncryptConditions(Collections.<EncryptCondition>emptyList());
        return encryptStatement instanceof InsertOptimizedStatement
                ? new InsertRewriteStatement((InsertOptimizedStatement) encryptStatement, shardingConditions, encryptConditions, encryptRule)
                : new RewriteStatement(encryptStatement, shardingConditions, encryptConditions);
    }
}
