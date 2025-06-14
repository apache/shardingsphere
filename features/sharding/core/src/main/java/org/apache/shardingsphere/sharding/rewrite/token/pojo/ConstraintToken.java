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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Map;

/**
 * Constraint token.
 */
public final class ConstraintToken extends SQLToken implements Substitutable, RouteUnitAware {
    
    @Getter
    private final int stopIndex;
    
    private final IdentifierValue identifier;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final ShardingRule shardingRule;
    
    public ConstraintToken(final int startIndex, final int stopIndex, final IdentifierValue identifier, final SQLStatementContext sqlStatementContext, final ShardingRule shardingRule) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.identifier = identifier;
        this.sqlStatementContext = sqlStatementContext;
        this.shardingRule = shardingRule;
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        return identifier.getQuoteCharacter().wrap(getConstraintValue(routeUnit));
    }
    
    private String getConstraintValue(final RouteUnit routeUnit) {
        StringBuilder result = new StringBuilder(identifier.getValue());
        Map<String, String> logicAndActualTables = ShardingTokenUtils.getLogicAndActualTableMap(routeUnit, sqlStatementContext, shardingRule);
        sqlStatementContext.getTablesContext().getTableNames().stream().findFirst().map(logicAndActualTables::get).ifPresent(optional -> result.append('_').append(optional));
        return result.toString();
    }
}
