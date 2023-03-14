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
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.PipelineContainerComposer;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.PipelineE2EEnvironment;
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
    
    private static final String SOURCE_TABLE_ORDER_NAME = "t_order";
    
    public RulesMigrationE2EIT(final PipelineTestParameter testParam) {
        super(testParam, new MigrationJobType());
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineE2EEnvironment.getInstance().getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        List<String> versions = PipelineE2EEnvironment.getInstance().listStorageContainerImages(new MySQLDatabaseType());
        if (versions.isEmpty()) {
            return result;
        }
        result.add(new PipelineTestParameter(new MySQLDatabaseType(), versions.get(0), "env/scenario/primary_key/text_primary_key/mysql.xml"));
        return result;
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
        getContainerComposer().createSourceOrderTable(SOURCE_TABLE_ORDER_NAME);
        try (Connection connection = getContainerComposer().getSourceDataSource().getConnection()) {
            PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, new UUIDKeyGenerateAlgorithm(), SOURCE_TABLE_ORDER_NAME, PipelineContainerComposer.TABLE_INIT_ROW_COUNT);
        }
        addMigrationSourceResource();
        addMigrationTargetResource();
        if (null != addRuleFn) {
            addRuleFn.call();
        }
        startMigration(SOURCE_TABLE_ORDER_NAME, getContainerComposer().getTargetTableOrderName());
        String jobId = listJobId().get(0);
        getContainerComposer().waitJobPrepareSuccess(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        getContainerComposer().waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        assertCheckMigrationSuccess(jobId, "DATA_MATCH");
        commitMigrationByJobId(jobId);
        getContainerComposer().proxyExecuteWithLog("REFRESH TABLE METADATA", 1);
        assertThat(getContainerComposer().getTargetTableRecordsCount(SOURCE_TABLE_ORDER_NAME), is(PipelineContainerComposer.TABLE_INIT_ROW_COUNT));
    }
}
