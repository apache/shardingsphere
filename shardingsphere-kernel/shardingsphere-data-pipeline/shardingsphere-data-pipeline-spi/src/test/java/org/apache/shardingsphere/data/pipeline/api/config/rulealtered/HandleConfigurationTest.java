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

package org.apache.shardingsphere.data.pipeline.api.config.rulealtered;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class HandleConfigurationTest {

    private static final String TABLE_NAME_1 = "tableName1";

    private static final String TABLE_NAME_2 = "tableName2";

    private static final String TABLE_NAME = "tableName1,tableName2";

    @Test
    public void assertGetJobShardingCountByNull() {
        HandleConfiguration handleConfiguration = new HandleConfiguration();
        handleConfiguration.getJobShardingCount();
        assertThat(handleConfiguration.getJobShardingCount(), is(0));
    }

    @Test
    public void assertGetJobShardingCount() {
        HandleConfiguration handleConfiguration = new HandleConfiguration();
        handleConfiguration.setJobShardingDataNodes(Lists.newArrayList("node1", "node2"));
        handleConfiguration.getJobShardingCount();
        assertThat(handleConfiguration.getJobShardingCount(), is(2));
    }

    @Test
    public void assertSplitLogicTableNames() {
        HandleConfiguration handleConfiguration = new HandleConfiguration();
        handleConfiguration.setLogicTables(TABLE_NAME);
        assertThat(handleConfiguration.splitLogicTableNames(), is(Lists.newArrayList(TABLE_NAME_1, TABLE_NAME_2)));
    }

    @Test
    public void assertGetJobIdDigestBySuperLong() {
        HandleConfiguration handleConfiguration = new HandleConfiguration();
        handleConfiguration.setJobId("jobIdExceed");
        assertThat(handleConfiguration.getJobIdDigest(), is("jobIdE"));
    }

    @Test
    public void assertGetJobIdDigest() {
        HandleConfiguration handleConfiguration = new HandleConfiguration();
        handleConfiguration.setJobId("jobId");
        assertThat(handleConfiguration.getJobIdDigest(), is("jobId"));
    }
}
