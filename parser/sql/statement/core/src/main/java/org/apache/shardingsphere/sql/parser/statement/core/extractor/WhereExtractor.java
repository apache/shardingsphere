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

package org.apache.shardingsphere.sql.parser.statement.core.extractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Where extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WhereExtractor {
    
    /**
     * Extract join where segment from select statement.
     *
     * @param selectStatement to be extracted select statement
     * @return extracted join where segments
     */
    public static Collection<WhereSegment> extractJoinWhereSegments(final SelectStatement selectStatement) {
        return selectStatement.getFrom().map(WhereExtractor::extractJoinWhereSegments).orElseGet(Collections::emptyList);
    }
    
    private static Collection<WhereSegment> extractJoinWhereSegments(final TableSegment tableSegment) {
        if (!(tableSegment instanceof JoinTableSegment) || null == ((JoinTableSegment) tableSegment).getCondition()) {
            return Collections.emptyList();
        }
        JoinTableSegment joinTableSegment = (JoinTableSegment) tableSegment;
        Collection<WhereSegment> result = new LinkedList<>();
        result.add(generateWhereSegment(joinTableSegment));
        result.addAll(extractJoinWhereSegments(joinTableSegment.getLeft()));
        result.addAll(extractJoinWhereSegments(joinTableSegment.getRight()));
        return result;
    }
    
    private static WhereSegment generateWhereSegment(final JoinTableSegment joinTableSegment) {
        ExpressionSegment expressionSegment = joinTableSegment.getCondition();
        return new WhereSegment(expressionSegment.getStartIndex(), expressionSegment.getStopIndex(), expressionSegment);
    }
    
    /**
     * Extract subquery where segment from SelectStatement.
     *
     * @param selectStatement to be extracted select statement
     * @return extracted subquery where segments
     */
    public static Collection<WhereSegment> extractSubqueryWhereSegments(final SelectStatement selectStatement) {
        Collection<WhereSegment> result = new LinkedList<>();
        for (SubquerySegment each : SubqueryExtractor.extractSubquerySegments(selectStatement, false)) {
            each.getSelect().getWhere().ifPresent(result::add);
            result.addAll(extractJoinWhereSegments(each.getSelect()));
        }
        return result;
    }
}
