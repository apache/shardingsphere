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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.binder.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.AggregationDistinctToken;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Aggregation distinct token generator.
 */
public final class AggregationDistinctTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, IgnoreForSingleRoute {
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext;
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        for (AggregationDistinctProjection each : ((SelectStatementContext) sqlStatementContext).getProjectionsContext().getAggregationDistinctProjections()) {
            result.add(generateSQLToken(each));
        }
        return result;
    }
    
    private AggregationDistinctToken generateSQLToken(final AggregationDistinctProjection projection) {
        Preconditions.checkArgument(projection.getAlias().isPresent());
        String derivedAlias = DerivedColumn.isDerivedColumnName(projection.getAlias().get().getValue()) ? projection.getAlias().get().getValue() : null;
        return new AggregationDistinctToken(projection.getStartIndex(), projection.getStopIndex(), projection.getDistinctInnerExpression(), derivedAlias);
    }
}
