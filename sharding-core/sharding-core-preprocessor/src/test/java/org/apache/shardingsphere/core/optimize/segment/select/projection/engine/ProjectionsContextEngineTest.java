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

package org.apache.shardingsphere.core.optimize.segment.select.projection.engine;

import org.apache.shardingsphere.core.optimize.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.core.optimize.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.core.optimize.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProjectionsContextEngineTest {
    
    @Test
    public void assertProjectionsContextCreatedProperly() {
        ProjectionsContextEngine projectionsContextEngine = new ProjectionsContextEngine(null);
        SelectStatement selectStatement = mock(SelectStatement.class);
        when(selectStatement.getSelectItems()).thenReturn(mock(SelectItemsSegment.class));
        ProjectionsContext projectionsContext = projectionsContextEngine.createProjectionsContext(null, selectStatement, mock(GroupByContext.class), mock(OrderByContext.class));
        assertNotNull(projectionsContext);
    }
}
