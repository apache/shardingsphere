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

package org.apache.shardingsphere.shardingjdbc.spring;

import org.apache.shardingsphere.shardingjdbc.spring.cases.WithNamespaceAlgorithmClassTest;
import org.apache.shardingsphere.shardingjdbc.spring.cases.WithNamespaceAlgorithmExpressionTest;
import org.apache.shardingsphere.shardingjdbc.spring.cases.WithNamespaceBindingTablesTest;
import org.apache.shardingsphere.shardingjdbc.spring.cases.WithNamespaceBroadcastTablesTest;
import org.apache.shardingsphere.shardingjdbc.spring.cases.WithNamespaceDefaultStrategyTest;
import org.apache.shardingsphere.shardingjdbc.spring.cases.WithNamespaceDifferentTablesTest;
import org.apache.shardingsphere.shardingjdbc.spring.cases.WithNamespaceForMasterSlaveWithDefaultStrategyTest;
import org.apache.shardingsphere.shardingjdbc.spring.cases.WithNamespaceForMasterSlaveWithStrategyRefTest;
import org.apache.shardingsphere.shardingjdbc.spring.cases.WithNamespaceForMasterSlaveWithStrategyTypeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        WithNamespaceAlgorithmClassTest.class, 
        WithNamespaceDifferentTablesTest.class, 
        WithNamespaceAlgorithmExpressionTest.class, 
        WithNamespaceDefaultStrategyTest.class, 
        WithNamespaceBindingTablesTest.class, 
        WithNamespaceBroadcastTablesTest.class,
        WithNamespaceDifferentTablesTest.class,
        WithNamespaceForMasterSlaveWithDefaultStrategyTest.class,
        WithNamespaceForMasterSlaveWithStrategyRefTest.class,
        WithNamespaceForMasterSlaveWithStrategyTypeTest.class,
        GenerateKeyJUnitTest.class,
        MasterSlaveNamespaceTest.class,
        ShardingNamespaceTest.class
    })
public final class AllTests {
}
