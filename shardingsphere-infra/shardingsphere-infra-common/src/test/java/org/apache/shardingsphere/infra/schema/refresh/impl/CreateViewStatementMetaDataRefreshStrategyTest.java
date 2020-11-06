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

package org.apache.shardingsphere.infra.schema.refresh.impl;

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.schema.refresh.AbstractMetaDataRefreshStrategyTest;
import org.apache.shardingsphere.infra.schema.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateViewStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

public final class CreateViewStatementMetaDataRefreshStrategyTest extends AbstractMetaDataRefreshStrategyTest {
    
    @Test
    public void refreshMetaDataWithUnConfiguredForMySQL() throws SQLException {
        refreshMetaDataWithUnConfigured(new MySQLCreateViewStatement());
    }
    
    @Test
    public void refreshMetaDataWithUnConfiguredForPostgreSQL() throws SQLException {
        refreshMetaDataWithUnConfigured(new PostgreSQLCreateViewStatement());
    }
    
    private void refreshMetaDataWithUnConfigured(final CreateViewStatement createViewStatement) throws SQLException {
        createViewStatement.setView(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_item_0"))));
        MetaDataRefreshStrategy<CreateViewStatement> metaDataRefreshStrategy = new CreateViewStatementMetaDataRefreshStrategy();
        metaDataRefreshStrategy.refreshMetaData(getSchema(), new MySQLDatabaseType(), Collections.singletonList("t_order_item"), createViewStatement, tableName -> Optional.empty());
    }
}
