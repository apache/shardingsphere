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

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.base.BasePostgreSQLITCase;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(Parameterized.class)
public class PostgreSQLTextPrimaryKeyIT extends BasePostgreSQLITCase {
    
    private static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    public PostgreSQLTextPrimaryKeyIT(final ScalingParameterized parameterized) {
        super(parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        for (String dockerImageName : ENV.getPostgresVersions()) {
            if (Strings.isNullOrEmpty(dockerImageName)) {
                continue;
            }
            result.add(new ScalingParameterized(new PostgreSQLDatabaseType(), dockerImageName, "env/scenario/primarykey/text_primary_key/postgresql.xml"));
        }
        return result;
    }
    
    @Test
    public void assertManualScalingSuccess() throws InterruptedException {
        addSourceResource("root", "root");
        initShardingAlgorithm();
        assertTrue(waitShardingAlgorithmEffect(15));
        createScalingRule();
        createOrderSharingTableRule();
        createOrderTable();
        UUIDKeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
        batchInsertOrder(keyGenerateAlgorithm);
        assertOriginalSourceSuccess();
        addTargetResource("root", "root");
        getJdbcTemplate().execute(getCommonSQLCommand().getAutoAlterOrderShardingTableRule());
        assertCheckMatchConsistencySuccess();
    }
}
