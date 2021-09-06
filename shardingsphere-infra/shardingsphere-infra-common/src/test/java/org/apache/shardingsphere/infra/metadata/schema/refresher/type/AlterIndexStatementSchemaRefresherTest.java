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
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLAlterIndexStatement;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class AlterIndexStatementSchemaRefresherTest {
    
    @Test
    public void refreshAlterIndexStatementWithRenameIndex() throws SQLException {
        PostgreSQLAlterIndexStatement alterIndexStatement = new PostgreSQLAlterIndexStatement();
        ShardingSphereSchema schema = ShardingSphereSchemaBuildUtil.buildSchema();
        SchemaRefresher<AlterIndexStatement> schemaRefresher = new AlterIndexStatementSchemaRefresher();
        alterIndexStatement.setIndex(new IndexSegment(1, 2, new IdentifierValue("index")));
        alterIndexStatement.setRenameIndex(new IndexSegment(1, 2, new IdentifierValue("index_new")));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("", mock(ShardingSphereResource.class), mock(ShardingSphereRuleMetaData.class), schema);
        schemaRefresher.refresh(metaData, Collections.emptyList(), alterIndexStatement, new ConfigurationProperties(new Properties()));
        assertFalse(schema.get("t_order").getIndexes().containsKey("index"));
        assertTrue(schema.get("t_order").getIndexes().containsKey("index_new"));
    }
}
