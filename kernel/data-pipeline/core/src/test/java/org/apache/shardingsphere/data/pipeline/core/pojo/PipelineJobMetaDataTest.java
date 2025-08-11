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

package org.apache.shardingsphere.data.pipeline.core.pojo;

import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PipelineJobMetaDataTest {
    
    @Test
    void assertNewInstance() {
        JobConfigurationPOJO jobConfigPOJO = new JobConfigurationPOJO();
        jobConfigPOJO.setJobName("foo_job");
        jobConfigPOJO.setDisabled(true);
        jobConfigPOJO.setShardingTotalCount(10);
        jobConfigPOJO.setProps(PropertiesBuilder.build(new Property("create_time", "2000-01-01 00:00:00"), new Property("stop_time", "2000-12-31 00:00:00")));
        jobConfigPOJO.setJobParameter("foo_job_param");
        PipelineJobMetaData actual = new PipelineJobMetaData(jobConfigPOJO);
        assertThat(actual.getJobId(), is("foo_job"));
        assertFalse(actual.isActive());
        assertThat(actual.getJobItemCount(), is(10));
        assertThat(actual.getCreateTime(), is("2000-01-01 00:00:00"));
        assertThat(actual.getStopTime(), is("2000-12-31 00:00:00"));
        assertThat(actual.getJobParameter(), is("foo_job_param"));
    }
}
