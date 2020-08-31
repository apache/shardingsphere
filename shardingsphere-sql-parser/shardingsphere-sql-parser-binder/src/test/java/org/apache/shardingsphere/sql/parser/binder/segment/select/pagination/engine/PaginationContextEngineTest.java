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

package org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.engine;

import org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.TableFactorSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.TableReferenceSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.top.TopProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PaginationContextEngineTest {
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentIsPresent() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 10, new NumberLiteralLimitValueSegment(0, 10, 100L),
                new NumberLiteralLimitValueSegment(0, 10, 100L)));
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, null, Collections.emptyList());
        assertTrue(paginationContext.getOffsetSegment().isPresent());
        assertTrue(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentAbsentAndTopSegmentPresent() {
        SelectStatement subquerySelectStatement = new SelectStatement();
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        projections.getProjections().add(new TopProjectionSegment(0, 10, null, "rowNumberAlias"));
        subquerySelectStatement.setProjections(projections);
        SubquerySegment subquerySegment = new SubquerySegment(0, 0, subquerySelectStatement);
        TableFactorSegment tableFactorSegment = new TableFactorSegment();
        tableFactorSegment.setTable(new SubqueryTableSegment(subquerySegment));
        TableReferenceSegment tableReferenceSegment = new TableReferenceSegment();
        tableReferenceSegment.setTableFactor(tableFactorSegment);
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getTableReferences().add(tableReferenceSegment);
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, null, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenLimitSegmentTopSegmentAbsentAndWhereSegmentPresent() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        selectStatement.setWhere(new WhereSegment(0, 10));
        
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.emptyList());
        PaginationContext paginationContext = new PaginationContextEngine().createPaginationContext(selectStatement, projectionsContext, Collections.emptyList());
        assertFalse(paginationContext.getOffsetSegment().isPresent());
        assertFalse(paginationContext.getRowCountSegment().isPresent());
    }
    
    @Test
    public void assertCreatePaginationContextWhenResultIsPaginationContext() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        ProjectionsContext projectionsContext = new ProjectionsContext(0, 0, false, Collections.emptyList());
        assertThat(new PaginationContextEngine().createPaginationContext(selectStatement, projectionsContext, Collections.emptyList()), instanceOf(PaginationContext.class));
    }
}
