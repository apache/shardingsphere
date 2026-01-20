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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.opengauss.type.OpenGaussDatabaseType;
import org.apache.shardingsphere.database.connector.postgresql.type.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.type.RunEnvironment.Type;
import org.apache.shardingsphere.test.e2e.operation.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.pipeline.command.MigrationDistSQLCommand;
import org.apache.shardingsphere.test.e2e.operation.pipeline.util.PipelineE2EDistSQLFacade;
import org.awaitility.Awaitility;
import org.opengauss.util.PSQLException;

import javax.xml.bind.JAXB;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    
    public void startCheckAndVerify(final PipelineE2EDistSQLFacade distSQLFacade, final String jobId, final String algorithmType) throws SQLException {
        distSQLFacade.startCheck(jobId, algorithmType, Collections.emptyMap());
        distSQLFacade.verifyCheck(jobId);
    }
}
