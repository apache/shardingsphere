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

package org.apache.shardingsphere.integration.data.pipeline.cases.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.MigrationDistSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ScalingITEnvTypeEnum;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;

import javax.xml.bind.JAXB;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
public abstract class AbstractMigrationITCase extends BaseITCase {
    
    private final MigrationDistSQLCommand migrationDistSQLCommand;
    
    public AbstractMigrationITCase(final ScalingParameterized parameterized) {
        super(parameterized);
        migrationDistSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BaseITCase.class.getClassLoader().getResource("env/common/command.xml")), MigrationDistSQLCommand.class);
    }
    
    protected void addMigrationSourceResource() throws SQLException {
        if (ENV.getItEnvType() == ScalingITEnvTypeEnum.NATIVE) {
            try {
                proxyExecuteWithLog("DROP MIGRATION SOURCE RESOURCE ds_0", 2);
            } catch (final SQLException ex) {
                log.warn("Drop sharding_db failed, maybe it's not exist. error msg={}", ex.getMessage());
            }
        }
        String addSourceResource = migrationDistSQLCommand.getAddMigrationSourceResourceTemplate().replace("${user}", getUsername())
                .replace("${password}", getPassword())
                .replace("${ds0}", getActualJdbcUrlTemplate(DS_0, true));
        addResource(addSourceResource);
    }
    
    protected void addMigrationTargetResource() throws SQLException {
        String addTargetResource = migrationDistSQLCommand.getAddMigrationTargetResourceTemplate().replace("${user}", getUsername())
                .replace("${password}", getPassword())
                .replace("${ds2}", getActualJdbcUrlTemplate(DS_2, true))
                .replace("${ds3}", getActualJdbcUrlTemplate(DS_3, true))
                .replace("${ds4}", getActualJdbcUrlTemplate(DS_4, true));
        addResource(addTargetResource);
        List<Map<String, Object>> resources = queryForListWithLog("SHOW DATABASE RESOURCES from sharding_db");
        assertThat(resources.size(), is(3));
    }
    
    protected void createTargetOrderTableRule() throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getCreateTargetOrderTableRule(), 2);
    }
    
    protected void createTargetOrderTableEncryptRule() throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getCreateTargetOrderTableEncryptRule(), 2);
    }
    
    protected void createTargetOrderItemTableRule() throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getCreateTargetOrderItemTableRule(), 2);
    }
    
    protected void startMigrationOrderCopy(final boolean withSchema) throws SQLException {
        if (withSchema) {
            proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderCopySingleTableWithSchema(), 1);
        } else {
            proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderCopySingleTable(), 1);
        }
    }
    
    protected void startMigrationOrder() throws SQLException {
        proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderSingleTable(), 1);
    }
    
    protected void startMigrationOrderItem(final boolean withSchema) throws SQLException {
        if (withSchema) {
            proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderItemSingleTableWithSchema(), 1);
        } else {
            proxyExecuteWithLog(migrationDistSQLCommand.getMigrationOrderItemSingleTable(), 1);
        }
    }
    
    protected void addMigrationProcessConfig() throws SQLException {
        try {
            proxyExecuteWithLog(migrationDistSQLCommand.getAddMigrationProcessConfig(), 0);
        } catch (final SQLException ex) {
            if ("58000".equals(ex.getSQLState()) || "42000".equals(ex.getSQLState())) {
                log.warn(ex.getMessage());
                return;
            }
            throw ex;
        }
    }
    
}
