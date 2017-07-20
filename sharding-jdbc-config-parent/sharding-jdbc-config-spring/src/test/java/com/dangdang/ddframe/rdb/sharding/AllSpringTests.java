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

package com.dangdang.ddframe.rdb.sharding;

import com.dangdang.ddframe.rdb.sharding.spring.GenerateKeyDBUnitTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.WithoutNamespaceDefaultStrategyTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.WithoutNamespaceTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.namespace.WithNamespaceAlgorithmClassAndPropsTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.namespace.WithNamespaceAlgorithmClassTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.namespace.WithNamespaceAlgorithmExpressionForDynamicTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.namespace.WithNamespaceAlgorithmExpressionTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.namespace.WithNamespaceBindingTablesTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.namespace.WithNamespaceDefaultStrategyTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.namespace.WithNamespaceDifferentTablesTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.namespace.WithNamespaceForIndicatedDataSourceNamesTest;
import com.dangdang.ddframe.rdb.sharding.spring.cases.namespace.WithNamespaceForMasterSlaveTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        WithNamespaceAlgorithmClassTest.class, 
        WithNamespaceAlgorithmClassAndPropsTest.class, 
        WithNamespaceDifferentTablesTest.class, 
        WithNamespaceAlgorithmExpressionTest.class, 
        WithNamespaceAlgorithmExpressionForDynamicTest.class, 
        WithNamespaceDefaultStrategyTest.class, 
        WithNamespaceBindingTablesTest.class, 
        WithoutNamespaceTest.class, 
        WithoutNamespaceDefaultStrategyTest.class, 
        WithNamespaceDifferentTablesTest.class,
        WithNamespaceForIndicatedDataSourceNamesTest.class,
        WithNamespaceForMasterSlaveTest.class,
        GenerateKeyDBUnitTest.class
    })
public class AllSpringTests {
}
