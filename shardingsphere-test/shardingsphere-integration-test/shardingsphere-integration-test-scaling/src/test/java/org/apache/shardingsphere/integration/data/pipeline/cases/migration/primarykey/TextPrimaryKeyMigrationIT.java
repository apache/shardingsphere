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

package org.apache.shardingsphere.integration.data.pipeline.cases.migration.primarykey;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.migration.AbstractMigrationITCase;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.sharding.algorithm.keygen.UUIDKeyGenerateAlgorithm;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@Slf4j
public class TextPrimaryKeyMigrationIT extends AbstractMigrationITCase {
    
    public TextPrimaryKeyMigrationIT(final ScalingParameterized parameterized) {
        super(parameterized);
        log.info("parameterized:{}", parameterized);
    }
    
    @Parameters(name = "{0}")
    public static Collection<ScalingParameterized> getParameters() {
        Collection<ScalingParameterized> result = new LinkedList<>();
        if (ENV.getItEnvType() == ITEnvTypeEnum.NONE) {
            return result;
        }
        for (String version : ENV.listDatabaseDockerImageNames(new MySQLDatabaseType())) {
            result.add(new ScalingParameterized(new MySQLDatabaseType(), version, "env/scenario/primary_key/text_primary_key/mysql.xml"));
            result.add(new ScalingParameterized(new MySQLDatabaseType(), version, "env/scenario/primary_key/unique_key/mysql.xml"));
        }
        for (String version : ENV.listDatabaseDockerImageNames(new PostgreSQLDatabaseType())) {
            result.add(new ScalingParameterized(new PostgreSQLDatabaseType(), version, "env/scenario/primary_key/text_primary_key/postgresql.xml"));
        }
        for (String version : ENV.listDatabaseDockerImageNames(new OpenGaussDatabaseType())) {
            result.add(new ScalingParameterized(new OpenGaussDatabaseType(), version, "env/scenario/primary_key/text_primary_key/postgresql.xml"));
        }
        return result;
    }
    
    @Test
    public void assertTextPrimaryMigrationSuccess() throws SQLException, InterruptedException {
        createSourceOrderTable();
        batchInsertOrder();
        addMigrationProcessConfig();
        addMigrationSourceResource();
        addMigrationTargetResource();
        createTargetOrderTableRule();
        startMigrationOrder();
        String jobId = listJobId().get(0);
        waitJobFinished(String.format("SHOW MIGRATION STATUS '%s'", jobId));
        sourceExecuteWithLog(String.format("INSERT INTO t_order (order_id,user_id,status) VALUES (%s, %s, '%s')", "1000000000", 1, "afterStop"));
        // TODO The ordering of primary or unique keys for text types is different, may cause check failed, need fix
        if (DatabaseTypeUtil.isMySQL(getDatabaseType())) {
            assertCheckMigrationSuccess(jobId, "CRC32_MATCH");
        } else {
            assertCheckMigrationSuccess(jobId, "DATA_MATCH");
        }
        stopMigrationByJobId(jobId);
        if (ENV.getItEnvType() == ITEnvTypeEnum.DOCKER) {
            commitMigrationByJobId(jobId);
            List<String> lastJobIds = listJobId();
            assertThat(lastJobIds.size(), is(0));
        }
    }
    
    private void batchInsertOrder() throws SQLException {
        UUIDKeyGenerateAlgorithm keyGenerateAlgorithm = new UUIDKeyGenerateAlgorithm();
        try (Connection connection = getSourceDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (order_id,user_id,status) VALUES (?,?,?)");
            for (int i = 0; i < TABLE_INIT_ROW_COUNT; i++) {
                preparedStatement.setObject(1, keyGenerateAlgorithm.generateKey());
                preparedStatement.setObject(2, ThreadLocalRandom.current().nextInt(0, 6));
                preparedStatement.setObject(3, "OK");
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
        log.info("init data succeed");
    }
}
