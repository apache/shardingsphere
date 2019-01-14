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

package io.shardingsphere.transaction.saga;

import io.shardingsphere.transaction.saga.config.SagaConfigurationLoaderTest;
import io.shardingsphere.transaction.saga.hook.AllHookTests;
import io.shardingsphere.transaction.saga.handler.AllHandlerTests;
import io.shardingsphere.transaction.saga.manager.AllManagerTests;
import io.shardingsphere.transaction.saga.servicecomb.AllServicecombTests;
import io.shardingsphere.transaction.saga.revert.AllRevertTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        SagaConfigurationLoaderTest.class,
        SagaTransactionTest.class,
        SagaSubTransactionTest.class,
        AllHandlerTests.class,
        AllHookTests.class,
        AllManagerTests.class,
        AllRevertTests.class,
        AllServicecombTests.class
})
public final class AllTests {
}
