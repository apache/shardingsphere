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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.position.TableCheckRangePosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.IntegerPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

class ConsistencyCheckJobItemContextTest {
    
    private static final String DATA_NODE = "ds_0.t_order";
    
    private static final String TABLE = "t_order";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    @Test
    void assertConstructWithEmptyValues() {
        ConsistencyCheckJobItemProgress jobItemProgress = new ConsistencyCheckJobItemProgress(TABLE, null, 0L, 10L, null, null, "H2");
        ConsistencyCheckJobItemContext actual = new ConsistencyCheckJobItemContext(new ConsistencyCheckJobConfiguration("", "", "DATA_MATCH", null, databaseType),
                0, JobStatus.RUNNING, jobItemProgress);
        assertThat(actual.getProgressContext().getTableCheckRangePositions().size(), is(0));
    }
    
    @Test
    void assertConstructWithNonEmptyValues() {
        ConsistencyCheckJobItemProgress jobItemProgress = new ConsistencyCheckJobItemProgress(TABLE, null, 0L, 10L, null, null, "H2");
        jobItemProgress.getTableCheckRangePositions().add(new TableCheckRangePosition(0, DATA_NODE, TABLE, createIntegerPosition(1L, 100L),
                createIntegerPosition(1L, 101L), null, 11, 11, false, null));
        jobItemProgress.getTableCheckRangePositions().add(new TableCheckRangePosition(1, DATA_NODE, TABLE, createIntegerPosition(101L, 200L),
                createIntegerPosition(101L, 203L), null, 132, 132, false, null));
        ConsistencyCheckJobItemContext actual = new ConsistencyCheckJobItemContext(new ConsistencyCheckJobConfiguration("", "", "DATA_MATCH", null, databaseType),
                0, JobStatus.RUNNING, jobItemProgress);
        assertThat(actual.getProgressContext().getTableCheckRangePositions().size(), is(2));
        assertTableCheckRangePosition(actual.getProgressContext().getTableCheckRangePositions().get(0),
                new TableCheckRangePosition(0, DATA_NODE, TABLE, createIntegerPosition(1L, 100L), createIntegerPosition(1L, 101L), null, 11, 11, false, null));
        assertTableCheckRangePosition(actual.getProgressContext().getTableCheckRangePositions().get(1),
                new TableCheckRangePosition(1, DATA_NODE, TABLE, createIntegerPosition(101L, 200L), createIntegerPosition(101L, 203L), null, 132, 132, false, null));
    }
    
    private IntegerPrimaryKeyIngestPosition createIntegerPosition(final long beginValue, final long endValue) {
        return new IntegerPrimaryKeyIngestPosition(BigInteger.valueOf(beginValue), BigInteger.valueOf(endValue));
    }
    
    private void assertTableCheckRangePosition(final TableCheckRangePosition actual, final TableCheckRangePosition expected) {
        assertRange(actual.getSourceRange(), expected.getSourceRange());
        assertRange(actual.getTargetRange(), expected.getTargetRange());
        assertThat(actual.getSplittingItem(), is(expected.getSplittingItem()));
        assertThat(actual.getSourceDataNode(), is(expected.getSourceDataNode()));
        assertThat(actual.getLogicTableName(), is(expected.getLogicTableName()));
        assertThat(actual.getSourcePosition(), is(expected.getSourcePosition()));
        assertThat(actual.getTargetPosition(), is(expected.getTargetPosition()));
        assertThat(actual.isFinished(), is(expected.isFinished()));
    }
    
    private void assertRange(final PrimaryKeyIngestPosition<?> actual, final PrimaryKeyIngestPosition<?> expected) {
        assertThat(actual.getClass(), is(expected.getClass()));
        assertThat(actual, instanceOf(IntegerPrimaryKeyIngestPosition.class));
        assertThat(((IntegerPrimaryKeyIngestPosition) actual).getBeginValue(), is(((IntegerPrimaryKeyIngestPosition) expected).getBeginValue()));
        assertThat(((IntegerPrimaryKeyIngestPosition) actual).getEndValue(), is(((IntegerPrimaryKeyIngestPosition) expected).getEndValue()));
    }
}
