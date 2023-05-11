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

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.command.MigrationDistSQLCommand;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.DatabaseTypeUtils;
import org.opengauss.util.PSQLException;

import javax.xml.bind.JAXB;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Getter
@Slf4j
public abstract class AbstractMigrationE2EIT {
    
    private final MigrationDistSQLCommand migrationDistSQL;
    
    protected AbstractMigrationE2EIT() {
        migrationDistSQL = JAXB.unmarshal(Objects.requireNonNull(AbstractMigrationE2EIT.class.getClassLoader().getResource("env/common/migration-command.xml")), MigrationDistSQLCommand.class);
    }
    
    protected void addMigrationSourceResource(final PipelineContainerComposer containerComposer) throws SQLException {
        if (PipelineEnvTypeEnum.NATIVE == PipelineE2EEnvironment.getInstance().getItEnvType()) {
            try {
                containerComposer.proxyExecuteWithLog("UNREGISTER MIGRATION SOURCE STORAGE UNIT ds_0", 2);
            } catch (final SQLException ex) {
                log.warn("Drop sharding_db failed, maybe it's not exist. error msg={}", ex.getMessage());
            }
        }
        String addSourceResource = migrationDistSQL.getRegisterMigrationSourceStorageUnitTemplate().replace("${user}", containerComposer.getUsername())
                .replace("${password}", containerComposer.getPassword())
                .replace("${ds0}", containerComposer.appendExtraParameter(containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_0, true)));
        containerComposer.addResource(addSourceResource);
    }
    
    protected void addMigrationTargetResource(final PipelineContainerComposer containerComposer) throws SQLException {
        String addTargetResource = migrationDistSQL.getRegisterMigrationTargetStorageUnitTemplate().replace("${user}", containerComposer.getUsername())
                .replace("${password}", containerComposer.getPassword())
                .replace("${ds2}", containerComposer.appendExtraParameter(containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_2, true)))
                .replace("${ds3}", containerComposer.appendExtraParameter(containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_3, true)))
                .replace("${ds4}", containerComposer.appendExtraParameter(containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_4, true)));
        containerComposer.addResource(addTargetResource);
        List<Map<String, Object>> resources = containerComposer.queryForListWithLog("SHOW STORAGE UNITS from sharding_db");
        assertThat(resources.size(), is(3));
    }
    
    protected void createSourceSchema(final PipelineContainerComposer containerComposer, final String schemaName) throws SQLException {
        if (DatabaseTypeUtils.isPostgreSQL(containerComposer.getDatabaseType())) {
            containerComposer.sourceExecuteWithLog(String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName));
            return;
        }
        if (DatabaseTypeUtils.isOpenGauss(containerComposer.getDatabaseType())) {
            try {
                containerComposer.sourceExecuteWithLog(String.format("CREATE SCHEMA %s", schemaName));
            } catch (final SQLException ex) {
                // only used for native mode.
                if (ex instanceof PSQLException && "42P06".equals(ex.getSQLState())) {
                    log.info("Schema {} already exists.", schemaName);
                } else {
                    throw ex;
                }
            }
        }
    }
    
    protected void createTargetOrderTableRule(final PipelineContainerComposer containerComposer) throws SQLException {
        containerComposer.proxyExecuteWithLog(migrationDistSQL.getCreateTargetOrderTableRule(), 2);
    }
    
    protected void createTargetOrderTableEncryptRule(final PipelineContainerComposer containerComposer) throws SQLException {
        containerComposer.proxyExecuteWithLog(migrationDistSQL.getCreateTargetOrderTableEncryptRule(), 2);
    }
    
    protected void createTargetOrderItemTableRule(final PipelineContainerComposer containerComposer) throws SQLException {
        containerComposer.proxyExecuteWithLog(migrationDistSQL.getCreateTargetOrderItemTableRule(), 2);
    }
    
    protected void startMigration(final PipelineContainerComposer containerComposer, final String sourceTableName, final String targetTableName) throws SQLException {
        containerComposer.proxyExecuteWithLog(migrationDistSQL.getMigrationSingleTable(sourceTableName, targetTableName), 5);
    }
    
    protected void startMigrationWithSchema(final PipelineContainerComposer containerComposer, final String sourceTableName, final String targetTableName) throws SQLException {
        containerComposer.proxyExecuteWithLog(migrationDistSQL.getMigrationSingleTableWithSchema(sourceTableName, targetTableName), 5);
    }
    
    protected void addMigrationProcessConfig(final PipelineContainerComposer containerComposer) throws SQLException {
        containerComposer.proxyExecuteWithLog(migrationDistSQL.getAlterMigrationRule(), 0);
    }
    
    protected void stopMigrationByJobId(final PipelineContainerComposer containerComposer, final String jobId) throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format("STOP MIGRATION '%s'", jobId), 1);
    }
    
    protected void startMigrationByJobId(final PipelineContainerComposer containerComposer, final String jobId) throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format("START MIGRATION '%s'", jobId), 1);
    }
    
    protected void commitMigrationByJobId(final PipelineContainerComposer containerComposer, final String jobId) throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format("COMMIT MIGRATION '%s'", jobId), 1);
    }
    
    protected List<String> listJobId(final PipelineContainerComposer containerComposer) {
        List<Map<String, Object>> jobList = containerComposer.queryForListWithLog("SHOW MIGRATION LIST");
        return jobList.stream().map(a -> a.get("id").toString()).collect(Collectors.toList());
    }
    
    protected String getJobIdByTableName(final PipelineContainerComposer containerComposer, final String tableName) {
        List<Map<String, Object>> jobList = containerComposer.queryForListWithLog("SHOW MIGRATION LIST");
        return jobList.stream().filter(a -> a.get("tables").toString().equals(tableName)).findFirst().orElseThrow(() -> new RuntimeException("not find " + tableName + " table")).get("id").toString();
    }
    
    @SneakyThrows(InterruptedException.class)
    protected void assertCheckMigrationSuccess(final PipelineContainerComposer containerComposer, final String jobId, final String algorithmType) throws SQLException {
        containerComposer.proxyExecuteWithLog(String.format("CHECK MIGRATION '%s' BY TYPE (NAME='%s')", jobId, algorithmType), 0);
        // TODO Need to add after the stop then to start, can continue the consistency check from the previous progress
        List<Map<String, Object>> resultList = Collections.emptyList();
        for (int i = 0; i < 10; i++) {
            resultList = containerComposer.queryForListWithLog(String.format("SHOW MIGRATION CHECK STATUS '%s'", jobId));
            if (resultList.isEmpty()) {
                Thread.sleep(3000L);
                continue;
            }
            List<String> checkEndTimeList = resultList.stream().map(map -> map.get("check_end_time").toString()).filter(each -> !Strings.isNullOrEmpty(each)).collect(Collectors.toList());
            if (checkEndTimeList.size() == resultList.size()) {
                break;
            } else {
                Thread.sleep(3000L);
            }
        }
        log.info("check job results: {}", resultList);
        assertFalse(resultList.isEmpty());
        for (Map<String, Object> each : resultList) {
            assertTrue(Boolean.parseBoolean(each.get("result").toString()), String.format("%s check result is false", each.get("tables")));
            assertThat("finished_percentage is not 100", each.get("finished_percentage").toString(), is("100"));
        }
    }
}
