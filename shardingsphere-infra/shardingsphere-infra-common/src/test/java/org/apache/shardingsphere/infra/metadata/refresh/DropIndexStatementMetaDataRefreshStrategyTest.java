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

package org.apache.shardingsphere.infra.metadata.refresh;

import org.apache.shardingsphere.infra.metadata.database.model.index.IndexMetaData;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.refresh.impl.DropIndexStatementMetaDataRefreshStrategy;
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
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

public final class DropIndexStatementMetaDataRefreshStrategyTest extends AbstractMetaDataRefreshStrategyTest {
    
    @Test
    public void refreshMetaDataForMySQL() throws SQLException {
        MySQLDropIndexStatement dropIndexStatement = new MySQLDropIndexStatement();
        dropIndexStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        refreshMetaData(dropIndexStatement);
    }
    
    @Test
    public void refreshMetaDataForSQLServer() throws SQLException {
        SQLServerDropIndexStatement dropIndexStatement = new SQLServerDropIndexStatement();
        dropIndexStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        refreshMetaData(dropIndexStatement);
    }
    
    private void refreshMetaData(final DropIndexStatement dropIndexStatement) throws SQLException {
        dropIndexStatement.getIndexes().add(new IndexSegment(1, 2, new IdentifierValue("index")));
        MetaDataRefreshStrategy<DropIndexStatement> metaDataRefreshStrategy = new DropIndexStatementMetaDataRefreshStrategy();
        metaDataRefreshStrategy.refreshMetaData(getMetaData(), mock(DatabaseType.class), Collections.emptyMap(), dropIndexStatement, tableName -> Optional.empty());
        assertFalse(getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData().get("t_order").getIndexes().containsKey("index"));
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
        dropIndexStatement.getIndexes().add(new IndexSegment(1, 2, new IdentifierValue("index")));
        dropIndexStatement.getIndexes().add(new IndexSegment(2, 3, new IdentifierValue("t_order_index")));
        dropIndexStatement.getIndexes().add(new IndexSegment(3, 4, new IdentifierValue("order_id_index")));
        Map<String, IndexMetaData> actualIndex = getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData().get("t_order").getIndexes();
        actualIndex.put("t_order_index", new IndexMetaData("t_order_index"));
        actualIndex.put("order_id_index", new IndexMetaData("order_id_index"));
        MetaDataRefreshStrategy<DropIndexStatement> metaDataRefreshStrategy = new DropIndexStatementMetaDataRefreshStrategy();
        metaDataRefreshStrategy.refreshMetaData(getMetaData(), mock(DatabaseType.class), Collections.emptyMap(), dropIndexStatement, tableName -> Optional.empty());
        assertFalse(getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData().get("t_order").getIndexes().containsKey("index"));
        assertFalse(getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData().get("t_order").getIndexes().containsKey("t_order_index"));
        assertFalse(getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData().get("t_order").getIndexes().containsKey("order_id_index"));
    }
}
