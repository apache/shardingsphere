/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api.rule;

import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLStatementType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MasterSlaveRuleTest {
    
    private MasterSlaveRule masterSlaveRule = new MasterSlaveRule("test_ds", "test_ds_master", Collections.singletonList("test_ds_slave"));
    
    @Before
    @After
    public void reset() throws NoSuchFieldException, IllegalAccessException {
        Field field = MasterSlaveRule.class.getDeclaredField("WAS_UPDATED");
        field.setAccessible(true);
        ((ThreadLocal) field.get(MasterSlaveRule.class)).remove();
    }
    
    @Test
    public void assertGetMasterOrSlaveDataSourceForDML() {
        assertThat(masterSlaveRule.getMasterOrSlaveDataSource(SQLStatementType.INSERT), is("test_ds_master"));
    }
    
    @Test
    public void assertGetMasterOrSlaveDataSourceForDQL() {
        assertThat(masterSlaveRule.getMasterOrSlaveDataSource(SQLStatementType.SELECT), is("test_ds_slave"));
    }
    
    @Test
    public void assertGetMasterOrSlaveDataSourceForDMLAndDQL() {
        assertThat(masterSlaveRule.getMasterOrSlaveDataSource(SQLStatementType.INSERT), is("test_ds_master"));
        assertThat(masterSlaveRule.getMasterOrSlaveDataSource(SQLStatementType.SELECT), is("test_ds_master"));
    }
    
    @Test
    public void assertWithinForMaster() {
        assertTrue(masterSlaveRule.within("test_ds_master"));
    }
    
    @Test
    public void assertWithinForSlave() {
        assertTrue(masterSlaveRule.within("test_ds_slave"));
    }
    
    @Test
    public void assertNotWithin() {
        assertFalse(masterSlaveRule.within("test_ds"));
    }
}
