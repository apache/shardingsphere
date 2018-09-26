/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.spring;

import io.shardingsphere.shardingjdbc.spring.cases.WithNamespaceAlgorithmClassTest;
import io.shardingsphere.shardingjdbc.spring.cases.WithNamespaceAlgorithmExpressionTest;
import io.shardingsphere.shardingjdbc.spring.cases.WithNamespaceBindingTablesTest;
import io.shardingsphere.shardingjdbc.spring.cases.WithNamespaceDefaultStrategyTest;
import io.shardingsphere.shardingjdbc.spring.cases.WithNamespaceDifferentTablesTest;
import io.shardingsphere.shardingjdbc.spring.cases.WithNamespaceForMasterSlaveWithDefaultStrategyTest;
import io.shardingsphere.shardingjdbc.spring.cases.WithNamespaceForMasterSlaveWithStrategyRefTest;
import io.shardingsphere.shardingjdbc.spring.cases.WithNamespaceForMasterSlaveWithStrategyTypeTest;
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
        GenerateKeyJUnitTest.class,
        MasterSlaveNamespaceTest.class,
        ShardingNamespaceTest.class
    })
public final class AllTests {
}
