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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateViewStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateViewStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.mock;

public final class CreateViewStatementSchemaRefresherTest {
    
    @Test
    public void refreshWithUnConfiguredForMySQL() throws SQLException {
        refreshWithUnConfigured(new MySQLCreateViewStatement());
    }
    
    @Test
    public void refreshWithUnConfiguredForPostgreSQL() throws SQLException {
        refreshWithUnConfigured(new PostgreSQLCreateViewStatement());
    }
    
    private void refreshWithUnConfigured(final CreateViewStatement createViewStatement) throws SQLException {
        createViewStatement.setView(new SimpleTableSegment(new TableNameSegment(1, 3, new IdentifierValue("t_order_item_0"))));
        SchemaRefresher<CreateViewStatement> schemaRefresher = new CreateViewStatementSchemaRefresher();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("", mock(ShardingSphereResource.class), mock(ShardingSphereRuleMetaData.class), ShardingSphereSchemaBuildUtil.buildSchema());
        schemaRefresher.refresh(metaData, Collections.singletonList("t_order_item"), createViewStatement, new ConfigurationProperties(new Properties()));
    }
}
