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

package org.apache.shardingsphere.sql.parser.engine.opengauss.visitor.statement;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenGaussStatementVisitorTest {
    
    @Test
    void assertVisitWindowAggregationProjection() {
        String sql = "select pg_catalog.max(ref_0.c36) over (partition by ref_0.c39 order by ref_0.vkey desc) as c_5 from t24 as ref_0";
        SelectStatement statement = parseSelectStatement(sql);
        ProjectionSegment projection = statement.getProjections().getProjections().iterator().next();
        assertThat(projection, isA(AggregationProjectionSegment.class));
        AggregationProjectionSegment aggregationProjection = (AggregationProjectionSegment) projection;
        assertThat(aggregationProjection.getExpression(), is("pg_catalog.max(ref_0.c36) over (partition by ref_0.c39 order by ref_0.vkey desc)"));
        assertThat(aggregationProjection.getAliasName().orElse(null), is("c_5"));
        assertTrue(aggregationProjection.getWindow().isPresent());
        WindowItemSegment windowItem = aggregationProjection.getWindow().get();
        assertThat(windowItem.getOrderBySegment().getOrderByItems().size(), is(1));
    }
    
    private SelectStatement parseSelectStatement(final String sql) {
        ParseASTNode parseASTNode = new SQLParserEngine("openGauss", new CacheOption(128, 1024L)).parse(sql, false);
        return (SelectStatement) new SQLStatementVisitorEngine("openGauss").visit(parseASTNode);
    }
}
