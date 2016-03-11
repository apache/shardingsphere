/**
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

package com.dangdang.ddframe.rdb.sharding.config.common.internal;

import com.dangdang.ddframe.rdb.sharding.config.common.exception.MissingConfigNodeException;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ConfigErrorTest extends AbstractConfigTest {
    
    @Test(expected = MissingConfigNodeException.class)
    public void testMissingArgument() {
        try {
            getDelegate("missing_argument");
        } catch (final Exception e) {
            assertThat(e.getMessage(), is("No signature of method: error.missing_argument.table() is applicable for argument types: (java.lang.String) values: [order]" + ConfigUtil.LINE_SEPARATOR +
                    "Standard syntax is table(String logicTable, List actualTables, (Optional)DatabaseShardingStrategy databaseShardingStrategy, (Optional)TableShardingStrategy tableShardingStrategy)"));
            throw e;
        }
    }
    
    @Test(expected = MissingConfigNodeException.class)
    public void testDatabaseStrategyError() {
        try {
            getDelegate("database_strategy_error");
        } catch (final Exception e) {
            assertThat(e.getMessage(), is("No signature of method: error.database_strategy_error.databaseStrategy() is applicable for argument types: (java.lang.String) values: [1]" + ConfigUtil.LINE_SEPARATOR +
                    "Standard syntax is databaseStrategy(List shardingColumns, {})"));
            throw e;
        }
    }
    
    @Test(expected = MissingConfigNodeException.class)
    public void testTableStrategyError() {
        try {
            getDelegate("table_strategy_error");
        } catch (final Exception e) {
            assertThat(e.getMessage(), is("No signature of method: error.table_strategy_error.tableStrategy() is applicable for argument types: (java.lang.String) values: [1]" + ConfigUtil.LINE_SEPARATOR +
                    "Standard syntax is tableStrategy(List shardingColumns, {})"));
            throw e;
        }
    }
    
    @Test(expected = MissingConfigNodeException.class)
    public void testDefaultStrategyError() {
        try {
            getDelegate("default_strategy_error");
        } catch (final Exception e) {
            assertThat(e.getMessage(), is("No signature of method: error.default_strategy_error.defaultStrategy() is applicable for argument types: (java.lang.String) values: [1]" + ConfigUtil.LINE_SEPARATOR +
                    "Standard syntax is defaultStrategy(databaseStrategy or tableStrategy)"));
            throw e;
        }
    }
    
    @Test(expected = MissingConfigNodeException.class)
    public void testBindError() {
        try {
            getDelegate("bind_error");
        } catch (final Exception e) {
            assertThat(e.getMessage(), is("No signature of method: error.bind_error.bind() is applicable for argument types: (java.lang.String) values: [1]" + ConfigUtil.LINE_SEPARATOR +
                    "Standard syntax is bind(List tableNames)"));
            throw e;
        }
    }
    
    @Override
    protected String packageName() {
        return "error";
    }
}
