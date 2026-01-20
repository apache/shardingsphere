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

package org.apache.shardingsphere.test.e2e.operation.pipeline.cases.migration;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.database.connector.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment.Type;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.command.MigrationDistSQLCommand;
import org.awaitility.Awaitility;
import org.opengauss.util.PSQLException;

import javax.xml.bind.JAXB;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
        migrationDistSQL = JAXB.unmarshal(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("env/common/migration-command.xml")), MigrationDistSQLCommand.class);
    }
    
    protected void addMigrationSourceResource(final PipelineContainerComposer containerComposer) throws SQLException {
        if (Type.NATIVE == E2ETestEnvironment.getInstance().getRunEnvironment().getType()) {
            try {
                containerComposer.proxyExecuteWithLog("UNREGISTER MIGRATION SOURCE STORAGE UNIT ds_0", 2);
            } catch (final SQLException ex) {
                log.warn("Drop sharding_db failed, maybe it's not exist. error msg={}", ex.getMessage());
            }
        }
        String registerMigrationSource = migrationDistSQL.getRegisterMigrationSourceStorageUnitTemplate().replace("${user}", containerComposer.getUsername())
                .replace("${password}", containerComposer.getPassword())
                .replace("${ds0}", containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_0, true));
        containerComposer.proxyExecuteWithLog(registerMigrationSource, 15);
    }
    
    protected void addMigrationTargetResource(final PipelineContainerComposer containerComposer) throws SQLException {
        String addTargetResource = migrationDistSQL.getRegisterMigrationTargetStorageUnitTemplate().replace("${user}", containerComposer.getUsername())
                .replace("${password}", containerComposer.getPassword())
                .replace("${ds2}", containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_2, true))
                .replace("${ds3}", containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_3, true))
                .replace("${ds4}", containerComposer.getActualJdbcUrlTemplate(PipelineContainerComposer.DS_4, true));
        containerComposer.proxyExecuteWithLog(addTargetResource, 0);
        Awaitility.await().ignoreExceptions().atMost(60L, TimeUnit.SECONDS).pollInterval(3L, TimeUnit.SECONDS).until(() -> 3 == containerComposer.showStorageUnitsName().size());
    }
    
    protected void createSourceSchema(final PipelineContainerComposer containerComposer, final String schemaName) throws SQLException {
        if (containerComposer.getDatabaseType() instanceof PostgreSQLDatabaseType) {
            containerComposer.sourceExecuteWithLog(String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName));
            return;
        }
        if (containerComposer.getDatabaseType() instanceof OpenGaussDatabaseType) {
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
        containerComposer.proxyExecuteWithLog(migrationDistSQL.getCreateTargetOrderTableRule(), 0);
        Awaitility.await().atMost(10L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(() -> !containerComposer.queryForListWithLog("SHOW SHARDING TABLE RULE t_order").isEmpty());
    }
    
    protected void createTargetOrderTableEncryptRule(final PipelineContainerComposer containerComposer) throws SQLException {
        containerComposer.proxyExecuteWithLog(migrationDistSQL.getCreateTargetOrderTableEncryptRule(), 2);
    }
    
    protected void createTargetOrderItemTableRule(final PipelineContainerComposer containerComposer) throws SQLException {
        containerComposer.proxyExecuteWithLog(migrationDistSQL.getCreateTargetOrderItemTableRule(), 0);
        Awaitility.await().atMost(4L, TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(() -> !containerComposer.queryForListWithLog("SHOW SHARDING TABLE RULE t_order_item").isEmpty());
    }
    
    protected void startMigration(final PipelineContainerComposer containerComposer, final String sourceTableName, final String targetTableName) throws SQLException {
        containerComposer.proxyExecuteWithLog(migrationDistSQL.getMigrationSingleTable(sourceTableName, targetTableName), 5);
    }
    
    protected void startMigrationWithSchema(final PipelineContainerComposer containerComposer, final String sourceTableName, final String targetTableName) throws SQLException {
        containerComposer.proxyExecuteWithLog(migrationDistSQL.getMigrationSingleTableWithSchema(sourceTableName, targetTableName), 5);
    }
    
    protected void startCheckAndVerify(final PipelineContainerComposer containerComposer, final String jobId, final String algorithmType) throws SQLException {
        startCheckAndVerify(containerComposer, jobId, algorithmType, Collections.emptyMap());
    }
    
    protected void startCheckAndVerify(final PipelineContainerComposer containerComposer, final String jobId,
                                       final String algorithmType, final Map<String, String> algorithmProps) throws SQLException {
        containerComposer.proxyExecuteWithLog(buildConsistencyCheckDistSQL(jobId, algorithmType, algorithmProps), 0);
        // TODO Need to add after the stop then to start, can continue the consistency check from the previous progress
        List<Map<String, Object>> resultList = Collections.emptyList();
        for (int i = 0; i < 30; i++) {
            resultList = containerComposer.queryForListWithLog(String.format("SHOW MIGRATION CHECK STATUS '%s'", jobId));
            if (resultList.isEmpty()) {
                containerComposer.sleepSeconds(3);
                continue;
            }
            List<String> checkEndTimeList = resultList.stream().map(map -> map.get("check_end_time").toString()).filter(each -> !Strings.isNullOrEmpty(each)).collect(Collectors.toList());
            Set<String> finishedPercentages = resultList.stream().map(map -> map.get("inventory_finished_percentage").toString()).collect(Collectors.toSet());
            if (checkEndTimeList.size() == resultList.size() && 1 == finishedPercentages.size() && finishedPercentages.contains("100")) {
                break;
            } else {
                containerComposer.sleepSeconds(1);
            }
        }
        log.info("check job results: {}", resultList);
        assertFalse(resultList.isEmpty());
        for (Map<String, Object> each : resultList) {
            assertTrue(Boolean.parseBoolean(each.get("result").toString()), String.format("%s check result is false", each.get("tables")));
            assertThat("inventory_finished_percentage is not 100", each.get("inventory_finished_percentage").toString(), is("100"));
        }
    }
    
    private String buildConsistencyCheckDistSQL(final String jobId, final String algorithmType, final Map<String, String> algorithmProps) {
        if (null == algorithmProps || algorithmProps.isEmpty()) {
            return String.format("CHECK MIGRATION '%s' BY TYPE (NAME='%s')", jobId, algorithmType);
        }
        String sql = "CHECK MIGRATION '%s' BY TYPE (NAME='%s', PROPERTIES("
                + algorithmProps.entrySet().stream().map(entry -> String.format("'%s'='%s'", entry.getKey(), entry.getValue())).collect(Collectors.joining(","))
                + "))";
        return String.format(sql, jobId, algorithmType);
    }
}
