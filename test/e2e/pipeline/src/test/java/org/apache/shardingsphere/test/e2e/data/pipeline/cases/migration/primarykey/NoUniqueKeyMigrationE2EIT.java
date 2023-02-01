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
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.PipelineBaseE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.migration.AbstractMigrationE2EIT;
import org.apache.shardingsphere.test.e2e.data.pipeline.env.enums.PipelineEnvTypeEnum;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.helper.PipelineCaseHelper;
import org.apache.shardingsphere.test.e2e.data.pipeline.framework.param.PipelineTestParameter;
import org.apache.shardingsphere.test.e2e.data.pipeline.util.AutoIncrementKeyGenerateAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;

@RunWith(Parameterized.class)
@Slf4j
public final class NoUniqueKeyMigrationE2EIT extends AbstractMigrationE2EIT {
    
    private final PipelineTestParameter testParam;
    
    public NoUniqueKeyMigrationE2EIT(final PipelineTestParameter testParam) {
        super(testParam);
        this.testParam = testParam;
    }
    
    @Parameters(name = "{0}")
    public static Collection<PipelineTestParameter> getTestParameters() {
        Collection<PipelineTestParameter> result = new LinkedList<>();
        if (PipelineBaseE2EIT.ENV.getItEnvType() == PipelineEnvTypeEnum.NONE) {
            return result;
        }
        for (String version : PipelineBaseE2EIT.ENV.listStorageContainerImages(new MySQLDatabaseType())) {
            result.add(new PipelineTestParameter(new MySQLDatabaseType(), version, "env/scenario/primary_key/none_primary_key/mysql.xml"));
        }
        return result;
    }
    
    @Override
    protected String getSourceTableOrderName() {
        return "t_order";
    }
    
    @Test
    public void assertTextPrimaryMigrationSuccess() throws SQLException, InterruptedException {
        log.info("assertTextPrimaryMigrationSuccess testParam:{}", testParam);
        initEnvironment(testParam.getDatabaseType(), new MigrationJobType());
        createSourceOrderTable();
        try (Connection connection = getSourceDataSource().getConnection()) {
            AutoIncrementKeyGenerateAlgorithm generateAlgorithm = new AutoIncrementKeyGenerateAlgorithm();
            PipelineCaseHelper.batchInsertOrderRecordsWithGeneralColumns(connection, generateAlgorithm, getSourceTableOrderName(), PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT);
        }
        addMigrationProcessConfig();
        addMigrationSourceResource();
        addMigrationTargetResource();
        createTargetOrderTableRule();
        startMigration(getSourceTableOrderName(), getTargetTableOrderName());
        String jobId = listJobId().get(0);
        waitIncrementTaskFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        proxyExecuteWithLog("REFRESH TABLE METADATA", 1);
        assertTargetAndSourceCountAreSame();
        commitMigrationByJobId(jobId);
        List<String> lastJobIds = listJobId();
        assertThat(lastJobIds.size(), is(0));
        log.info("{} E2E IT finished, database type={}, docker image={}", this.getClass().getName(), testParam.getDatabaseType(), testParam.getStorageContainerImage());
    }
    
    private void assertTargetAndSourceCountAreSame() {
        List<Map<String, Object>> targetList = queryForListWithLog("SELECT COUNT(*) AS count FROM t_order");
        assertFalse(targetList.isEmpty());
        assertThat(Integer.parseInt(targetList.get(0).get("count").toString()), is(PipelineBaseE2EIT.TABLE_INIT_ROW_COUNT));
    }
}
