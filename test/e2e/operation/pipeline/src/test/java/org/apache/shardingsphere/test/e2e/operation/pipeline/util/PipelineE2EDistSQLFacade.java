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

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.PipelineContainerComposer;
import org.awaitility.Awaitility;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Getter
@Slf4j
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
        String sql = String.format("COMMIT %s %s", jobTypeName, jobId);
        containerComposer.proxyExecuteWithLog(sql, 0);
        Awaitility.waitAtMost(10, TimeUnit.SECONDS).until(() -> !listJobIds().contains(jobId));
    }
    
    /**
     * Drop job.
     *
     * @param jobId job id
     * @throws SQLException SQL exception
     */
    public void drop(final String jobId) throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format("DROP %s %s", jobTypeName, jobId), 0);
    }
    
    /**
     * Wait job prepare success.
     *
     * @param jobId job id
     */
    public void waitJobPrepareSuccess(final String jobId) {
        String distSQL = buildShowJobStatusDistSQL(jobId);
        for (int i = 0; i < 5; i++) {
            List<Map<String, Object>> jobStatus = containerComposer.queryForListWithLog(distSQL);
            Set<String> statusSet = jobStatus.stream().map(each -> String.valueOf(each.get("status"))).collect(Collectors.toSet());
            if (statusSet.contains(JobStatus.PREPARING.name()) || statusSet.contains(JobStatus.RUNNING.name())) {
                containerComposer.sleepSeconds(2);
                continue;
            }
            break;
        }
    }
    
    /**
     * Wait job status reached.
     *
     * @param distSQL dist SQL
     * @param jobStatus job status
     * @param maxSleepSeconds max sleep seconds
     * @throws IllegalStateException if job status not reached
     */
    public void waitJobStatusReached(final String distSQL, final JobStatus jobStatus, final int maxSleepSeconds) {
        for (int i = 0, count = maxSleepSeconds / 2 + (0 == maxSleepSeconds % 2 ? 0 : 1); i < count; i++) {
            List<Map<String, Object>> jobStatusRecords = containerComposer.queryForListWithLog(distSQL);
            log.info("Wait job status reached, job status records: {}", jobStatusRecords);
            Set<String> statusSet = jobStatusRecords.stream().map(each -> String.valueOf(each.get("status"))).collect(Collectors.toSet());
            if (statusSet.stream().allMatch(each -> each.equals(jobStatus.name()))) {
                return;
            }
            containerComposer.sleepSeconds(2);
        }
        throw new IllegalStateException("Job status not reached: " + jobStatus);
    }
    
    /**
     * Wait increment task finished.
     *
     * @param jobId job id
     * @return result
     */
    public List<Map<String, Object>> waitIncrementTaskFinished(final String jobId) {
        String distSQL = buildShowJobStatusDistSQL(jobId);
        for (int i = 0; i < 10; i++) {
            List<Map<String, Object>> jobStatusRecords = containerComposer.queryForListWithLog(distSQL);
            log.info("Wait incremental task finished, job status records: {}", jobStatusRecords);
            Set<String> actualStatus = new HashSet<>(jobStatusRecords.size(), 1F);
            Collection<Integer> incrementalIdleSecondsList = new LinkedList<>();
            for (Map<String, Object> each : jobStatusRecords) {
                assertTrue(Strings.isNullOrEmpty((String) each.get("error_message")), "error_message: `" + each.get("error_message") + "`");
                actualStatus.add(each.get("status").toString());
                String incrementalIdleSeconds = (String) each.get("incremental_idle_seconds");
                incrementalIdleSecondsList.add(Strings.isNullOrEmpty(incrementalIdleSeconds) ? 0 : Integer.parseInt(incrementalIdleSeconds));
            }
            if (Collections.min(incrementalIdleSecondsList) <= 5) {
                containerComposer.sleepSeconds(3);
                continue;
            }
            if (actualStatus.size() == 1 && actualStatus.contains(JobStatus.EXECUTE_INCREMENTAL_TASK.name())) {
                return jobStatusRecords;
            }
        }
        return Collections.emptyList();
    }
    
    private String buildShowJobStatusDistSQL(final String jobId) {
        return String.format("SHOW %s STATUS %s", jobTypeName, jobId);
    }
}
