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

import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.ConsistencyCheckJobItemProgress;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ConsistencyCheckJobItemContextTest {
    
    private static final String TABLE = "t_order";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    @Test
    void assertConstructWithEmptyValues() {
        ConsistencyCheckJobItemProgress jobItemProgress = new ConsistencyCheckJobItemProgress(TABLE, null, 0L, 10L, null, null, "H2");
        ConsistencyCheckJobItemContext actual = new ConsistencyCheckJobItemContext(new ConsistencyCheckJobConfiguration("", "", "DATA_MATCH", null, databaseType),
                0, JobStatus.RUNNING, jobItemProgress);
        assertThat(actual.getProgressContext().getSourceTableCheckPositions().size(), is(0));
        assertThat(actual.getProgressContext().getTargetTableCheckPositions().size(), is(0));
    }
    
    @Test
    void assertConstructWithNonEmptyValues() {
        ConsistencyCheckJobItemProgress jobItemProgress = new ConsistencyCheckJobItemProgress(TABLE, null, 0L, 10L, null, null, "H2");
        jobItemProgress.getSourceTableCheckPositions().put(TABLE, 6);
        jobItemProgress.getTargetTableCheckPositions().put(TABLE, 5);
        ConsistencyCheckJobItemContext actual = new ConsistencyCheckJobItemContext(new ConsistencyCheckJobConfiguration("", "", "DATA_MATCH", null, databaseType),
                0, JobStatus.RUNNING, jobItemProgress);
        assertThat(actual.getProgressContext().getSourceTableCheckPositions().size(), is(1));
        assertThat(actual.getProgressContext().getTargetTableCheckPositions().size(), is(1));
        assertThat(actual.getProgressContext().getSourceTableCheckPositions().get(TABLE), is(6));
        assertThat(actual.getProgressContext().getTargetTableCheckPositions().get(TABLE), is(5));
    }
}
