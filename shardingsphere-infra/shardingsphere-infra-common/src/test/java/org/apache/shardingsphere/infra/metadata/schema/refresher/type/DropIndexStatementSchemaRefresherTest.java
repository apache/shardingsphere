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

package org.apache.shardingsphere.infra.metadata.schema.refresher.type;

import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLDropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerDropIndexStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public final class DropIndexStatementSchemaRefresherTest {
    
    @Test
    public void refreshForMySQL() throws SQLException {
        MySQLDropIndexStatement dropIndexStatement = new MySQLDropIndexStatement();
        dropIndexStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        refresh(dropIndexStatement);
    }
    
    @Test
    public void refreshForSQLServer() throws SQLException {
        SQLServerDropIndexStatement dropIndexStatement = new SQLServerDropIndexStatement();
        dropIndexStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        refresh(dropIndexStatement);
    }
    
    private void refresh(final DropIndexStatement dropIndexStatement) throws SQLException {
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        dropIndexStatement.getIndexes().add(new IndexSegment(1, 2, new IdentifierValue("index")));
        SchemaRefresher<DropIndexStatement> metaDataRefreshStrategy = new DropIndexStatementSchemaRefresher();
        metaDataRefreshStrategy.refresh(schema, Collections.emptyList(), dropIndexStatement, mock(SchemaBuilderMaterials.class));
        assertFalse(schema.get("t_order").getIndexes().containsKey("index"));
    }
    
    @Test
    public void assertRemoveIndexesForMySQL() throws SQLException {
        MySQLDropIndexStatement dropIndexStatement = new MySQLDropIndexStatement();
        dropIndexStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        assertRemoveIndexes(dropIndexStatement);
    }
    
    @Test
    public void assertRemoveIndexesForSQLServer() throws SQLException {
        SQLServerDropIndexStatement dropIndexStatement = new SQLServerDropIndexStatement();
        dropIndexStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        assertRemoveIndexes(dropIndexStatement);
    }
    
    private void assertRemoveIndexes(final DropIndexStatement dropIndexStatement) throws SQLException {
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        dropIndexStatement.getIndexes().add(new IndexSegment(1, 2, new IdentifierValue("index")));
        dropIndexStatement.getIndexes().add(new IndexSegment(2, 3, new IdentifierValue("t_order_index")));
        dropIndexStatement.getIndexes().add(new IndexSegment(3, 4, new IdentifierValue("order_id_index")));
        Map<String, IndexMetaData> actualIndex = schema.get("t_order").getIndexes();
        actualIndex.put("t_order_index", new IndexMetaData("t_order_index"));
        actualIndex.put("order_id_index", new IndexMetaData("order_id_index"));
        SchemaRefresher<DropIndexStatement> schemaRefresher = new DropIndexStatementSchemaRefresher();
        schemaRefresher.refresh(schema, Collections.emptyList(), dropIndexStatement, mock(SchemaBuilderMaterials.class));
        assertFalse(schema.get("t_order").getIndexes().containsKey("index"));
        assertFalse(schema.get("t_order").getIndexes().containsKey("t_order_index"));
        assertFalse(schema.get("t_order").getIndexes().containsKey("order_id_index"));
    }
}
