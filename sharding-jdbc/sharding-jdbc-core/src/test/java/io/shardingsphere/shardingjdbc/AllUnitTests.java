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

package io.shardingsphere.shardingjdbc;

import io.shardingsphere.shardingjdbc.api.AllApiTests;
import io.shardingsphere.shardingjdbc.executor.AllExecutorTests;
import io.shardingsphere.shardingjdbc.jdbc.AllJDBCTests;
import io.shardingsphere.shardingjdbc.transaction.AllTransactionTests;
import io.shardingsphere.shardingjdbc.util.AllUtilTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        AllApiTests.class, 
        AllExecutorTests.class, 
        AllJDBCTests.class, 
        AllTransactionTests.class, 
        AllUtilTests.class
    })
public final class AllUnitTests {
}
