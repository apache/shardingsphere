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

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.refresh.impl.AlterTableStatementMetaDataRefreshStrategy;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.ddl.AlterTableStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class AlterTableStatementMetaDataRefreshStrategyTest extends AbstractMetaDataRefreshStrategyTest {
    
    @Test
    public void refreshMetaData() throws SQLException {
        MetaDataRefreshStrategy<AlterTableStatementContext> metaDataRefreshStrategy = new AlterTableStatementMetaDataRefreshStrategy();
        AlterTableStatement alterTableStatement = new AlterTableStatement(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order"))));
        AlterTableStatementContext alterTableStatementContext = new AlterTableStatementContext(alterTableStatement);
        metaDataRefreshStrategy.refreshMetaData(getMetaData(), mock(DatabaseType.class), Collections.emptyMap(), alterTableStatementContext, tableName -> Optional.of(new TableMetaData(
                Collections.singletonList(new ColumnMetaData("order_id", 1, "String", true, false, false)),
                Collections.singletonList(new IndexMetaData("index_alter")))));
        assertTrue(getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData().get("t_order").getIndexes().containsKey("index_alter"));
    }
}
