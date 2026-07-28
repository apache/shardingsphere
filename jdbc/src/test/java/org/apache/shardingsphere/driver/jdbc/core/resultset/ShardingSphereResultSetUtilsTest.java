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

package org.apache.shardingsphere.driver.jdbc.core.resultset;

import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.junit.jupiter.api.Test;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereResultSetUtilsTest {
    
    @Test
    void assertCreateColumnLabelAndIndexMapWithSelectWithoutExpandProjections() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class);
        when(selectStatementContext.getProjectionsContext()).thenReturn(new ProjectionsContext(0, 0, false, Collections.emptyList()));
        ResultSetMetaData resultSetMetaData = mockResultSetMetaData("label");
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual, is(Collections.singletonMap("label", 1)));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapWithSelectWithExpandProjections() throws SQLException {
        Map<String, Integer> expandedMap = new HashMap<>(2, 1F);
        expandedMap.put("col1", 1);
        expandedMap.put("col2", 2);
        SelectStatementContext selectStatementContext = mockSelectWithExpandProjections(expandedMap);
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, null);
        assertThat(actual, is(expandedMap));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapWhenExpandMapMissesMetaDataLabel() throws SQLException {
        Map<String, Integer> expandedMap = Collections.singletonMap("status_new", 1);
        SelectStatementContext selectStatementContext = mockSelectWithExpandProjections(expandedMap);
        ResultSetMetaData resultSetMetaData = mockResultSetMetaData("order_id", "status_new");
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual.get("order_id"), is(1));
        assertThat(actual.get("status_new"), is(2));
    }
    
    @Test
    void assertCreateColumnLabelAndIndexMapWhenExpandMapSizeEqualsButLabelsDiffer() throws SQLException {
        Map<String, Integer> expandedMap = new HashMap<>(2, 1F);
        expandedMap.put("derived_only", 1);
        expandedMap.put("status_new", 2);
        SelectStatementContext selectStatementContext = mockSelectWithExpandProjections(expandedMap);
        ResultSetMetaData resultSetMetaData = mockResultSetMetaData("order_id", "status_new");
        Map<String, Integer> actual = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(selectStatementContext, resultSetMetaData);
        assertThat(actual.get("order_id"), is(1));
        assertThat(actual.get("status_new"), is(2));
        assertThat(actual.get("derived_only"), is(1));
    }
    
    private SelectStatementContext mockSelectWithExpandProjections(final Map<String, Integer> expandedMap) {
        SelectStatementContext result = mock(SelectStatementContext.class);
        when(result.containsDerivedProjections()).thenReturn(true);
        ProjectionsContext projectionsContext = mock(ProjectionsContext.class);
        when(projectionsContext.getColumnLabelAndIndexMap()).thenReturn(expandedMap);
        when(result.getProjectionsContext()).thenReturn(projectionsContext);
        return result;
    }
    
    private ResultSetMetaData mockResultSetMetaData(final String... columnLabels) throws SQLException {
        ResultSetMetaData result = mock(ResultSetMetaData.class);
        when(result.getColumnCount()).thenReturn(columnLabels.length);
        for (int index = 0; index < columnLabels.length; index++) {
            when(result.getColumnLabel(index + 1)).thenReturn(columnLabels[index]);
        }
        return result;
    }
}
