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

package io.shardingjdbc.spring;

import io.shardingjdbc.spring.cases.WithNamespaceAlgorithmClassTest;
import io.shardingjdbc.spring.cases.WithNamespaceAlgorithmExpressionTest;
import io.shardingjdbc.spring.cases.WithNamespaceBindingTablesTest;
import io.shardingjdbc.spring.cases.WithNamespaceDefaultStrategyTest;
import io.shardingjdbc.spring.cases.WithNamespaceDifferentTablesTest;
import io.shardingjdbc.spring.cases.WithNamespaceForMasterSlaveWithDefaultStrategyTest;
import io.shardingjdbc.spring.cases.WithNamespaceForMasterSlaveWithStrategyRefTest;
import io.shardingjdbc.spring.cases.WithNamespaceForMasterSlaveWithStrategyTypeTest;
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
        WithNamespaceDifferentTablesTest.class,
        WithNamespaceForMasterSlaveWithDefaultStrategyTest.class,
        WithNamespaceForMasterSlaveWithStrategyRefTest.class,
        WithNamespaceForMasterSlaveWithStrategyTypeTest.class,
        GenerateKeyDBUnitTest.class,
        MasterSlaveNamespaceTest.class,
        ShardingNamespaceTest.class
    })
public class AllSpringTests {
}
