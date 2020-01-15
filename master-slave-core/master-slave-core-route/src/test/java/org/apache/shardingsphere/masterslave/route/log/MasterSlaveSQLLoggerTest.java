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

package org.apache.shardingsphere.masterslave.route.log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.mockito.Mockito.inOrder;

@RunWith(MockitoJUnitRunner.class)
public final class MasterSlaveSQLLoggerTest {
    
    private static final String SQL = "SELECT * FROM t_user";
    
    @Mock
    private Logger logger;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field field = MasterSlaveSQLLogger.class.getDeclaredField("log");
        setFinalStatic(field, logger);
    }
    
    @Test
    public void assertLogSQLMasterSlave() {
        MasterSlaveSQLLogger.logSQL(SQL, "ms_ds");
        InOrder inOrder = inOrder(logger);
        inOrder.verify(logger).info("Rule Type: master-slave", new Object[]{});
        inOrder.verify(logger).info("SQL: {} ::: DataSource: {}", new Object[]{SQL, "ms_ds"});
    }
    
    private static void setFinalStatic(final Field field, final Object newValue) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
