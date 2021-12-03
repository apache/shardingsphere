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

package org.apache.shardingsphere.migration.common.job.preparer;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.job.preparer.TableDefinitionSQLType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class AbstractDataSourcePreparerTest {
    
    private static final Pattern PATTERN_CREATE_TABLE_IF_NOT_EXISTS = Pattern.compile("CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+", Pattern.CASE_INSENSITIVE);
    
    private final AbstractDataSourcePreparer preparer = new AbstractDataSourcePreparer() {
        @Override
        public void prepareTargetTables(final JobConfiguration jobConfig) {
        }
    };
    
    @Test
    public void assertGetTableDefinitionSQLType() {
        Collection<Pair<String, TableDefinitionSQLType>> pairs = new ArrayList<>();
        pairs.add(Pair.of("SET search_path = public", TableDefinitionSQLType.UNKNOWN));
        pairs.add(Pair.of("CREATE TABLE t1_0 (id int NOT NULL)", TableDefinitionSQLType.CREATE_TABLE));
        pairs.add(Pair.of("ALTER TABLE t1_0 ADD CONSTRAINT t1_0_pkey PRIMARY KEY (id)", TableDefinitionSQLType.ALTER_TABLE));
        for (Pair<String, TableDefinitionSQLType> each : pairs) {
            TableDefinitionSQLType sqlType = preparer.getTableDefinitionSQLType(each.getKey());
            assertThat(sqlType, is(each.getValue()));
        }
    }
    
    @Test
    public void assertAddIfNotExistsForCreateTableSQL() {
        List<String> createTableSQLs = Arrays.asList("CREATE TABLE IF NOT EXISTS t (id int)", "CREATE TABLE t (id int)",
                "CREATE  TABLE IF \nNOT \tEXISTS t (id int)", "CREATE \tTABLE t (id int)");
        for (String createTableSQL : createTableSQLs) {
            String sql = preparer.addIfNotExistsForCreateTableSQL(createTableSQL);
            assertTrue(PATTERN_CREATE_TABLE_IF_NOT_EXISTS.matcher(sql).find());
        }
    }
    
    @Test
    public void assertReplaceActualTableNameToLogicTableName() {
        String sql = "ALTER TABLE t_order_0 ADD CONSTRAINT t_order_0_uniq UNIQUE (order_id)";
        String expected = "ALTER TABLE t_order ADD CONSTRAINT t_order_0_uniq UNIQUE (order_id)";
        String actual = preparer.replaceActualTableNameToLogicTableName(sql, "t_order_0", "t_order");
        assertThat(actual, is(expected));
    }
}
