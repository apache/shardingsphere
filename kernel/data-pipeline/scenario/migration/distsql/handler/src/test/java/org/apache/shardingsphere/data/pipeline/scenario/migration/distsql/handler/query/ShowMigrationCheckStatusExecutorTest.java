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

package org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.handler.query;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.pojo.ConsistencyCheckJobItemInfo;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.queryable.ShowMigrationCheckStatusStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class ShowMigrationCheckStatusExecutorTest {
    
    private final ShowMigrationCheckStatusExecutor executor = new ShowMigrationCheckStatusExecutor();
    
    @Test
    void assertGetColumnNames() {
        Collection<String> actual = executor.getColumnNames(new ShowMigrationCheckStatusStatement("foo_job"));
        assertThat(actual, is(Arrays.asList("tables", "result", "check_failed_tables", "ignored_tables", "status", "active", "inventory_finished_percentage",
                "inventory_remaining_seconds", "incremental_idle_seconds", "check_begin_time", "check_end_time", "duration_seconds", "algorithm_type", "algorithm_props", "error_message")));
    }
    
    @Test
    void assertGetRows() throws ReflectiveOperationException {
        ConsistencyCheckJobItemInfo jobItemInfo = createJobItemInfo(10L);
        mockJobAPI(jobItemInfo);
        Collection<LocalDataQueryResultRow> actualRows = executor.getRows(new ShowMigrationCheckStatusStatement("foo_job"), mock(ContextManager.class));
        assertThat(actualRows.size(), is(1));
        assertRow(actualRows.iterator().next(), "foo_table", "true", "foo_failed_table", "foo_ignored_table", "EXECUTE_INVENTORY_TASK", "true", "50", "10", "10",
                "foo_begin_time", "foo_end_time", "20", "foo_algorithm", "foo_props", "foo_error");
    }
    
    private ConsistencyCheckJobItemInfo createJobItemInfo(final Long incrementalIdleSeconds) {
        ConsistencyCheckJobItemInfo result = new ConsistencyCheckJobItemInfo();
        result.setTableNames("foo_table");
        result.setCheckSuccess(true);
        result.setCheckFailedTableNames("foo_failed_table");
        result.setIgnoredTableNames("foo_ignored_table");
        result.setStatus(JobStatus.EXECUTE_INVENTORY_TASK);
        result.setActive(true);
        result.setInventoryFinishedPercentage(50);
        result.setInventoryRemainingSeconds(10L);
        result.setIncrementalIdleSeconds(incrementalIdleSeconds);
        result.setCheckBeginTime("foo_begin_time");
        result.setCheckEndTime("foo_end_time");
        result.setDurationSeconds(20L);
        result.setAlgorithmType("foo_algorithm");
        result.setAlgorithmProps("foo_props");
        result.setErrorMessage("foo_error");
        return result;
    }
    
    private void mockJobAPI(final ConsistencyCheckJobItemInfo jobItemInfo) throws ReflectiveOperationException {
        ConsistencyCheckJobAPI jobAPI = mock(ConsistencyCheckJobAPI.class);
        when(jobAPI.getJobItemInfo("foo_job")).thenReturn(jobItemInfo);
        Plugins.getMemberAccessor().set(ShowMigrationCheckStatusExecutor.class.getDeclaredField("jobAPI"), executor, jobAPI);
    }
    
    private void assertRow(final LocalDataQueryResultRow actualRow, final String... expected) {
        for (int i = 0; i < expected.length; i++) {
            assertThat(actualRow.getCell(i + 1), is(expected[i]));
        }
        assertThrows(IllegalArgumentException.class, () -> actualRow.getCell(expected.length + 1));
    }
}
