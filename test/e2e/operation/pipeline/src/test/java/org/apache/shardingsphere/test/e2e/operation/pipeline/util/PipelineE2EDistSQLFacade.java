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

package org.apache.shardingsphere.test.e2e.operation.pipeline.util;

import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.PipelineContainerComposer;
import org.awaitility.Awaitility;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public final class PipelineE2EDistSQLFacade {
    
    private static final String PIPELINE_RULE_SQL_TEMPLATE = "ALTER %s RULE(\n"
            + "READ(WORKER_THREAD=20, BATCH_SIZE=1000, SHARDING_SIZE=100000, RATE_LIMITER (TYPE(NAME='QPS',PROPERTIES('qps'='500')))),\n"
            + "WRITE(WORKER_THREAD=20, BATCH_SIZE=1000, RATE_LIMITER (TYPE(NAME='TPS',PROPERTIES('tps'='2000')))),\n"
            + "STREAM_CHANNEL(TYPE(NAME='MEMORY', PROPERTIES('block-queue-size'=1000))))";
    
    private final PipelineContainerComposer containerComposer;
    
    private final String jobTypeName;
    
    public PipelineE2EDistSQLFacade(final PipelineContainerComposer containerComposer, final PipelineJobType<?> jobType) {
        this.containerComposer = containerComposer;
        jobTypeName = jobType.getType();
    }
    
    /**
     * Load all single tables.
     *
     * @throws SQLException if there's DistSQL execution failure
     */
    public void loadAllSingleTables() throws SQLException {
        containerComposer.proxyExecuteWithLog("LOAD SINGLE TABLE *.*", 5);
    }
    
    /**
     * Create broadcast rule.
     *
     * @param tableName table name
     * @throws SQLException if there's DistSQL execution failure
     */
    public void createBroadcastRule(final String tableName) throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format("CREATE BROADCAST TABLE RULE %s", tableName), 2);
    }
    
    /**
     * Alter pipeline rule.
     *
     * @throws SQLException if there's DistSQL execution failure
     */
    public void alterPipelineRule() throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format(PIPELINE_RULE_SQL_TEMPLATE, jobTypeName), 2);
    }
    
    /**
     * List job ids.
     *
     * @return job ids
     */
    public List<String> listJobIds() {
        return listJobs().stream().map(a -> a.get("id").toString()).collect(Collectors.toList());
    }
    
    /**
     * List jobs.
     *
     * @return jobs
     */
    public List<Map<String, Object>> listJobs() {
        return containerComposer.queryForListWithLog(String.format("SHOW %s LIST", jobTypeName));
    }
    
    /**
     * Get job id by table name.
     *
     * @param tableName table name
     * @return job id
     */
    public String getJobIdByTableName(final String tableName) {
        return listJobs().stream().filter(a -> a.get("tables").toString().equals(tableName)).findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find job by table name: `" + tableName + "` table`")).get("id").toString();
    }
    
    /**
     * Pause job.
     *
     * @param jobId job id
     * @throws SQLException SQL exception
     */
    public void pauseJob(final String jobId) throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format("STOP %s %s", jobTypeName, jobId), 1);
    }
    
    /**
     * Resume job.
     *
     * @param jobId job id
     * @throws SQLException SQL exception
     */
    public void resumeJob(final String jobId) throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format("START %s %s", jobTypeName, jobId), 5);
    }
    
    /**
     * Rollback job.
     *
     * @param jobId job id
     * @throws SQLException SQL exception
     */
    public void rollback(final String jobId) throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format("ROLLBACK %s %s", jobTypeName, jobId), 2);
    }
    
    /**
     * Commit job.
     *
     * @param jobId job id
     * @throws SQLException SQL exception
     */
    public void commit(final String jobId) throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format("COMMIT %s %s", jobTypeName, jobId), 2);
        Awaitility.await().atMost(3, TimeUnit.SECONDS).until(() -> !listJobIds().contains(jobId));
    }
}
