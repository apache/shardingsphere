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

package org.apache.shardingsphere.integration.data.pipeline.cases.migration;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.integration.data.pipeline.cases.base.BaseITCase;
import org.apache.shardingsphere.integration.data.pipeline.command.MigrationDistSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
import org.opengauss.util.PSQLException;

import javax.xml.bind.JAXB;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
public abstract class AbstractMigrationITCase extends BaseITCase {
    
    @Getter
    private final MigrationDistSQLCommand migrationDistSQLCommand;
    
    public AbstractMigrationITCase(final ScalingParameterized parameterized) {
        super(parameterized);
        migrationDistSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BaseITCase.class.getClassLoader().getResource("env/common/migration-command.xml")), MigrationDistSQLCommand.class);
        if (ITEnvTypeEnum.NATIVE == ENV.getItEnvType()) {
            try {
                cleanUpPipelineJobs();
            } catch (final SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    
    private void cleanUpPipelineJobs() throws SQLException {
        List<String> jobIds = listJobId();
        for (String each : jobIds) {
            proxyExecuteWithLog(String.format("ROLLBACK MIGRATION '%s'", each), 0);
        }
    }
    
    protected void addMigrationSourceResource() throws SQLException {
        if (ITEnvTypeEnum.NATIVE == ENV.getItEnvType()) {
            try {
                proxyExecuteWithLog("UNREGISTER MIGRATION SOURCE STORAGE UNIT ds_0", 2);
            } catch (final SQLException ex) {
                log.warn("Drop sharding_db failed, maybe it's not exist. error msg={}", ex.getMessage());
            }
        }
        String addSourceResource = migrationDistSQLCommand.getRegisterMigrationSourceStorageUnitTemplate().replace("${user}", getUsername())
                .replace("${password}", getPassword())
                .replace("${ds0}", appendExtraParam(getActualJdbcUrlTemplate(DS_0, true)));
        addResource(addSourceResource);
    }
    
    protected void addMigrationTargetResource() throws SQLException {
        String addTargetResource = migrationDistSQLCommand.getRegisterMigrationTargetStorageUnitTemplate().replace("${user}", getUsername())
                .replace("${password}", getPassword())
                .replace("${ds2}", appendExtraParam(getActualJdbcUrlTemplate(DS_2, true)))
                .replace("${ds3}", appendExtraParam(getActualJdbcUrlTemplate(DS_3, true)))
                .replace("${ds4}", appendExtraParam(getActualJdbcUrlTemplate(DS_4, true)));
        addResource(addTargetResource);
        List<Map<String, Object>> resources = queryForListWithLog("SHOW STORAGE UNITS from sharding_db");
        assertThat(resources.size(), is(3));
    }
    
    protected void createSourceSchema(final String schemaName) throws SQLException {
        if (DatabaseTypeUtil.isPostgreSQL(getDatabaseType())) {
            sourceExecuteWithLog(String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName));
            return;
        }
        if (DatabaseTypeUtil.isOpenGauss(getDatabaseType())) {
            try {
                sourceExecuteWithLog(String.format("CREATE SCHEMA %s", schemaName));
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
    
    protected void createTargetOrderTableRule() throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getCreateTargetOrderTableRule(), 2);
    }
    
    protected void createTargetOrderTableEncryptRule() throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getCreateTargetOrderTableEncryptRule(), 2);
    }
    
    protected void createTargetOrderItemTableRule() throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getCreateTargetOrderItemTableRule(), 2);
    }
    
    protected void startMigration(final String sourceTableName, final String targetTableName) throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getMigrationSingleTable(sourceTableName, targetTableName), 5);
    }
    
    protected void startMigrationWithSchema(final String sourceTableName, final String targetTableName) throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getMigrationSingleTableWithSchema(sourceTableName, targetTableName), 5);
    }
    
    protected void addMigrationProcessConfig() throws SQLException {
        if (ITEnvTypeEnum.NATIVE == ENV.getItEnvType()) {
            try {
                proxyExecuteWithLog("DROP MIGRATION PROCESS CONFIGURATION '/'", 0);
            } catch (final SQLException ex) {
                log.warn("Drop migration process configuration failed, maybe it's not exist. error msg={}", ex.getMessage());
            }
        }
        proxyExecuteWithLog(migrationDistSQLCommand.getAlterMigrationRule(), 0);
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
    
    protected void assertCheckMigrationSuccess(final String jobId, final String algorithmType) throws SQLException {
        proxyExecuteWithLog(String.format("CHECK MIGRATION '%s' BY TYPE (NAME='%s')", jobId, algorithmType), 0);
        List<Map<String, Object>> resultList = Collections.emptyList();
        for (int i = 0; i < 10; i++) {
            resultList = queryForListWithLog(String.format("SHOW MIGRATION CHECK STATUS '%s'", jobId));
            List<String> checkEndTimeList = resultList.stream().map(map -> map.get("check_end_time").toString()).filter(each -> !Strings.isNullOrEmpty(each)).collect(Collectors.toList());
            if (checkEndTimeList.size() == resultList.size()) {
                break;
            }
            ThreadUtil.sleep(3, TimeUnit.SECONDS);
        }
        log.info("check job results: {}", resultList);
        for (Map<String, Object> each : resultList) {
            assertTrue("check result is false", Boolean.parseBoolean(each.get("result").toString()));
            assertThat("finished_percentage is not 100", each.get("finished_percentage").toString(), is("100"));
        }
    }
}
