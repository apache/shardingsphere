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
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ScalingITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

@RunWith(Parameterized.class)
@Slf4j
public class TextPrimaryKeyMigrationIT extends BaseExtraSQLITCase {
    
    public TextPrimaryKeyMigrationIT(final ScalingParameterized parameterized) {
        super(parameterized);
        log.info("parameterized:{}", parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        if (ENV.getItEnvType() == ScalingITEnvTypeEnum.NONE) {
            return result;
        }
        for (String version : ENV.listDatabaseDockerImageNames(new MySQLDatabaseType())) {
            result.add(new ScalingParameterized(new MySQLDatabaseType(), version, "env/scenario/primarykey/text_primary_key/mysql.xml"));
        }
        for (String version : ENV.listDatabaseDockerImageNames(new PostgreSQLDatabaseType())) {
            result.add(new ScalingParameterized(new PostgreSQLDatabaseType(), version, "env/scenario/primarykey/text_primary_key/postgresql.xml"));
        }
        for (String version : ENV.listDatabaseDockerImageNames(new OpenGaussDatabaseType())) {
            result.add(new ScalingParameterized(new OpenGaussDatabaseType(), version, "env/scenario/primarykey/text_primary_key/postgresql.xml"));
        }
        return result;
    }
    
    @Test
    public void assertTextPrimaryMigrationSuccess() throws InterruptedException, SQLException {
        createSourceOrderTable();
        batchInsertOrder();
        createScalingRule();
        addSourceResource();
        addTargetResource();
        createTargetOrderTableRule();
        startMigrationOrder();
        String jobId = listJobId().get(0);
        waitMigrationFinished(jobId);
        stopMigration(jobId);
        assertCheckScalingSuccess(jobId);
    }
    
    private void batchInsertOrder() throws SQLException {
        UUIDKeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
        try (Connection connection = getSourceDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (id,order_id,user_id,status) VALUES (?,?,?,?)");
            for (int i = 0; i < TABLE_INIT_ROW_COUNT; i++) {
                preparedStatement.setObject(1, keyGenerateAlgorithm.generateKey());
                preparedStatement.setObject(2, ThreadLocalRandom.current().nextInt(0, 6));
                preparedStatement.setObject(3, ThreadLocalRandom.current().nextInt(0, 6));
                preparedStatement.setObject(4, "OK");
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }
}
