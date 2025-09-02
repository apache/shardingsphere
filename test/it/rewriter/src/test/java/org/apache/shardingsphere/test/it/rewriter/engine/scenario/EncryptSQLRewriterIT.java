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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
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

@SQLRewriterITSettings("scenario/encrypt/case")
class EncryptSQLRewriterIT extends SQLRewriterIT {
    
    @Override
    protected Collection<ShardingSphereSchema> mockSchemas(final String schemaName) {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        tables.add(new ShardingSphereTable("t_account", Arrays.asList(
                new ShardingSphereColumn("account_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("certificate_number", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("password", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("amount", Types.DECIMAL, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.TINYINT, false, false, false, false, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_account_bak", Arrays.asList(
                new ShardingSphereColumn("account_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("certificate_number", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("password", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("amount", Types.DECIMAL, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.TINYINT, false, false, false, false, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_account_detail", Arrays.asList(
                new ShardingSphereColumn("account_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("certificate_number", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("password", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("amount", Types.DECIMAL, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.TINYINT, false, false, false, false, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_order", Arrays.asList(
                new ShardingSphereColumn("ORDER_ID", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("USER_ID", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("CONTENT", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_user", Arrays.asList(
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("user_name", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("password", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("email", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("telephone", Types.VARCHAR, false, false, false, true, false, false),
                new ShardingSphereColumn("creation_date", Types.DATE, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        return Collections.singleton(new ShardingSphereSchema(schemaName, tables, Collections.emptyList()));
    }
    
    @Override
    protected void mockDatabaseRules(final Collection<ShardingSphereRule> rules, final String schemaName, final SQLStatement sqlStatement) {
        Optional<SingleRule> singleRule = rules.stream().filter(SingleRule.class::isInstance).map(SingleRule.class::cast).findFirst();
        if (singleRule.isPresent() && !(sqlStatement instanceof CreateTableStatement)) {
            singleRule.get().getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).put("encrypt_ds", schemaName, "t_account");
            singleRule.get().getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).put("encrypt_ds", schemaName, "t_account_bak");
            singleRule.get().getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).put("encrypt_ds", schemaName, "t_account_detail");
            singleRule.get().getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).put("encrypt_ds", schemaName, "t_order");
            singleRule.get().getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).put("encrypt_ds", schemaName, "t_user");
        }
    }
}
