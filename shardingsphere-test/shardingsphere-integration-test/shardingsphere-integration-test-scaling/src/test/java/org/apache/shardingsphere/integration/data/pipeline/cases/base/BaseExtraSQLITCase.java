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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.test.integration.env.container.atomic.util.DatabaseTypeUtil;

import javax.xml.bind.JAXB;
import java.util.Objects;

import static org.junit.Assert.assertTrue;

@Slf4j
public abstract class BaseExtraSQLITCase extends BaseITCase {
    
    @Getter
    private final ExtraSQLCommand extraSQLCommand;
    
    public BaseExtraSQLITCase(final ScalingParameterized parameterized) {
        super(parameterized);
        extraSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BaseExtraSQLITCase.class.getClassLoader().getResource(parameterized.getScenario())), ExtraSQLCommand.class);
    }
    
    protected void createNoUseTable() {
        executeWithLog("CREATE SHARDING TABLE RULE no_use (RESOURCES(ds_0, ds_1), SHARDING_COLUMN=sharding_id, TYPE(NAME='MOD',PROPERTIES('sharding-count'='4')))");
        executeWithLog("CREATE TABLE no_use(id int(11) NOT NULL,sharding_id int(11) NOT NULL, PRIMARY KEY (id))");
    }
    
    protected void createOrderTable() {
        executeWithLog(extraSQLCommand.getCreateTableOrder());
    }
    
    protected void createTableIndexList(final String schema) {
        if (DatabaseTypeUtil.isPostgreSQL(getDatabaseType())) {
            executeWithLog(String.format("CREATE INDEX IF NOT EXISTS idx_user_id ON %s.t_order ( user_id )", schema));
        } else if (DatabaseTypeUtil.isOpenGauss(getDatabaseType())) {
            executeWithLog(String.format("CREATE INDEX idx_user_id ON %s.t_order ( user_id )", schema));
        }
    }
    
    protected void createOrderItemTable() {
        executeWithLog(extraSQLCommand.getCreateTableOrderItem());
    }
    
    private boolean executeSql(final String sql) {
        try {
            getJdbcTemplate().execute(sql);
            return true;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            // TODO openGauss seem return the different error message, need to check it
            if (DatabaseTypeUtil.isOpenGauss(getDatabaseType())) {
                log.info("openGauss error msg:{}", ex.getMessage());
                return false;
            } else {
                assertTrue(ex.getMessage(), ex.getCause().getMessage().endsWith("The database sharding_db is read-only"));
            }
            return false;
        }
    }
}
