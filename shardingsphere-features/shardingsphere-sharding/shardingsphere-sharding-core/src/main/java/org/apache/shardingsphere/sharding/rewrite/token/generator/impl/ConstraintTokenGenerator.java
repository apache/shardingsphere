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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import lombok.Setter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.ConstraintAvailable;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ConstraintToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.aware.ShardingRuleAware;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Constraint token generator.
 */
@Setter
public final class ConstraintTokenGenerator implements CollectionSQLTokenGenerator, ShardingRuleAware {
    
    private ShardingRule shardingRule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof ConstraintAvailable && !((ConstraintAvailable) sqlStatementContext).getConstraints().isEmpty();
    }
    
    @Override
    public Collection<ConstraintToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<ConstraintToken> result = new LinkedList<>();
        if (sqlStatementContext instanceof ConstraintAvailable) {
            for (SQLSegment each : ((ConstraintAvailable) sqlStatementContext).getConstraints()) {
                IdentifierValue constraintIdentifier = ((ConstraintSegment) each).getIdentifier();
                if (null == constraintIdentifier) {
                    continue;
                }
                result.add(new ConstraintToken(each.getStartIndex(), each.getStopIndex(), constraintIdentifier, sqlStatementContext, shardingRule));
            }
        }
        return result;
    }
}
