/*
 * Copyright 1999-2015 dangdang.com.
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

package com.dangdang.ddframe.rdb.common.sql.tbl;

import com.dangdang.ddframe.rdb.common.jaxb.ExpectedData;
import com.dangdang.ddframe.rdb.common.jaxb.SqlParameters;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.Set;

@RunWith(Parameterized.class)
@Ignore
public final class ShardingTablesOnlyWithSelectTest extends AbstractShardingTablesOnlyTest {
    
    public ShardingTablesOnlyWithSelectTest(final String testCaseName, final String sql, final Set<DatabaseType> types, final ExpectedData expectedData, final SqlParameters params) {
        super(testCaseName, sql, types, expectedData, params);
    }
    
    @Parameters(name = "{0}")
    public static Collection<Object[]> dataParameters() {
        return AbstractShardingTablesOnlyTest.dataParameters("integrate/assert/tbl/select");
    }
}
