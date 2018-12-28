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

package io.shardingsphere.core.metadata.table;

import com.google.common.base.Joiner;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.ColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnAfterPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnFirstPositionSegment;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.CreateTableStatement;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class TableMetaDataFactoryTest {
    
    @Test
    public void assertNewInstanceWithCreateTable() {
        CreateTableStatement createTableStatement = new CreateTableStatement();
        createTableStatement.getColumnDefinitions().add(new ColumnDefinitionSegment("id", "bigint", true));
        createTableStatement.getColumnDefinitions().add(new ColumnDefinitionSegment("status", "varchar", false));
        TableMetaData actual = TableMetaDataFactory.newInstance(createTableStatement);
        assertThat(Joiner.on(", ").join(actual.getColumns().keySet()), is("id, status"));
        assertThat(actual.getColumns().get("id"), is(new ColumnMetaData("id", "bigint", true)));
        assertThat(actual.getColumns().get("status"), is(new ColumnMetaData("status", "varchar", false)));
    }
    
    @Test
    public void assertNewInstanceWithAlterTable() {
        AlterTableStatement alterTableStatement = new AlterTableStatement();
        alterTableStatement.getAddedColumnDefinitions().add(new ColumnDefinitionSegment("new_column_1", "bigint", true));
        alterTableStatement.getAddedColumnDefinitions().add(new ColumnDefinitionSegment("new_column_2", "varchar", false));
        alterTableStatement.getModifiedColumnDefinitions().put("id", new ColumnDefinitionSegment("user_id", "bigint", true));
        alterTableStatement.getModifiedColumnDefinitions().put("status", new ColumnDefinitionSegment("status", "char", false));
        alterTableStatement.getDroppedColumnNames().add("drop_column_1");
        alterTableStatement.getDroppedColumnNames().add("drop_column_2");
        TableMetaData oldTableMetaData = new TableMetaData(Arrays.asList(new ColumnMetaData("id", "bigint", true), 
                new ColumnMetaData("status", "varchar", false), new ColumnMetaData("drop_column_1", "varchar", false), new ColumnMetaData("drop_column_2", "varchar", false)));
        TableMetaData actual = TableMetaDataFactory.newInstance(alterTableStatement, oldTableMetaData);
        assertThat(Joiner.on(", ").join(actual.getColumns().keySet()), is("user_id, status, new_column_1, new_column_2"));
        assertThat(actual.getColumns().get("user_id"), is(new ColumnMetaData("user_id", "bigint", true)));
        assertThat(actual.getColumns().get("status"), is(new ColumnMetaData("status", "char", false)));
        assertThat(actual.getColumns().get("new_column_1"), is(new ColumnMetaData("new_column_1", "bigint", true)));
        assertThat(actual.getColumns().get("new_column_2"), is(new ColumnMetaData("new_column_2", "varchar", false)));
    }
    
    @Test
    public void assertNewInstanceWithAlterTableWhenChangedPosition() {
        AlterTableStatement alterTableStatement = new AlterTableStatement();
        alterTableStatement.getAddedColumnDefinitions().add(new ColumnDefinitionSegment("new_column_1", "bigint", true));
        alterTableStatement.getAddedColumnDefinitions().add(new ColumnDefinitionSegment("new_column_2", "varchar", false));
        alterTableStatement.getModifiedColumnDefinitions().put("id", new ColumnDefinitionSegment("user_id", "bigint", true));
        alterTableStatement.getModifiedColumnDefinitions().put("status", new ColumnDefinitionSegment("status", "char", false));
        alterTableStatement.getChangedPositionColumns().add(new ColumnFirstPositionSegment("new_column_1", 1));
        alterTableStatement.getChangedPositionColumns().add(new ColumnFirstPositionSegment("user_id", 2));
        alterTableStatement.getChangedPositionColumns().add(new ColumnAfterPositionSegment("status", 3, "new_column_2"));
        TableMetaData oldTableMetaData = new TableMetaData(Arrays.asList(new ColumnMetaData("status", "varchar", false), new ColumnMetaData("id", "bigint", true)));
        TableMetaData actual = TableMetaDataFactory.newInstance(alterTableStatement, oldTableMetaData);
        assertThat(Joiner.on(", ").join(actual.getColumns().keySet()), is("user_id, new_column_1, new_column_2, status"));
        assertThat(actual.getColumns().get("user_id"), is(new ColumnMetaData("user_id", "bigint", true)));
        assertThat(actual.getColumns().get("new_column_1"), is(new ColumnMetaData("new_column_1", "bigint", true)));
        assertThat(actual.getColumns().get("new_column_2"), is(new ColumnMetaData("new_column_2", "varchar", false)));
        assertThat(actual.getColumns().get("status"), is(new ColumnMetaData("status", "char", false)));
    }
    
    @Test
    public void assertNewInstanceWithAlterTableWhenDropPrimaryKey() {
        AlterTableStatement alterTableStatement = new AlterTableStatement();
        alterTableStatement.setDropPrimaryKey(true);
        alterTableStatement.getAddedColumnDefinitions().add(new ColumnDefinitionSegment("new_column", "bigint", true));
        TableMetaData oldTableMetaData = new TableMetaData(Arrays.asList(new ColumnMetaData("id", "bigint", true), new ColumnMetaData("status", "varchar", false)));
        TableMetaData actual = TableMetaDataFactory.newInstance(alterTableStatement, oldTableMetaData);
        assertThat(Joiner.on(", ").join(actual.getColumns().keySet()), is("id, status, new_column"));
        assertThat(actual.getColumns().get("id"), is(new ColumnMetaData("id", "bigint", false)));
        assertThat(actual.getColumns().get("new_column"), is(new ColumnMetaData("new_column", "bigint", false)));
        assertThat(actual.getColumns().get("status"), is(new ColumnMetaData("status", "varchar", false)));
    }
}
