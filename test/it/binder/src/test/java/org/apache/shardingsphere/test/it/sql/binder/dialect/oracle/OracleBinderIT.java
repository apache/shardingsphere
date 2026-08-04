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

package org.apache.shardingsphere.test.it.sql.binder.dialect.oracle;

import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.test.it.sql.binder.SQLBinderIT;
import org.apache.shardingsphere.test.it.sql.binder.SQLBinderITSettings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

@SQLBinderITSettings("Oracle")
class OracleBinderIT extends SQLBinderIT {
    
    @Test
    void assertBindRemovedUnparenthesizedFunctionNamesAsDerivedColumns() {
        String sql = "SELECT derived.day, derived.rownum_, derived.row_number "
                + "FROM (SELECT order_id AS day, user_id AS rownum_, status AS row_number FROM t_order) derived";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql);
        List<ProjectionSegment> actualProjections = actual.getProjections().getProjections();
        assertColumnBound(actualProjections.get(0), "t_order", "order_id");
        assertColumnBound(actualProjections.get(1), "t_order", "user_id");
        assertColumnBound(actualProjections.get(2), "t_order", "status");
    }
    
    @Test
    void assertBindQuotedUnparenthesizedFunctionAsDerivedColumn() {
        String sql = "SELECT derived.\"SYSDATE\" FROM (SELECT creation_date AS \"SYSDATE\" FROM t_order) derived";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql);
        assertColumnBound(actual.getProjections().getProjections().get(0), "t_order", "creation_date");
    }
    
    @Test
    void assertBindOraclePaginationAlias() {
        String sql = "SELECT tt.rownum_ FROM (SELECT ROWNUM rownum_ FROM t_order) tt WHERE tt.rownum_ > 1";
        SelectStatement actual = (SelectStatement) bindSQLStatement("Oracle", sql);
        assertColumnBound(actual.getProjections().getProjections().get(0), "", "ROWNUM");
    }
    
    private void assertColumnBound(final ProjectionSegment actualProjection, final String expectedTable, final String expectedColumn) {
        assertThat(actualProjection, isA(ColumnProjectionSegment.class));
        ColumnSegmentBoundInfo actual = ((ColumnProjectionSegment) actualProjection).getColumn().getColumnBoundInfo();
        assertThat(actual.getOriginalTable().getValue(), is(expectedTable));
        assertThat(actual.getOriginalColumn().getValue(), is(expectedColumn));
    }
}
