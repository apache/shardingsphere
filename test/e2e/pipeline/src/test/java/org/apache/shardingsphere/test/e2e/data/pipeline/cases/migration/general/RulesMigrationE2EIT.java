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

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.general;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.PipelineBaseE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * E2E IT for different types of rules, includes:
 * 1) no any rule.
 * 2) only encrypt rule.
 */
@RunWith(Parameterized.class)
@Slf4j
public final class RulesMigrationE2EIT extends AbstractMigrationE2EIT {
    
    public RulesMigrationE2EIT(final PipelineTestParameter testParam) {
        super(testParam);
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineBaseE2EIT.ENV.getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        List<String> versions = PipelineBaseE2EIT.ENV.listStorageContainerImages(new MySQLDatabaseType());
        if (versions.isEmpty()) {
            return result;
        }
        result.add(new PipelineTestParameter(new MySQLDatabaseType(), versions.get(0), "env/scenario/primary_key/text_primary_key/mysql.xml"));
        return result;
    }
    
    @Override
    protected String getSourceTableOrderName() {
        return "t_order";
    }
    
    @Test
    public void assertNoRuleMigrationSuccess() throws Exception {
        assertMigrationSuccess(null);
    }
    
    @Test
    public void assertOnlyEncryptRuleMigrationSuccess() throws Exception {
        assertMigrationSuccess(() -> {
            createTargetOrderTableEncryptRule();
            return null;
        });
    }
    
    private void assertMigrationSuccess(final Callable<Void> addRuleFn) throws Exception {
        initEnvironment(getDatabaseType(), new MigrationJobType());
        createSourceOrderTable();
        try (Connection connection = getSourceDataSource().getConnection()) {
            PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, new UUIDKeyGenerateAlgorithm(), getSourceTableOrderName(), PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT);
        }
        addMigrationSourceResource();
        addMigrationTargetResource();
        if (null != addRuleFn) {
            addRuleFn.call();
        }
        startMigration(getSourceTableOrderName(), getTargetTableOrderName());
        String jobId = listJobId().get(0);
        waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        assertCheckMigrationSuccess(jobId, "DATA_MATCH");
        commitMigrationByJobId(jobId);
        proxyExecuteWithLog("REFRESH TABLE METADATA", 1);
        assertThat(getTargetTableRecordsCount(getSourceTableOrderName()), is(PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT));
    }
}
