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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckIgnoredType;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataMatchTableInventoryCheckerTest {
    
    @Test
    void assertPreCheckWithUniqueKey() {
        try (DataMatchTableDataConsistencyChecker checker = new DataMatchTableDataConsistencyChecker()) {
            List<PipelineColumnMetaData> uniqueKeys = Collections.singletonList(new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "int", false, true, true));
            TableInventoryCheckParameter param = buildTableInventoryCheckParameter(uniqueKeys);
            TableInventoryChecker tableInventoryChecker = checker.buildTableInventoryChecker(param);
            Optional<TableDataConsistencyCheckResult> actual = tableInventoryChecker.preCheck();
            assertFalse(actual.isPresent());
        }
    }
    
    @Test
    void assertPreCheckWithoutUniqueKey() {
        try (DataMatchTableDataConsistencyChecker checker = new DataMatchTableDataConsistencyChecker()) {
            TableInventoryCheckParameter param = buildTableInventoryCheckParameter(Collections.emptyList());
            TableInventoryChecker tableInventoryChecker = checker.buildTableInventoryChecker(param);
            Optional<TableDataConsistencyCheckResult> actual = tableInventoryChecker.preCheck();
            assertTrue(actual.isPresent());
            assertNotNull(actual.get().getIgnoredType());
            assertThat(actual.get().getIgnoredType(), is(TableDataConsistencyCheckIgnoredType.NO_UNIQUE_KEY));
        }
    }
    
    private TableInventoryCheckParameter buildTableInventoryCheckParameter(final List<PipelineColumnMetaData> uniqueKeys) {
        return new TableInventoryCheckParameter("jobId1", null, null, new QualifiedTable(null, "t_order"), new QualifiedTable(null, "t_order"),
                Arrays.asList("order_id", "user_id", "status"), uniqueKeys, null, null);
    }
}
