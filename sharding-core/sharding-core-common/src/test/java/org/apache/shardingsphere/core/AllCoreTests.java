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

package org.apache.shardingsphere.core;

import org.apache.shardingsphere.core.config.AllConfigTests;
import org.apache.shardingsphere.core.constant.AllConstantsTests;
import org.apache.shardingsphere.core.metadata.AllMetaDataTests;
import org.apache.shardingsphere.core.rule.AllRuleTests;
import org.apache.shardingsphere.core.spi.AllSPITests;
import org.apache.shardingsphere.core.strategy.AllStrategyTests;
import org.apache.shardingsphere.core.util.AllUtilTests;
import org.apache.shardingsphere.core.yaml.AllYamlTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        AllConfigTests.class, 
        AllConstantsTests.class,
        AllStrategyTests.class,
        AllSPITests.class,
        AllRuleTests.class,
        AllMetaDataTests.class,
        AllYamlTests.class,
        AllUtilTests.class
})
public final class AllCoreTests {
}
