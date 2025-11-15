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

package org.apache.shardingsphere.data.pipeline.scenario.migration;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlTransmissionJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobTarget;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MigrationJobTypeTest {
    
    @SuppressWarnings("rawtypes")
    private final PipelineJobType jobType = TypedSPILoader.getService(PipelineJobType.class, "MIGRATION");
    
    @Test
    void assertGetOption() {
        PipelineJobOption actual = jobType.getOption();
        assertThat(actual.getCode(), is("01"));
        assertTrue(actual.isTransmissionJob());
        assertThat(actual.getYamlJobConfigurationSwapper(), isA(YamlMigrationJobConfigurationSwapper.class));
        assertThat(actual.getYamlJobItemProgressSwapper(), isA(YamlTransmissionJobItemProgressSwapper.class));
        assertThat(actual.getJobClass(), is(MigrationJob.class));
        assertFalse(actual.isIgnoreToStartDisabledJobWhenJobItemProgressIsFinished());
        assertThat(actual.getToBeStartDisabledNextJobType(), is("CONSISTENCY_CHECK"));
        assertThat(actual.getToBeStoppedPreviousJobType(), is("CONSISTENCY_CHECK"));
        assertFalse(actual.isForceNoShardingWhenConvertToJobConfigurationPOJO());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGetJobTarget() {
        JobDataNodeEntry dataNodeEntry1 = new JobDataNodeEntry("foo_tbl", Arrays.asList(new DataNode("db.foo_tbl_0"), new DataNode("db.foo_tbl_1")));
        JobDataNodeEntry dataNodeEntry2 = new JobDataNodeEntry("bar_tbl", Arrays.asList(new DataNode("db.bar_tbl_0"), new DataNode("db.bar_tbl_1")));
        MigrationJobConfiguration jobConfig = mock(MigrationJobConfiguration.class);
        when(jobConfig.getJobShardingDataNodes()).thenReturn(Collections.singletonList(new JobDataNodeLine(Arrays.asList(dataNodeEntry1, dataNodeEntry2))));
        PipelineJobTarget actual = jobType.getJobTarget(jobConfig);
        assertNull(actual.getDatabaseName());
        assertThat(actual.getTableName(), is("db.foo_tbl_0,db.foo_tbl_1,db.bar_tbl_0,db.bar_tbl_1"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertBuildDataConsistencyChecker() {
        assertThat(jobType.buildDataConsistencyChecker(mock(MigrationJobConfiguration.class), mock(), mock()), isA(PipelineDataConsistencyChecker.class));
    }
}
