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
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.PipelineBaseE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
@Slf4j
public class TextPrimaryKeyMigrationE2EIT extends AbstractMigrationE2EIT {
    
    private final PipelineTestParameter testParam;
    
    public TextPrimaryKeyMigrationE2EIT(final PipelineTestParameter testParam) {
        super(testParam);
        this.testParam = testParam;
    }
    
    @Override
    protected String getSourceTableOrderName() {
        if (DatabaseTypeUtil.isMySQL(getDatabaseType())) {
            return "T_ORDER";
        }
        return "t_order";
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineBaseE2EIT.ENV.getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        for (String version : PipelineBaseE2EIT.ENV.listStorageContainerImages(new MySQLDatabaseType())) {
            result.add(new PipelineTestParameter(new MySQLDatabaseType(), version, "env/scenario/primary_key/text_primary_key/mysql.xml"));
        }
        for (String version : PipelineBaseE2EIT.ENV.listStorageContainerImages(new PostgreSQLDatabaseType())) {
            result.add(new PipelineTestParameter(new PostgreSQLDatabaseType(), version, "env/scenario/primary_key/text_primary_key/postgresql.xml"));
        }
        for (String version : PipelineBaseE2EIT.ENV.listStorageContainerImages(new OpenGaussDatabaseType())) {
            result.add(new PipelineTestParameter(new OpenGaussDatabaseType(), version, "env/scenario/primary_key/text_primary_key/postgresql.xml"));
        }
        return result;
    }
    
    @Test
    public void assertTextPrimaryMigrationSuccess() throws SQLException, InterruptedException {
        log.info("assertTextPrimaryMigrationSuccess testParam:{}", testParam);
        initEnvironment(testParam.getDatabaseType(), new MigrationJobType());
        createSourceOrderTable();
        try (Connection connection = getSourceDataSource().getConnection()) {
            UUIDKeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
            PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, keyGenerateAlgorithm, getSourceTableOrderName(), PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT);
        }
        addMigrationProcessConfig();
        addMigrationSourceResource();
        addMigrationTargetResource();
        createTargetOrderTableRule();
        startMigration(getSourceTableOrderName(), getTargetTableOrderName());
        String jobId = listJobId().get(0);
        sourceExecuteWithLog(String.format("INSERT INTO %s (order_id,user_id,status) VALUES (%s, %s, '%s')", getSourceTableOrderName(), "1000000000", 1, "afterStop"));
        waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        assertCheckMigrationSuccess(jobId, "DATA_MATCH");
        commitMigrationByJobId(jobId);
        List<String> lastJobIds = listJobId();
        assertThat(lastJobIds.size(), is(0));
        log.info("{} E2E IT finished, database type={}, docker image={}", this.getClass().getName(), testParam.getDatabaseType(), testParam.getStorageContainerImage());
    }
}
