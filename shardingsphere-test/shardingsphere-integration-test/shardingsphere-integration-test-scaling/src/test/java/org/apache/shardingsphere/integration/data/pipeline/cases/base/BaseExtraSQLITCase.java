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
import org.apache.shardingsphere.integration.data.pipeline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;

import javax.xml.bind.JAXB;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class BaseExtraSQLITCase extends BaseITCase {
    
    @Getter
    private final ExtraSQLCommand extraSQLCommand;
    
    public BaseExtraSQLITCase(final ScalingParameterized parameterized) {
        super(parameterized);
        extraSQLCommand = JAXB.unmarshal(BaseExtraSQLITCase.class.getClassLoader().getResource(parameterized.getScenario()), ExtraSQLCommand.class);
    }
    
    protected void createNoUseTable() {
        executeWithLog("CREATE SHARDING TABLE RULE no_use (RESOURCES(ds_0, ds_1), SHARDING_COLUMN=sharding_id, TYPE(NAME=MOD,PROPERTIES('sharding-count'=4)))");
        executeWithLog("CREATE TABLE no_use(id int(11) NOT NULL,sharding_id int(11) NOT NULL, PRIMARY KEY (id))");
    }
    
    protected void createOrderTable() {
        executeWithLog(extraSQLCommand.getCreateTableOrder());
    }
    
    protected void createTableIndexList() {
        List<String> createTableIndexList = extraSQLCommand.getCreateTableIndexList();
        for (String each : createTableIndexList) {
            executeWithLog(each);
        }
    }
    
    protected void createOrderItemTable() {
        executeWithLog(extraSQLCommand.getCreateTableOrderItem());
    }
    
    @Override
    protected void assertStopScalingSourceWriting() {
        assertFalse(executeSql(extraSQLCommand.getUpdateTableOrderStatus()));
        assertFalse(executeSql(extraSQLCommand.getCreateIndexStatus()));
    }
    
    @Override
    protected void assertRestoreScalingSourceWriting() {
        assertTrue(executeSql(extraSQLCommand.getUpdateTableOrderStatus()));
        assertTrue(executeSql(extraSQLCommand.getCreateIndexStatus()));
    }
    
    private boolean executeSql(final String sql) {
        try {
            getJdbcTemplate().execute(sql);
            return true;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            assertTrue(ex.getCause().getMessage().endsWith("The database sharding_db is read-only"));
            return false;
        }
    }
}
