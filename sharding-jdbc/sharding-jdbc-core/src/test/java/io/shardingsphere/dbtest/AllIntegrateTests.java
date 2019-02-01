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

package io.shardingsphere.dbtest;

import io.shardingsphere.dbtest.engine.dcl.AllDCLTests;
import io.shardingsphere.dbtest.engine.ddl.AllDDLTests;
import io.shardingsphere.dbtest.engine.dml.AllDMLTests;
import io.shardingsphere.dbtest.engine.dql.AllDQLTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        AllDQLTests.class,
        AllDMLTests.class,
        AllDDLTests.class,
        AllDCLTests.class
})
public final class AllIntegrateTests {
}
