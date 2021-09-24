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

package org.apache.shardingsphere.infra.optimize.core.metadata.refresher;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.optimize.metadata.refresher.FederationMetaDataRefresher;
import org.apache.shardingsphere.infra.optimize.metadata.refresher.type.AlterTableFederationMetaDataRefresher;
import org.apache.shardingsphere.infra.optimize.core.metadata.rule.CommonFixtureRule;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerAlterTableStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterTableFederationMetaDataRefresherTest {
    
    @Mock
    private SchemaBuilderMaterials materials;
    
    @Test
    public void refreshTable() throws SQLException {
        refreshTable(new MySQLAlterTableStatement());
        refreshTable(new OracleAlterTableStatement());
        refreshTable(new PostgreSQLAlterTableStatement());
        refreshTable(new SQLServerAlterTableStatement());
        refreshTable(new SQL92AlterTableStatement());
    }
    
    private void refreshTable(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        TableContainedRule rule = mock(TableContainedRule.class);
        when(materials.getRules()).thenReturn(Collections.singletonList(rule));
        FederationMetaDataRefresher<AlterTableStatement> federationMetaDataRefresher = new AlterTableFederationMetaDataRefresher();
        FederationSchemaMetaData schema = buildSchema();
        federationMetaDataRefresher.refresh(schema, Collections.singletonList("ds"), alterTableStatement, materials);
        assertTrue(schema.getTables().containsKey("t_order"));
    }
    
    @Test
    public void renameTable() throws SQLException {
        renameTable(new MySQLAlterTableStatement());
        renameTable(new OracleAlterTableStatement());
        renameTable(new PostgreSQLAlterTableStatement());
        renameTable(new SQLServerAlterTableStatement());
        renameTable(new SQL92AlterTableStatement());
    }
    
    private void renameTable(final AlterTableStatement alterTableStatement) throws SQLException {
        alterTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        alterTableStatement.setRenameTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_new"))));
        when(materials.getRules()).thenReturn(Collections.singletonList(new CommonFixtureRule()));
        when(materials.getDataSourceMap()).thenReturn(Collections.singletonMap("ds", mock(DataSource.class)));
        FederationMetaDataRefresher<AlterTableStatement> federationMetaDataRefresher = new AlterTableFederationMetaDataRefresher();
        FederationSchemaMetaData schema = buildSchema();
        federationMetaDataRefresher.refresh(schema, Collections.singletonList("ds"), alterTableStatement, materials);
        assertFalse(schema.getTables().containsKey("t_order"));
        assertTrue(schema.getTables().containsKey("t_order_new"));
    }
    
    private FederationSchemaMetaData buildSchema() {
        Map<String, TableMetaData> metaData = ImmutableMap.of("t_order", new TableMetaData("t_order", Collections.singletonList(new ColumnMetaData("order_id", 1, false, false, false)),
                        Collections.singletonList(new IndexMetaData("index"))));
        return new FederationSchemaMetaData("t_order", metaData);
    }
}
