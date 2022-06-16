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

package org.apache.shardingsphere.integration.data.pipeline.cases.primarykey;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.base.BaseExtraSQLITCase;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@Slf4j
public class TextPrimaryKeyScalingIT extends BaseExtraSQLITCase {
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    public TextPrimaryKeyScalingIT(final ScalingParameterized parameterized) {
        super(parameterized);
        log.info("parameterized:{}", parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        for (String version : ENV.getMysqlVersions()) {
            result.add(new ScalingParameterized(new MySQLDatabaseType(), version, "env/scenario/primarykey/text_primary_key/mysql.xml"));
        }
        for (String version : ENV.getPostgresVersions()) {
            result.add(new ScalingParameterized(new PostgreSQLDatabaseType(), version, "env/scenario/primarykey/text_primary_key/postgresql.xml"));
        }
        for (String version : ENV.getOpenGaussVersions()) {
            result.add(new ScalingParameterized(new OpenGaussDatabaseType(), version, "env/scenario/primarykey/text_primary_key/postgresql.xml"));
        }
        return result;
    }
    
    @Test
    public void assertTextPrimaryKeyScalingSuccess() throws InterruptedException {
        addSourceResource();
        initShardingAlgorithm();
        assertTrue(waitShardingAlgorithmEffect(15));
        createScalingRule();
        createOrderSharingTableRule();
        createOrderTable();
        batchInsertOrder();
        addTargetResource();
        executeWithLog(getCommonSQLCommand().getAutoAlterOrderShardingTableRule());
        String jobId = getScalingJobId();
        waitScalingFinished(jobId);
        assertCheckScalingSuccess(jobId);
        applyScaling(jobId);
        assertPreviewTableSuccess("t_order", Arrays.asList("ds_2", "ds_3", "ds_4"));
    }
    
    private void batchInsertOrder() {
        UUIDKeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
        List<Object[]> orderData = new ArrayList<>(3000);
        for (int i = 1; i <= 3000; i++) {
            orderData.add(new Object[]{keyGenerateAlgorithm.generateKey(), ThreadLocalRandom.current().nextInt(0, 6), ThreadLocalRandom.current().nextInt(0, 6), "OK"});
        }
        getJdbcTemplate().batchUpdate("INSERT INTO t_order (id,order_id,user_id,status) VALUES (?,?,?,?)", orderData);
    }
}
