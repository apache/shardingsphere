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


package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class AlterTransactionRuleHandlerTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: update(final ContextManager contextManager, final AlterTransactionRuleStatement sqlStatement)
     */
    @Test
    public void testUpdate() throws Exception {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        AlterTransactionRuleStatement statement =
                mock(AlterTransactionRuleStatement.class,RETURNS_DEEP_STUBS);
        AlterTransactionRuleHandler alterTransactionRuleHandler =
                new AlterTransactionRuleHandler();
        alterTransactionRuleHandler.update(contextManager,statement);

        //TODO: Test goes here...
    }

    /**
     * Method: getStatement()
     */
    @Test
    public void testGetStatement() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: getDatabaseType()
     */
    @Test
    public void testGetDatabaseType() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: getConnectionSession()
     */
    @Test
    public void testGetConnectionSession() throws Exception {
        //TODO: Test goes here...
    }


    /**
     * Method: buildTransactionRuleConfiguration()
     */
    @Test
    public void testBuildTransactionRuleConfiguration() throws Exception {
        //TODO: Test goes here...
/* 
try { 
   Method method = AlterTransactionRuleHandler.getClass().getMethod("buildTransactionRuleConfiguration"); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

} 
