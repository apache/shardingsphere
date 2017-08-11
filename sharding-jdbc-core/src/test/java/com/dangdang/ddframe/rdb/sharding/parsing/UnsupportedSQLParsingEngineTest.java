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

package com.dangdang.ddframe.rdb.sharding.parsing;

import com.dangdang.ddframe.rdb.common.jaxb.SQLStatement;
import com.dangdang.ddframe.rdb.common.jaxb.helper.SQLStatementHelper;
import com.dangdang.ddframe.rdb.sharding.api.fixture.ShardingRuleMockBuilder;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public final class UnsupportedSQLParsingEngineTest {
    
    private String testCaseName;
    
    private String sql;
    
    private Set<DatabaseType> types;
    
    public UnsupportedSQLParsingEngineTest(final String testCaseName, final String sql, final Set<DatabaseType> types) {
        this.testCaseName = testCaseName;
        this.sql = sql;
        this.types = types;
    }
    
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> dataParameters() {
        Collection<Object[]> result = new ArrayList<>();
        for (SQLStatement each : SQLStatementHelper.getUnsupportedSqlStatements()) {
            Object[] object = new Object[3];
            object[0] = each.getId();
            object[1] = each.getSql();
            object[2] = getTypes(each.getTypes());
            result.add(object);
        }
        return result;
    }
    
    private static Set<DatabaseType> getTypes(final String types) {
        if (types == null || types.isEmpty()) {
            return Sets.newHashSet(DatabaseType.values());
        }
        Set<DatabaseType> result = new HashSet<>();
        for (String each : types.split(",")) {
            result.add(DatabaseType.valueOf(each));
        }
        return result;
    }
    
    @Test
    public void assertUnsupportedStatement() {
        for (DatabaseType each : types) {
            try {
                new SQLParsingEngine(each, sql,
                        new ShardingRuleMockBuilder().addShardingColumns("user_id").addShardingColumns("order_id")
                                .addGenerateKeyColumn("t_order", "order_id").build()).parse();
                fail(String.format("Should have thrown an SQLParsingUnsupportedException because %s is invalid!", sql));
                //CHECKSTYLE:OFF
            } catch (final Exception exception) {
                //CHECKSTYLE:ON
                assertTrue(exception instanceof SQLParsingUnsupportedException);
            }
        }
    }
}
