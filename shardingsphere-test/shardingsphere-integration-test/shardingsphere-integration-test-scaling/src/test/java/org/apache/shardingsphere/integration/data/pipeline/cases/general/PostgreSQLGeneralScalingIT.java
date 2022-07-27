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

package org.apache.shardingsphere.integration.data.pipeline.cases.general;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.base.BaseExtraSQLITCase;
import org.apache.shardingsphere.integration.data.pipeline.cases.task.PostgreSQLIncrementTask;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ScalingITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.helper.ScalingCaseHelper;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * PostgreSQL general scaling test case. include openGauss type, same process.
 */
@Slf4j
@RunWith(Parameterized.class)
public final class PostgreSQLGeneralScalingIT extends BaseExtraSQLITCase {
    
    private final ScalingParameterized parameterized;
    
    public PostgreSQLGeneralScalingIT(final ScalingParameterized parameterized) {
        super(parameterized);
        this.parameterized = parameterized;
        log.info("parameterized:{}", parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        if (ENV.getItEnvType() == ScalingITEnvTypeEnum.NONE) {
            return result;
        }
        for (String dockerImageName : ENV.listDatabaseDockerImageNames(new PostgreSQLDatabaseType())) {
            result.add(new ScalingParameterized(new PostgreSQLDatabaseType(), dockerImageName, "env/scenario/general/postgresql.xml"));
        }
        for (String dockerImageName : ENV.listDatabaseDockerImageNames(new OpenGaussDatabaseType())) {
            result.add(new ScalingParameterized(new OpenGaussDatabaseType(), dockerImageName, "env/scenario/general/postgresql.xml"));
        }
        return result;
    }
    
    @Test
    public void assertManualScalingSuccess() throws InterruptedException {
        addSourceResource();
        initShardingAlgorithm();
        assertTrue(waitShardingAlgorithmEffect(15));
        createScalingRule();
        createSchema("test");
        createOrderTableRule();
        createOrderItemTableRule();
        createOrderTable();
        createOrderItemTable();
        // TODO wait kernel support create index if not exists
        // createTableIndexList("test");
        executeWithLog("COMMENT ON COLUMN test.t_order.user_id IS 'user id';");
        SnowflakeKeyGenerateAlgorithm keyGenerateAlgorithm = new SnowflakeKeyGenerateAlgorithm();
        Pair<List<Object[]>, List<Object[]>> dataPair = ScalingCaseHelper.generateFullInsertData(keyGenerateAlgorithm, parameterized.getDatabaseType(), 3000);
        getJdbcTemplate().batchUpdate(getExtraSQLCommand().getFullInsertOrder(), dataPair.getLeft());
        getJdbcTemplate().batchUpdate(getExtraSQLCommand().getFullInsertOrderItem(), dataPair.getRight());
        addTargetResource();
        startIncrementTask(new PostgreSQLIncrementTask(getJdbcTemplate(), new SnowflakeKeyGenerateAlgorithm(), "test", true, 20));
        executeWithLog(getCommonSQLCommand().getAlterOrderWithItemAutoTableRule());
        String jobId = getScalingJobId();
        waitScalingFinished(jobId);
        stopScaling(jobId);
        executeWithLog(String.format("INSERT INTO test.t_order (id,order_id,user_id,status) VALUES (%s, %s, %s, '%s')", keyGenerateAlgorithm.generateKey(), 1, 1, "afterStopScaling"));
        startScaling(jobId);
        assertCheckScalingSuccess(jobId);
        applyScaling(jobId);
        assertPreviewTableSuccess("t_order", Arrays.asList("ds_2", "ds_3", "ds_4"));
        assertPreviewTableSuccess("t_order_item", Arrays.asList("ds_2", "ds_3", "ds_4"));
        restoreScalingSourceWriting(jobId);
        assertRestoreScalingSourceWriting();
    }
}
