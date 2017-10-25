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

package io.shardingjdbc.orchestration.spring;

import io.shardingjdbc.orchestration.spring.cases.WithNamespaceAlgorithmClassTest;
import io.shardingjdbc.orchestration.spring.cases.WithNamespaceForMasterSlaveWithStrategyTypeTest;
import io.shardingjdbc.orchestration.spring.cases.WithNamespaceAlgorithmExpressionTest;
import io.shardingjdbc.orchestration.spring.cases.WithNamespaceBindingTablesTest;
import io.shardingjdbc.orchestration.spring.cases.WithNamespaceDefaultStrategyTest;
import io.shardingjdbc.orchestration.spring.cases.WithNamespaceDifferentTablesTest;
import io.shardingjdbc.orchestration.spring.cases.WithNamespaceForMasterSlaveWithDefaultStrategyTest;
import io.shardingjdbc.orchestration.spring.cases.WithNamespaceForMasterSlaveWithStrategyRefTest;
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
