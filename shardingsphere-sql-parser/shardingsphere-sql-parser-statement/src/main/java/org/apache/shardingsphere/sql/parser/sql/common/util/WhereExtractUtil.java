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

package org.apache.shardingsphere.sql.parser.sql.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Where extract utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WhereExtractUtil {

    /**
     * Get join where segment from SelectStatement.
     *
     * @param selectStatement SelectStatement
     * @return join where segment collection
     */
    public static Collection<WhereSegment> getJoinWhereSegments(final SelectStatement selectStatement) {
        if (null == selectStatement.getFrom()) {
            return Collections.emptyList();
        }
        TableSegment tableSegment = selectStatement.getFrom();
        Collection<WhereSegment> result = new LinkedList<>();
        if (tableSegment instanceof JoinTableSegment && null != ((JoinTableSegment) tableSegment).getCondition()) {
            ExpressionSegment expressionSegment = ((JoinTableSegment) tableSegment).getCondition();
            WhereSegment whereSegment = new WhereSegment(expressionSegment.getStartIndex(), expressionSegment.getStopIndex(), expressionSegment);
            result.add(whereSegment);
        }
        return result;
    }

    /**
     * Get subquery where segment from SelectStatement.
     *
     * @param selectStatement SelectStatement
     * @return subquery where segment collection
     */
    public static Collection<WhereSegment> getSubqueryWhereSegments(final SelectStatement selectStatement) {
        Collection<WhereSegment> result = new LinkedList<>();
        for (SubquerySegment each : SubqueryExtractUtil.getSubquerySegments(selectStatement)) {
            each.getSelect().getWhere().ifPresent(result::add);
        }
        return result;
    }
}
