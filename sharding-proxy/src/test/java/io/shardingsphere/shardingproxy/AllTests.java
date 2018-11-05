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

package io.shardingsphere.shardingproxy;

import io.shardingsphere.shardingproxy.config.AllConfigTests;
import io.shardingsphere.shardingproxy.frontend.AllFrontendTests;
import io.shardingsphere.shardingproxy.listener.AllListenerTests;
import io.shardingsphere.shardingproxy.runtime.AllRuntimeTests;
import io.shardingsphere.shardingproxy.transport.AllTransportTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        AllRuntimeTests.class, 
        AllListenerTests.class, 
        AllConfigTests.class, 
        AllTransportTests.class, 
        AllFrontendTests.class
})
public final class AllTests {
}
