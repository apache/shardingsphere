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

package com.dangdang.ddframe.rdb.common.sql.base;

import com.dangdang.ddframe.rdb.common.jaxb.SqlShardingRule;
import com.dangdang.ddframe.rdb.common.sql.common.SQLAssertUtil;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class AbstractNullableShardingSQLTest extends AbstractSQLAssertTest {
    
    protected AbstractNullableShardingSQLTest(final String testCaseName, final String sql, final Set<DatabaseType> types, final List<SqlShardingRule> sqlShardingRules) {
        super(testCaseName, sql, types, sqlShardingRules);
    }
    
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> dataParameters() {
        return SQLAssertUtil.getDataParameters("integrate/assert/select_aggregate.xml");
    }
}
