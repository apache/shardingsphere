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

package org.apache.shardingsphere.integration.data.pipeline.cases.base;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.MigrationDistSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;

import javax.xml.bind.JAXB;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
public abstract class AbstractMigrationITCase extends BaseITCase {
    
    private final MigrationDistSQLCommand migrationDistSQLCommand;
    
    public AbstractMigrationITCase(final ScalingParameterized parameterized) {
        super(parameterized);
        migrationDistSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BaseITCase.class.getClassLoader().getResource("env/common/command.xml")), MigrationDistSQLCommand.class);
    }
    
    protected void addMigrationSourceResource() throws SQLException {
        if (ENV.getItEnvType() == ITEnvTypeEnum.NATIVE) {
            try {
                proxyExecuteWithLog("DROP MIGRATION SOURCE RESOURCE ds_0", 2);
            } catch (final SQLException ex) {
                log.warn("Drop sharding_db failed, maybe it's not exist. error msg={}", ex.getMessage());
            }
        }
        String addSourceResource = migrationDistSQLCommand.getAddMigrationSourceResourceTemplate().replace("${user}", getUsername())
                .replace("${password}", getPassword())
                .replace("${ds0}", getActualJdbcUrlTemplate(DS_0, true));
        addResource(addSourceResource);
    }
    
    protected void addMigrationTargetResource() throws SQLException {
        String addTargetResource = migrationDistSQLCommand.getAddMigrationTargetResourceTemplate().replace("${user}", getUsername())
                .replace("${password}", getPassword())
                .replace("${ds2}", getActualJdbcUrlTemplate(DS_2, true))
                .replace("${ds3}", getActualJdbcUrlTemplate(DS_3, true))
                .replace("${ds4}", getActualJdbcUrlTemplate(DS_4, true));
        addResource(addTargetResource);
        List<Map<String, Object>> resources = queryForListWithLog("SHOW DATABASE RESOURCES from sharding_db");
        assertThat(resources.size(), is(3));
    }
    
    protected void createTargetOrderTableRule() throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getCreateTargetOrderTableRule(), 2);
    }
    
    protected void createTargetOrderTableEncryptRule() throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getCreateTargetOrderTableEncryptRule(), 2);
    }
    
    protected void createTargetOrderItemTableRule() throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getCreateTargetOrderItemTableRule(), 2);
    }
    
    protected void startMigrationOrderCopy(final boolean withSchema) throws SQLException {
        if (withSchema) {
            proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderCopySingleTableWithSchema(), 1);
        } else {
            proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderCopySingleTable(), 1);
        }
    }
    
    protected void startMigrationOrder() throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderSingleTable(), 1);
    }
    
    protected void startMigrationOrderItem(final boolean withSchema) throws SQLException {
        if (withSchema) {
            proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderItemSingleTableWithSchema(), 1);
        } else {
            proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderItemSingleTable(), 1);
        }
    }
    
    protected void addMigrationProcessConfig() throws SQLException {
        try {
            proxyExecuteWithLog(migrationDistSQLCommand.getAddMigrationProcessConfig(), 0);
        } catch (final SQLException ex) {
            if ("58000".equals(ex.getSQLState()) || "42000".equals(ex.getSQLState())) {
                log.warn(ex.getMessage());
                return;
            }
            throw ex;
        }
    }
    
    protected void stopMigrationByJobId(final String jobId) throws SQLException {
        proxyExecuteWithLog(String.format("STOP MIGRATION '%s'", jobId), 1);
    }
    
    protected void startMigrationByJobId(final String jobId) throws SQLException {
        proxyExecuteWithLog(String.format("START MIGRATION '%s'", jobId), 1);
    }
    
    protected void commitMigrationByJobId(final String jobId) throws SQLException {
        proxyExecuteWithLog(String.format("COMMIT MIGRATION '%s'", jobId), 1);
    }
    
    protected List<String> listJobId() {
        List<Map<String, Object>> jobList = queryForListWithLog("SHOW MIGRATION LIST");
        return jobList.stream().map(a -> a.get("id").toString()).collect(Collectors.toList());
    }
    
    protected String getJobIdByTableName(final String tableName) {
        List<Map<String, Object>> jobList = queryForListWithLog("SHOW MIGRATION LIST");
        return jobList.stream().filter(a -> a.get("tables").toString().equals(tableName)).findFirst().orElseThrow(() -> new RuntimeException("not find " + tableName + " table")).get("id").toString();
    }
    
    @SneakyThrows(InterruptedException.class)
    protected void waitMigrationFinished(final String jobId) {
        if (null != getIncreaseTaskThread()) {
            TimeUnit.SECONDS.timedJoin(getIncreaseTaskThread(), 60);
        }
        log.info("jobId: {}", jobId);
        Set<String> actualStatus;
        for (int i = 0; i < 10; i++) {
            List<Map<String, Object>> showJobStatusResult = showJobStatus(jobId);
            log.info("show migration status result: {}", showJobStatusResult);
            actualStatus = showJobStatusResult.stream().map(each -> each.get("status").toString()).collect(Collectors.toSet());
            assertFalse(CollectionUtils.containsAny(actualStatus, Arrays.asList(JobStatus.PREPARING_FAILURE.name(), JobStatus.EXECUTE_INVENTORY_TASK_FAILURE.name(),
                    JobStatus.EXECUTE_INCREMENTAL_TASK_FAILURE.name())));
            if (actualStatus.size() == 1 && actualStatus.contains(JobStatus.EXECUTE_INCREMENTAL_TASK.name())) {
                break;
            } else if (actualStatus.size() >= 1 && actualStatus.containsAll(new HashSet<>(Arrays.asList("", JobStatus.EXECUTE_INCREMENTAL_TASK.name())))) {
                log.warn("one of the shardingItem was not started correctly");
            }
            ThreadUtil.sleep(3, TimeUnit.SECONDS);
        }
    }
    
    protected List<Map<String, Object>> showJobStatus(final String jobId) {
        return queryForListWithLog(String.format("SHOW MIGRATION STATUS '%s'", jobId));
    }
    
    protected void assertCheckMigrationSuccess(final String jobId) {
        for (int i = 0; i < 10; i++) {
            if (checkJobIncrementTaskFinished(jobId)) {
                break;
            }
            ThreadUtil.sleep(3, TimeUnit.SECONDS);
        }
        boolean secondCheckJobResult = checkJobIncrementTaskFinished(jobId);
        log.info("second check job result: {}", secondCheckJobResult);
        List<Map<String, Object>> checkJobResults = queryForListWithLog(String.format("CHECK MIGRATION '%s' BY TYPE (NAME='DATA_MATCH')", jobId));
        log.info("check job results: {}", checkJobResults);
        for (Map<String, Object> entry : checkJobResults) {
            assertTrue(Boolean.parseBoolean(entry.get("records_content_matched").toString()));
        }
    }
    
    protected boolean checkJobIncrementTaskFinished(final String jobId) {
        List<Map<String, Object>> listJobStatus = showJobStatus(jobId);
        log.info("list job status result: {}", listJobStatus);
        for (Map<String, Object> entry : listJobStatus) {
            if (!JobStatus.EXECUTE_INCREMENTAL_TASK.name().equalsIgnoreCase(entry.get("status").toString())) {
                return false;
            }
            int incrementalIdleSeconds = Integer.parseInt(entry.get("incremental_idle_seconds").toString());
            if (incrementalIdleSeconds < 3) {
                return false;
            }
        }
        return true;
    }
}
