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

package org.apache.shardingsphere.data.pipeline.core.metadata.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;

import java.util.regex.Pattern;

/**
 * Pipeline meta data node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineMetaDataNode {
    
    private static final String JOB_PATTERN_PREFIX = DataPipelineConstants.DATA_PIPELINE_ROOT + "/jobs/(j\\d{2}\\d{2}[0-9a-z]+)";
    
    public static final Pattern CONFIG_PATTERN = Pattern.compile(JOB_PATTERN_PREFIX + "/config");
    
    public static final Pattern BARRIER_PATTERN = Pattern.compile(JOB_PATTERN_PREFIX + "/barrier/(enable|disable)/\\d+");
    
    /**
     * Get metadata data sources path.
     *
     * @param jobType job type
     * @return data sources path
     */
    public static String getMetaDataDataSourcesPath(final JobType jobType) {
        return String.join("/", getMetaDataRootPath(jobType), "dataSources");
    }
    
    private static String getMetaDataRootPath(final JobType jobType) {
        if (null != jobType) {
            return String.join("/", DataPipelineConstants.DATA_PIPELINE_ROOT, jobType.getLowercaseTypeName(), "metadata");
        } else {
            return String.join("/", DataPipelineConstants.DATA_PIPELINE_ROOT, "metadata");
        }
    }
    
    /**
     * Get metadata process configuration path.
     *
     * @param jobType job type
     * @return data sources path
     */
    public static String getMetaDataProcessConfigPath(final JobType jobType) {
        return String.join("/", getMetaDataRootPath(jobType), "processConfig");
    }
    
    /**
     * Get ElasticJob namespace.
     *
     * @return namespace
     */
    public static String getElasticJobNamespace() {
        // ElasticJob will persist job to namespace
        return getJobsPath();
    }
    
    private static String getJobsPath() {
        return String.join("/", DataPipelineConstants.DATA_PIPELINE_ROOT, "jobs");
    }
    
    /**
     * Get job root path.
     *
     * @param jobId job id.
     * @return root path
     */
    public static String getJobRootPath(final String jobId) {
        return String.join("/", getJobsPath(), jobId);
    }
    
    /**
     * Get job offset item path.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job offset path
     */
    public static String getJobOffsetItemPath(final String jobId, final int shardingItem) {
        return String.join("/", getJobOffsetPath(jobId), Integer.toString(shardingItem));
    }
    
    /**
     * Get job offset path.
     *
     * @param jobId job id
     * @return job offset path
     */
    public static String getJobOffsetPath(final String jobId) {
        return String.join("/", getJobRootPath(jobId), "offset");
    }
    
    /**
     * Get job config path.
     *
     * @param jobId job id
     * @return job configuration path
     */
    public static String getJobConfigPath(final String jobId) {
        return String.join("/", getJobRootPath(jobId), "config");
    }
    
    /**
     * Get check latest detailed result path.
     *
     * @param jobId job id
     * @return check latest job id path
     */
    public static String getCheckLatestJobIdPath(final String jobId) {
        return String.join("/", getJobRootPath(jobId), "check", "latest_job_id");
    }
    
    /**
     * Get check latest result path.
     *
     * @param jobId job id
     * @param checkJobId check job id
     * @return check latest result path
     */
    public static String getCheckJobResultPath(final String jobId, final String checkJobId) {
        return String.join("/", getCheckJobIdsRootPath(jobId), checkJobId);
    }
    
    /**
     * Get check job ids root path.
     *
     * @param jobId job id
     * @return check job ids root path
     */
    public static String getCheckJobIdsRootPath(final String jobId) {
        return String.join("/", getJobRootPath(jobId), "check", "job_ids");
    }
    
    /**
     * Get check job id path.
     *
     * @param jobId job id
     * @param checkJobId check job id
     * @return check job id path
     */
    public static String getCheckJobIdPath(final String jobId, final String checkJobId) {
        return String.join("/", getCheckJobIdsRootPath(jobId), checkJobId);
    }
    
    /**
     * Get job barrier enable path.
     *
     * @param jobId job id
     * @return job barrier enable path
     */
    public static String getJobBarrierEnablePath(final String jobId) {
        return String.join("/", getJobRootPath(jobId), "barrier", "enable");
    }
    
    /**
     * Get job barrier disable path.
     *
     * @param jobId job id
     * @return job barrier disable path
     */
    public static String getJobBarrierDisablePath(final String jobId) {
        return String.join("/", getJobRootPath(jobId), "barrier", "disable");
    }
    
    /**
     * Get job item error msg.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job item error msg
     */
    public static String getJobItemErrorMessagePath(final String jobId, final int shardingItem) {
        return String.join("/", getJobRootPath(jobId), "error", Integer.toString(shardingItem));
    }
}
