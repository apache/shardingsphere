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

package org.apache.shardingsphere.data.pipeline.core.prepare.datasource;

import org.apache.shardingsphere.data.pipeline.api.prepare.datasource.TableDefinitionSQLType;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AbstractDataSourcePreparerTest {
    
    private static final Pattern PATTERN_CREATE_TABLE_IF_NOT_EXISTS = Pattern.compile("CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+", Pattern.CASE_INSENSITIVE);
    
    private final AbstractDataSourcePreparer preparer = new AbstractDataSourcePreparer() {
        
        @Override
        public void prepareTargetTables(final PrepareTargetTablesParameter parameter) {
        }
    };
    
    @Test
    public void assertGetTableDefinitionSQLType() {
        assertThat(preparer.getTableDefinitionSQLType("SET search_path = public"), is(TableDefinitionSQLType.UNKNOWN));
        assertThat(preparer.getTableDefinitionSQLType("CREATE TABLE t1_0 (id int NOT NULL)"), is(TableDefinitionSQLType.CREATE_TABLE));
        assertThat(preparer.getTableDefinitionSQLType("ALTER TABLE t1_0 ADD CONSTRAINT t1_0_pkey PRIMARY KEY (id)"), is(TableDefinitionSQLType.ALTER_TABLE));
    }
    
    @Test
    public void assertAddIfNotExistsForCreateTableSQL() {
        Collection<String> createTableSQLs = Arrays.asList("CREATE TABLE IF NOT EXISTS t (id int)", "CREATE TABLE t (id int)",
                "CREATE  TABLE IF \nNOT \tEXISTS t (id int)", "CREATE \tTABLE t (id int)");
        for (String each : createTableSQLs) {
            String sql = preparer.addIfNotExistsForCreateTableSQL(each);
            assertTrue(PATTERN_CREATE_TABLE_IF_NOT_EXISTS.matcher(sql).find());
        }
    }
    
    @Test
    public void assertReplaceActualTableNameToLogicTableName() {
        String sql = "ALTER TABLE t_order_0 ADD CONSTRAINT t_order_0_uniq UNIQUE (order_id)";
        String expected = "ALTER TABLE t_order ADD CONSTRAINT t_order_uniq UNIQUE (order_id)";
        String actual = preparer.replaceActualTableNameToLogicTableName(sql, "t_order_0", "t_order");
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertReplaceActualTableNameToLogicTableNameTheSame() {
        String sql = "ALTER TABLE t_order ADD CONSTRAINT t_order_uniq UNIQUE (order_id)";
        String expected = "ALTER TABLE t_order ADD CONSTRAINT t_order_uniq UNIQUE (order_id)";
        String actual = preparer.replaceActualTableNameToLogicTableName(sql, "t_order", "t_order");
        assertThat(actual, is(expected));
    }
}
