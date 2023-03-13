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

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.primarykey;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.DatabaseTypeUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(Parameterized.class)
@Slf4j
public class TextPrimaryKeyMigrationE2EIT extends AbstractMigrationE2EIT {
    
    public TextPrimaryKeyMigrationE2EIT(final PipelineTestParameter testParam) {
        super(testParam);
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineE2EEnvironment.getInstance().getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        for (String version : PipelineE2EEnvironment.getInstance().listStorageContainerImages(new MySQLDatabaseType())) {
            result.add(new PipelineTestParameter(new MySQLDatabaseType(), version, "env/scenario/primary_key/text_primary_key/mysql.xml"));
        }
        for (String version : PipelineE2EEnvironment.getInstance().listStorageContainerImages(new PostgreSQLDatabaseType())) {
            result.add(new PipelineTestParameter(new PostgreSQLDatabaseType(), version, "env/scenario/primary_key/text_primary_key/postgresql.xml"));
        }
        for (String version : PipelineE2EEnvironment.getInstance().listStorageContainerImages(new OpenGaussDatabaseType())) {
            result.add(new PipelineTestParameter(new OpenGaussDatabaseType(), version, "env/scenario/primary_key/text_primary_key/postgresql.xml"));
        }
        return result;
    }
    
    @Test
    public void assertTextPrimaryMigrationSuccess() throws SQLException, InterruptedException {
        getContainerComposer().initEnvironment(getContainerComposer().getDatabaseType(), new MigrationJobType());
        getContainerComposer().createSourceOrderTable(getSourceTableOrderName());
        try (Connection connection = getContainerComposer().getSourceDataSource().getConnection()) {
            UUIDKeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
            PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, keyGenerateAlgorithm, getSourceTableOrderName(), PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
        }
        addMigrationProcessConfig();
        addMigrationSourceResource();
        addMigrationTargetResource();
        createTargetOrderTableRule();
        startMigration(getSourceTableOrderName(), getContainerComposer().getTargetTableOrderName());
        String jobId = listJobId().get(0);
        getContainerComposer().sourceExecuteWithLog(String.format("INSERT INTO %s (order_id,user_id,status) VALUES (%s, %s, '%s')", getSourceTableOrderName(), "1000000000", 1, "afterStop"));
        getContainerComposer().waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        assertCheckMigrationSuccess(jobId, "DATA_MATCH");
        commitMigrationByJobId(jobId);
        List<String> lastJobIds = listJobId();
        assertTrue(lastJobIds.isEmpty());
    }
    
    private String getSourceTableOrderName() {
        return DatabaseTypeUtil.isMySQL(getContainerComposer().getDatabaseType()) ? "T_ORDER" : "t_order";
    }
}
