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

package com.dangdang.ddframe.rdb.sharding.jdbc;

import com.dangdang.ddframe.rdb.sharding.fixture.TestDataSource;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLStatementType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class MasterSlaveDataSourceTest {
    
    private DataSource masterDataSource = new TestDataSource("test_ds_master");
    
    private DataSource slaveDataSource = new TestDataSource("test_ds_slave");
    
    private MasterSlaveDataSource masterSlaveDataSource = new MasterSlaveDataSource("test_ds", masterDataSource, Collections.singletonList(slaveDataSource));
    
    @Before
    @After
    public void reset() throws NoSuchFieldException, IllegalAccessException {
        Field field = MasterSlaveDataSource.class.getDeclaredField("WAS_UPDATED");
        field.setAccessible(true);
        ((ThreadLocal) field.get(MasterSlaveDataSource.class)).remove();
    }
    
    @Test
    public void assertGetDataSourceForDML() {
        assertThat(masterSlaveDataSource.getDataSource(SQLStatementType.INSERT), is(masterDataSource));
    }
    
    @Test
    public void assertGetDataSourceForDQL() {
        assertThat(masterSlaveDataSource.getDataSource(SQLStatementType.SELECT), is(slaveDataSource));
    }
    
    @Test
    public void assertGetDataSourceForDMLAndDQL() {
        assertThat(masterSlaveDataSource.getDataSource(SQLStatementType.INSERT), is(masterDataSource));
        assertThat(masterSlaveDataSource.getDataSource(SQLStatementType.SELECT), is(masterDataSource));
    }
}
