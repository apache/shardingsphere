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

package org.apache.shardingsphere.infra.datanode;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.InvalidDataNodeFormatException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataNodeTest {
    
    @Test
    void assertNewValidDataNode() {
        DataNode dataNode = new DataNode("ds_0.tbl_0");
        assertThat(dataNode.getDataSourceName(), is("ds_0"));
        assertThat(dataNode.getTableName(), is("tbl_0"));
    }
    
    @Test
    void assertNewInValidDataNodeWithoutDelimiter() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0tbl_0"));
    }
    
    @Test
    void assertNewInValidDataNodeWithTwoDelimiters() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0.db_0.tbl_0.tbl_1"));
    }
    
    @Test
    void assertNewValidDataNodeWithInvalidDelimiter() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0,tbl_0"));
    }
    
    @Test
    void assertFormatWithSchema() {
        assertThat(new DataNode("foo_ds", "foo_schema", "foo_tbl").format(), is("foo_ds.foo_schema.foo_tbl"));
    }
    
    @Test
    void assertFormatWithoutSchema() {
        DataNode dataNode = new DataNode("foo_ds", (String) null, "foo_tbl");
        assertThat(dataNode.format(), is("foo_ds.foo_tbl"));
    }
    
    @SuppressWarnings({"SimplifiableAssertion", "ConstantValue"})
    @Test
    void assertEquals() {
        DataNode dataNode = new DataNode("ds_0.tbl_0");
        assertThat(dataNode, is(dataNode));
        assertThat(dataNode, is(new DataNode("ds_0.tbl_0")));
        assertThat(dataNode, is(new DataNode("DS_0.TBL_0")));
        assertThat(dataNode, not(new DataNode("ds_0.tbl_1")));
        assertThat(dataNode.equals("ds.tbl"), is(false));
        assertFalse(dataNode.equals(null));
    }
    
    @Test
    void assertEqualsWithSchema() {
        DataNode dataNode = new DataNode("ds", "schema1", "tbl");
        assertThat(dataNode, not(new DataNode("ds", "schema2", "tbl")));
        assertThat(dataNode, not(new DataNode("ds", (String) null, "tbl")));
    }
    
    @Test
    void assertHashCode() {
        assertThat(new DataNode("ds_0.tbl_0").hashCode(), is(new DataNode("ds_0.tbl_0").hashCode()));
        assertThat(new DataNode("ds_0.tbl_0").hashCode(), is(new DataNode("DS_0.TBL_0").hashCode()));
        assertThat(new DataNode("ds_0.db_0.tbl_0").hashCode(), is(new DataNode("ds_0.db_0.tbl_0").hashCode()));
        assertThat(new DataNode("ds_0.db_0.tbl_0").hashCode(), is(new DataNode("DS_0.DB_0.TBL_0").hashCode()));
        assertThat(new DataNode("DS", "SCHEMA", "TBL").hashCode(), is(new DataNode("ds", "schema", "tbl").hashCode()));
    }
    
    @Test
    void assertToString() {
        assertThat(new DataNode("ds_0.tbl_0").toString(), is("DataNode(dataSourceName=ds_0, schemaName=null, tableName=tbl_0)"));
        assertThat(new DataNode("ds", "schema", "tbl").toString(), is("DataNode(dataSourceName=ds, schemaName=schema, tableName=tbl)"));
        assertThat(new DataNode("ds_0.schema_0.tbl_0").toString(), is("DataNode(dataSourceName=ds_0, schemaName=schema_0, tableName=tbl_0)"));
    }
    
    @Test
    void assertEmptyDataSourceDataNode() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode(".tbl_0"));
    }
    
    @Test
    void assertEmptyTableDataNode() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0."));
    }
    
    @Test
    void assertNewValidDataNodeIncludeInstance() {
        DataNode dataNode = new DataNode("ds_0.schema_0.tbl_0");
        assertThat(dataNode.getDataSourceName(), is("ds_0"));
        assertThat(dataNode.getTableName(), is("tbl_0"));
        assertThat(dataNode.getSchemaName(), is("schema_0"));
    }
    
    @Test
    void assertNewDataNodeWithOnlyOneSegment() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0"));
    }
    
    @Test
    void assertNewDataNodeWithFourSegments() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0.db_0.tbl_0.col_0"));
    }
    
    @Test
    void assertNewDataNodeWithEmptySegments() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0..tbl_0"));
    }
    
    @Test
    void assertNewDataNodeWithLeadingDelimiter() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode(".ds_0.tbl_0"));
    }
    
    @Test
    void assertNewDataNodeWithTrailingDelimiter() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0.tbl_0."));
    }
    
    @Test
    void assertNewDataNodeWithMultipleConsecutiveDelimiters() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0..tbl_0"));
    }
    
    @Test
    void assertNewDataNodeWithWhitespaceInSegments() {
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("ds_0 . tbl_0"));
    }
    
    @Test
    void assertNewDataNodeWithSpecialCharacters() {
        DataNode dataNode = new DataNode("ds-0.tbl_0");
        assertThat(dataNode.getDataSourceName(), is("ds-0"));
        assertThat(dataNode.getTableName(), is("tbl_0"));
    }
    
    @Test
    void assertNewDataNodeWithUnderscores() {
        DataNode dataNode = new DataNode("data_source_0.table_name_0");
        assertThat(dataNode.getDataSourceName(), is("data_source_0"));
        assertThat(dataNode.getTableName(), is("table_name_0"));
    }
    
    @Test
    void assertNewDataNodeWithNumbers() {
        DataNode dataNode = new DataNode("ds123.tbl456");
        assertThat(dataNode.getDataSourceName(), is("ds123"));
        assertThat(dataNode.getTableName(), is("tbl456"));
    }
    
    @Test
    void assertNewDataNodeWithMixedFormat() {
        DataNode dataNode = new DataNode("prod-db-01.schema_01.users");
        assertThat(dataNode.getDataSourceName(), is("prod-db-01"));
        assertThat(dataNode.getTableName(), is("users"));
    }
    
    @Test
    void assertNewDataNodeWithLongNames() {
        String longDataSource = "very_long_data_source_name_that_exceeds_normal_length";
        String longTable = "very_long_table_name_that_exceeds_normal_length";
        DataNode dataNode = new DataNode(longDataSource + "." + longTable);
        assertThat(dataNode.getDataSourceName(), is(longDataSource));
        assertThat(dataNode.getTableName(), is(longTable));
    }
    
    @Test
    void assertNewDataNodeWithSingleCharacterNames() {
        DataNode dataNode = new DataNode("a.b");
        assertThat(dataNode.getDataSourceName(), is("a"));
        assertThat(dataNode.getTableName(), is("b"));
    }
    
    @Test
    void assertNewDataNodeWithInstanceFormat() {
        DataNode dataNode = new DataNode("instance1.database1.table1");
        assertThat(dataNode.getDataSourceName(), is("instance1"));
        assertThat(dataNode.getTableName(), is("table1"));
    }
    
    @Test
    void assertNewDataNodeWithComplexInstanceFormat() {
        DataNode dataNode = new DataNode("prod-cluster-01.mysql-master.users");
        assertThat(dataNode.getDataSourceName(), is("prod-cluster-01"));
        assertThat(dataNode.getTableName(), is("users"));
    }
    
    @Test
    void assertNewDataNodeWithDatabaseType() {
        DataNode dataNode = new DataNode("test_db", TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"), "ds.schema.tbl");
        assertThat(dataNode.getDataSourceName(), is("ds"));
        assertThat(dataNode.getSchemaName(), is("schema"));
        assertThat(dataNode.getTableName(), is("tbl"));
    }
    
    @Test
    void assertFormatWithDatabaseType() {
        assertThat(new DataNode("ds", "schema", "tbl").format(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")), is("ds.schema.tbl"));
    }
    
    @Test
    void assertFormatWithDatabaseTypeWithoutSchema() {
        DataNode dataNode = new DataNode("ds", (String) null, "tbl");
        assertThat(dataNode.format(TypedSPILoader.getService(DatabaseType.class, "MySQL")), is("ds.tbl"));
    }
    
    @Test
    void assertNewDataNodeWithDatabaseTypeWithoutSchemaSupport() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        DataNode dataNode = new DataNode("test_db", databaseType, "ds.tbl");
        assertThat(dataNode.getDataSourceName(), is("ds"));
        assertThat(dataNode.getSchemaName(), is("test_db"));
        assertThat(dataNode.getTableName(), is("tbl"));
    }
    
    @Test
    void assertNewDataNodeWithDatabaseTypeAndInvalidThreeSegment() {
        DataNode dataNode = new DataNode("test_db", TypedSPILoader.getService(DatabaseType.class, "MySQL"), "ds.schema.tbl");
        assertThat(dataNode.getDataSourceName(), is("ds"));
        assertThat(dataNode.getSchemaName(), is("test_db"));
        assertThat(dataNode.getTableName(), is("schema.tbl"));
    }
    
    @Test
    void assertNewDataNodeWithDatabaseTypeCheckStateException() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        assertThrows(InvalidDataNodeFormatException.class, () -> new DataNode("test_db", databaseType, "invalid_format_without_delimiter"));
    }
    
    @Test
    void assertFormatWithDatabaseTypeAndNullSchema() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        DataNode dataNode = new DataNode("ds", (String) null, "tbl");
        assertThat(dataNode.format(databaseType), is("ds.tbl"));
    }
    
    @Test
    void assertFormatMethodWithTableNameLowercasing() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        DataNode dataNode = new DataNode("test_db", databaseType, "ds.schema.TABLE");
        assertThat(dataNode.getTableName(), is("table"));
        assertThat(dataNode.getSchemaName(), is("schema"));
    }
}
