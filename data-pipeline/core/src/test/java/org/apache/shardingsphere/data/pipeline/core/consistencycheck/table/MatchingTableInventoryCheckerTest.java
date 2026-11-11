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

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.TableCheckRangePosition;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.RecordTableInventoryCheckCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableInventoryCheckCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.calculator.TableInventoryCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.calculator.TableInventoryCalculator;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.UniqueKeyIngestPosition;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchingTableInventoryCheckerTest {
    
    @Test
    void assertCheckSingleTableInventoryDataMatched() {
        TableCheckRangePosition position = createPosition();
        ConsistencyCheckJobItemProgressContext progressContext = createProgressContext(position);
        MatchingTableInventoryChecker checker = createChecker(progressContext, Collections.singletonList(createCalculatedResult(1)), Collections.singletonList(createCalculatedResult(1)));
        TableDataConsistencyCheckResult actual = checker.checkSingleTableInventoryData();
        assertTrue(actual.isMatched());
        assertFalse(actual.isIgnored());
        assertTrue(position.isFinished());
        assertTrue(position.getMatched());
        assertThat(position.getSourcePosition(), is(1));
        assertThat(position.getTargetPosition(), is(1));
        assertThat(progressContext.getCheckedRecordsCount().get(), is(1L));
    }
    
    @Test
    void assertCheckSingleTableInventoryDataContentMismatch() {
        TableCheckRangePosition position = createPosition();
        ConsistencyCheckJobItemProgressContext progressContext = createProgressContext(position);
        MatchingTableInventoryChecker checker = createChecker(progressContext, Collections.singletonList(createCalculatedResult(1)), Collections.singletonList(createCalculatedResult(2)));
        TableDataConsistencyCheckResult actual = checker.checkSingleTableInventoryData();
        assertFalse(actual.isMatched());
        assertTrue(position.isFinished());
        assertFalse(position.getMatched());
        assertNull(position.getSourcePosition());
        assertNull(position.getTargetPosition());
        assertThat(progressContext.getCheckedRecordsCount().get(), is(0L));
    }
    
    @Test
    void assertCheckSingleTableInventoryDataCountMismatch() {
        TableCheckRangePosition position = createPosition();
        ConsistencyCheckJobItemProgressContext progressContext = createProgressContext(position);
        MatchingTableInventoryChecker checker = createChecker(progressContext, Arrays.asList(createCalculatedResult(1), createCalculatedResult(2)),
                Collections.singletonList(createCalculatedResult(1)));
        TableDataConsistencyCheckResult actual = checker.checkSingleTableInventoryData();
        assertFalse(actual.isMatched());
        assertTrue(position.isFinished());
        assertFalse(position.getMatched());
        assertThat(position.getSourcePosition(), is(1));
        assertThat(position.getTargetPosition(), is(1));
        assertThat(progressContext.getCheckedRecordsCount().get(), is(1L));
    }
    
    @Test
    void assertCheckSingleTableInventoryDataTargetCountMismatch() {
        TableCheckRangePosition position = createPosition();
        ConsistencyCheckJobItemProgressContext progressContext = createProgressContext(position);
        MatchingTableInventoryChecker checker = createChecker(progressContext, Collections.singletonList(createCalculatedResult(1)),
                Arrays.asList(createCalculatedResult(1), createCalculatedResult(2)));
        TableDataConsistencyCheckResult actual = checker.checkSingleTableInventoryData();
        assertFalse(actual.isMatched());
        assertTrue(position.isFinished());
        assertFalse(position.getMatched());
        assertThat(progressContext.getCheckedRecordsCount().get(), is(1L));
    }
    
    private TableCheckRangePosition createPosition() {
        return new TableCheckRangePosition(0, "foo_ds", "foo_table", UniqueKeyIngestPosition.ofUnsplit(), UniqueKeyIngestPosition.ofUnsplit(), null);
    }
    
    private ConsistencyCheckJobItemProgressContext createProgressContext(final TableCheckRangePosition position) {
        ConsistencyCheckJobItemProgressContext result = new ConsistencyCheckJobItemProgressContext("foo_job", 0, "FIXTURE");
        result.getTableCheckRangePositions().add(position);
        return result;
    }
    
    private TableInventoryCheckCalculatedResult createCalculatedResult(final int value) {
        return new RecordTableInventoryCheckCalculatedResult(1, Collections.singletonList(Collections.singletonMap("foo_id", value)));
    }
    
    private MatchingTableInventoryChecker createChecker(final ConsistencyCheckJobItemProgressContext progressContext,
                                                        final Iterable<TableInventoryCheckCalculatedResult> sourceResults,
                                                        final Iterable<TableInventoryCheckCalculatedResult> targetResults) {
        PipelineDataSource dataSource = new PipelineDataSource(new MockedDataSource(), TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        QualifiedTable table = new QualifiedTable("foo_schema", "foo_table");
        TableInventoryCheckParameter param = new TableInventoryCheckParameter("foo_job", dataSource, dataSource, table, table,
                Collections.emptyList(), Collections.emptyList(), null, progressContext);
        return new StubMatchingTableInventoryChecker(param, Arrays.asList(new StubCalculator(sourceResults), new StubCalculator(targetResults)).iterator());
    }
    
    private static final class StubMatchingTableInventoryChecker extends MatchingTableInventoryChecker {
        
        private final Iterator<StubCalculator> calculators;
        
        private StubMatchingTableInventoryChecker(final TableInventoryCheckParameter param, final Iterator<StubCalculator> calculators) {
            super(param);
            this.calculators = calculators;
        }
        
        @Override
        protected TableInventoryCalculator<TableInventoryCheckCalculatedResult> buildSingleTableInventoryCalculator() {
            return calculators.next();
        }
    }
    
    private static final class StubCalculator implements TableInventoryCalculator<TableInventoryCheckCalculatedResult> {
        
        private final Iterable<TableInventoryCheckCalculatedResult> results;
        
        private StubCalculator(final Iterable<TableInventoryCheckCalculatedResult> results) {
            this.results = results;
        }
        
        @Override
        public Iterable<TableInventoryCheckCalculatedResult> calculate(final TableInventoryCalculateParameter param) {
            return results;
        }
        
        @Override
        public void cancel() {
        }
        
        @Override
        public boolean isCanceling() {
            return false;
        }
    }
}
