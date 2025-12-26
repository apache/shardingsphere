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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PipelineInventoryCalculateSQLBuilderTest {
    
    private static final Collection<String> COLUMN_NAMES = Arrays.asList("order_id", "user_id", "status");
    
    private static final List<String> UNIQUE_KEYS = Arrays.asList("order_id", "status");
    
    private static final List<String> SHARDING_COLUMNS_NAMES = Collections.singletonList("user_id");
    
    private final PipelineInventoryCalculateSQLBuilder sqlBuilder = new PipelineInventoryCalculateSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
    
    @Test
    void assertBuildRangeQueryOrderingSQLPageQuery() {
        String actual = sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS,
                new Range(1, true, 5), true, SHARDING_COLUMNS_NAMES);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id>=? AND order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC LIMIT ?"));
        actual = sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS,
                new Range(1, false, 5), true, SHARDING_COLUMNS_NAMES);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id>? AND order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC LIMIT ?"));
        actual = sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS,
                new Range(1, false, null), true, SHARDING_COLUMNS_NAMES);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id>? ORDER BY order_id ASC, status ASC, user_id ASC LIMIT ?"));
        actual = sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS,
                new Range(null, false, 5), true, SHARDING_COLUMNS_NAMES);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC LIMIT ?"));
        actual = sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS,
                new Range(null, false, null), true, SHARDING_COLUMNS_NAMES);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order ORDER BY order_id ASC, status ASC, user_id ASC LIMIT ?"));
    }
    
    @Test
    void assertBuildRangeQueryOrderingSQLNotPageQuery() {
        String actual = sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS,
                new Range(1, true, 5), false, SHARDING_COLUMNS_NAMES);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id>=? AND order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC"));
        actual = sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS,
                new Range(1, false, 5), false, SHARDING_COLUMNS_NAMES);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id>? AND order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC"));
        actual = sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS,
                new Range(1, false, null), false, SHARDING_COLUMNS_NAMES);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id>? ORDER BY order_id ASC, status ASC, user_id ASC"));
        actual = sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS,
                new Range(null, false, 5), false, SHARDING_COLUMNS_NAMES);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id<=? ORDER BY order_id ASC, status ASC, user_id ASC"));
        actual = sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS,
                new Range(null, false, null), false, SHARDING_COLUMNS_NAMES);
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order ORDER BY order_id ASC, status ASC, user_id ASC"));
    }
    
    @Test
    void assertBuildPointQuerySQLWithoutQueryCondition() {
        String actual = sqlBuilder.buildPointQuerySQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS, Collections.emptyList());
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id=? AND status=?"));
        actual = sqlBuilder.buildPointQuerySQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS, Collections.emptyList());
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id=? AND status=?"));
        actual = sqlBuilder.buildPointQuerySQL(new QualifiedTable(null, "t_order"), COLUMN_NAMES, UNIQUE_KEYS, Collections.singletonList("user_id"));
        assertThat(actual, is("SELECT order_id,user_id,status FROM t_order WHERE order_id=? AND status=? AND user_id=?"));
    }
}
