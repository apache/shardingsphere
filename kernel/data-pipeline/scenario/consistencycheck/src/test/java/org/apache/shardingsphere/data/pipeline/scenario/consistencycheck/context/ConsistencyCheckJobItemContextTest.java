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

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class ConsistencyCheckJobItemContextTest {
    
    private static final String TABLE = "t_order";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    @Test
    void assertConstructWithoutTableCheckPositions() {
        Map<String, Object> sourceTableCheckPositions = Collections.emptyMap();
        Map<String, Object> targetTableCheckPositions = Collections.emptyMap();
        ConsistencyCheckJobItemProgress jobItemProgress = new ConsistencyCheckJobItemProgress(TABLE, null, 0L, 10L, null, null, sourceTableCheckPositions, targetTableCheckPositions, "H2");
        ConsistencyCheckJobItemContext actual = new ConsistencyCheckJobItemContext(new ConsistencyCheckJobConfiguration("", "", "DATA_MATCH", null, databaseType),
                0, JobStatus.RUNNING, jobItemProgress);
        verifyProgressContext(actual.getProgressContext(), 0, sourceTableCheckPositions, targetTableCheckPositions);
    }
    
    @Test
    void assertConstructWithTableCheckPositions() {
        Map<String, Object> sourceTableCheckPositions = ImmutableMap.of(TABLE, 6);
        Map<String, Object> targetTableCheckPositions = ImmutableMap.of(TABLE, 5);
        ConsistencyCheckJobItemProgress jobItemProgress = new ConsistencyCheckJobItemProgress(TABLE, null, 0L, 10L, null, null, sourceTableCheckPositions, targetTableCheckPositions, "H2");
        ConsistencyCheckJobItemContext actual = new ConsistencyCheckJobItemContext(new ConsistencyCheckJobConfiguration("", "", "DATA_MATCH", null, databaseType),
                0, JobStatus.RUNNING, jobItemProgress);
        verifyProgressContext(actual.getProgressContext(), 1, sourceTableCheckPositions, targetTableCheckPositions);
        assertThat(actual.getProgressContext().getSourceTableCheckPositions().get(TABLE), is(6));
        assertThat(actual.getProgressContext().getTargetTableCheckPositions().get(TABLE), is(5));
    }
    
    private void verifyProgressContext(final ConsistencyCheckJobItemProgressContext progressContext, final int expectedSize,
                                       final Map<String, Object> sourceTableCheckPositions, final Map<String, Object> targetTableCheckPositions) {
        assertThat(progressContext.getSourceTableCheckPositions().size(), is(expectedSize));
        assertThat(progressContext.getTargetTableCheckPositions().size(), is(expectedSize));
        assertNotSame(progressContext.getSourceTableCheckPositions(), sourceTableCheckPositions);
        assertNotSame(progressContext.getTargetTableCheckPositions(), targetTableCheckPositions);
    }
}
