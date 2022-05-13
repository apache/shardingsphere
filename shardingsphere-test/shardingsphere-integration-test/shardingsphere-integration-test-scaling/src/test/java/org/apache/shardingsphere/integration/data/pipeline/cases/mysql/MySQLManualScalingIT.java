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

package org.apache.shardingsphere.integration.data.pipeline.cases.mysql;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.integration.data.pipeline.cases.base.BaseMySQLITCase;
import org.apache.shardingsphere.integration.data.pipeline.cases.scenario.ScalingScenario;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.LinkedList;

/**
 * MySQL manual scaling test case.
 */
@Slf4j
@RunWith(Parameterized.class)
public final class MySQLManualScalingIT extends BaseMySQLITCase {
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    public MySQLManualScalingIT(final ScalingParameterized parameterized) {
        super(parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        for (String version : ENV.getMysqlVersions()) {
            if (Strings.isNullOrEmpty(version)) {
                continue;
            }
            for (String scenario : ScalingScenario.listScenario()) {
                result.add(new ScalingParameterized(DATABASE_TYPE, version, Joiner.on("/").join("env/scenario/manual/mysql", scenario, ScalingScenario.SCENARIO_SUFFIX)));
            }
        }
        return result;
    }
    
    @Before
    public void setUp() {
        addResource();
        initShardingAlgorithm();
        // TODO wait for algorithm init
        ThreadUtil.sleep(2000);
        createScalingRule();
    }
    
    @Test
    public void assertManualScalingSuccess() throws InterruptedException {
        createAllSharingTableRule();
        bindingShardingRule();
        getSqlHelper().createOrderTable();
        getSqlHelper().createOrderItemTable();
        getSqlHelper().initTableData(true);
        startIncrementTask(new SnowflakeKeyGenerateAlgorithm());
        assertOriginalSourceSuccess();
        getJdbcTemplate().execute(getCommonSQLCommand().getAutoAlterTableRule());
        String jobId = String.valueOf(getJdbcTemplate().queryForMap("SHOW SCALING LIST").get("id"));
        getIncreaseTaskThread().join(60 * 1000L);
        assertCheckMatchConsistencySuccess(getJdbcTemplate(), jobId);
    }
}
