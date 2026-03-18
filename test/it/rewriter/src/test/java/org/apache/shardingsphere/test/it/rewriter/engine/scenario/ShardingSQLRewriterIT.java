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

package org.apache.shardingsphere.test.it.rewriter.engine.scenario;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.test.it.rewriter.engine.SQLRewriterIT;
import org.apache.shardingsphere.test.it.rewriter.engine.SQLRewriterITSettings;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

import static org.mockito.Mockito.mock;

@SQLRewriterITSettings("scenario/sharding/case")
class ShardingSQLRewriterIT extends SQLRewriterIT {
    
    @Override
    protected Collection<ShardingSphereSchema> mockSchemas(final String schemaName) {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        tables.add(new ShardingSphereTable("t_account", Arrays.asList(
                new ShardingSphereColumn("account_id", Types.INTEGER, true, true, false, true, false, false),
                new ShardingSphereColumn("amount", Types.DECIMAL, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.TINYINT, false, false, false, false, false, false)),
                Collections.singletonList(new ShardingSphereIndex("status_idx_exist", Collections.emptyList(), false)),
                Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_account_detail", Arrays.asList(
                new ShardingSphereColumn("account_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("amount", Types.DECIMAL, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.TINYINT, false, false, false, false, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_user", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("content", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_user_extend", Arrays.asList(
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("content", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_single", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("account_id", Types.INTEGER, false, false, false, true, false, false)),
                Collections.singletonList(new ShardingSphereIndex("single_id_idx", Collections.emptyList(), false)),
                Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_single_extend", Collections.singletonList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_config", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("account_id", Types.INTEGER, false, false, false, true, false, false)),
                Collections.singletonList(new ShardingSphereIndex("broadcast_id_idx", Collections.emptyList(), false)),
                Collections.emptyList()));
        tables.add(new ShardingSphereTable("T_ROLE", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("ROLE_ID", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("T_ROLE_ADMIN", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("ROLE_ID", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_account_view", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("account_id", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_order", Arrays.asList(
                new ShardingSphereColumn("order_id", Types.BIGINT, true, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("merchant_id", Types.INTEGER, false, false, false, true, false, true),
                new ShardingSphereColumn("remark", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_order_item", Arrays.asList(
                new ShardingSphereColumn("item_id", Types.BIGINT, true, false, false, true, false, false),
                new ShardingSphereColumn("order_id", Types.BIGINT, false, false, false, true, false, false),
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_order_extend", Arrays.asList(
                new ShardingSphereColumn("extend_id", Types.BIGINT, true, false, false, true, false, false),
                new ShardingSphereColumn("order_id", Types.BIGINT, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_order_type", Arrays.asList(
                new ShardingSphereColumn("type_id", Types.INTEGER, true, false, false, true, false, false),
                new ShardingSphereColumn("type_name", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        return Collections.singleton(new ShardingSphereSchema(schemaName, mock(DatabaseType.class), tables, Collections.emptyList()));
    }
    
    @Override
    protected void mockDatabaseRules(final Collection<ShardingSphereRule> rules, final String schemaName, final SQLStatement sqlStatement) {
        Optional<SingleRule> singleRule = rules.stream().filter(SingleRule.class::isInstance).map(SingleRule.class::cast).findFirst();
        if (singleRule.isPresent() && !(sqlStatement instanceof CreateTableStatement)) {
            singleRule.get().getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).put("db", schemaName, "t_single");
            singleRule.get().getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).put("db", schemaName, "t_single_extend");
        }
    }
}
