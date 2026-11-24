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

package org.apache.shardingsphere.shadow.rewrite.token.generator;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertValue;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.InsertValuesToken;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.rewrite.token.pojo.ShadowInsertValuesToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Insert values token generator for shadow.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class ShadowInsertValuesTokenGenerator implements OptionalSQLTokenGenerator<InsertStatementContext> {
    
    private final RouteContext routeContext;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && !((InsertStatementContext) sqlStatementContext).getInsertValueContexts().isEmpty();
    }
    
    @Override
    public InsertValuesToken generateSQLToken(final InsertStatementContext sqlStatementContext) {
        Collection<InsertValuesSegment> insertValuesSegments = sqlStatementContext.getSqlStatement().getValues();
        ShadowInsertValuesToken result = new ShadowInsertValuesToken(getStartIndex(insertValuesSegments), getStopIndex(insertValuesSegments), getDataSourceInsertValueIndexes());
        for (InsertValueContext each : sqlStatementContext.getInsertValueContexts()) {
            result.getInsertValues().add(new InsertValue(new LinkedList<>(each.getValueExpressions())));
        }
        return result;
    }
    
    private int getStartIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStartIndex();
        for (InsertValuesSegment each : segments) {
            result = Math.min(result, each.getStartIndex());
        }
        return result;
    }
    
    private int getStopIndex(final Collection<InsertValuesSegment> segments) {
        int result = segments.iterator().next().getStopIndex();
        for (InsertValuesSegment each : segments) {
            result = Math.max(result, each.getStopIndex());
        }
        return result;
    }
    
    private Map<String, Collection<Integer>> getDataSourceInsertValueIndexes() {
        Map<String, Collection<Integer>> result = new LinkedHashMap<>(routeContext.getRouteUnitInsertValueIndexes().size(), 1F);
        for (Entry<RouteUnit, Collection<Integer>> entry : routeContext.getRouteUnitInsertValueIndexes().entrySet()) {
            result.put(entry.getKey().getDataSourceMapper().getActualName(), entry.getValue());
        }
        return result;
    }
}
