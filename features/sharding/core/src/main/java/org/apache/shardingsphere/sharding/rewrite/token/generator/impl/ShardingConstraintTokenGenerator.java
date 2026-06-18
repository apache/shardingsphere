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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ConstraintToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ConstraintSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Sharding constraint token generator.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class ShardingConstraintTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext> {
    
    private final ShardingRule rule;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement().getAttributes().findAttribute(ConstraintSQLStatementAttribute.class).map(optional -> !optional.getConstraints().isEmpty()).orElse(false);
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        for (ConstraintSegment each : sqlStatementContext.getSqlStatement().getAttributes().getAttribute(ConstraintSQLStatementAttribute.class).getConstraints()) {
            IdentifierValue constraintIdentifier = each.getIdentifier();
            // TODO make sure can remove null check here? @duanzhengqiang
            if (null != constraintIdentifier) {
                result.add(new ConstraintToken(each.getStartIndex(), each.getStopIndex(), constraintIdentifier, sqlStatementContext, rule));
            }
        }
        return result;
    }
}
