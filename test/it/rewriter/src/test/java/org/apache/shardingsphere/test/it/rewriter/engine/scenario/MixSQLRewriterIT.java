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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.test.it.rewriter.engine.SQLRewriterIT;
import org.apache.shardingsphere.test.it.rewriter.engine.SQLRewriterITSettings;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

@SQLRewriterITSettings("scenario/mix/case")
class MixSQLRewriterIT extends SQLRewriterIT {
    
    @Override
    protected Collection<ShardingSphereSchema> mockSchemas(final String schemaName) {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        tables.add(new ShardingSphereTable("t_account", Arrays.asList(
                new ShardingSphereColumn("account_id", Types.INTEGER, true, true, "int", false, true, false, false),
                new ShardingSphereColumn("password", Types.VARCHAR, false, false, "varchar", false, true, false, false),
                new ShardingSphereColumn("amount", Types.DECIMAL, false, false, "decimal", false, true, false, false),
                new ShardingSphereColumn("status", Types.TINYINT, false, false, "tinyint", false, false, false, false)),
                Collections.singletonList(new ShardingSphereIndex("index_name", Collections.emptyList(), false)), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_account_bak", Arrays.asList(
                new ShardingSphereColumn("account_id", Types.INTEGER, true, true, "int", false, true, false, false),
                new ShardingSphereColumn("password", Types.VARCHAR, false, false, "varchar", false, true, false, false),
                new ShardingSphereColumn("amount", Types.DECIMAL, false, false, "decimal", false, true, false, false),
                new ShardingSphereColumn("status", Types.TINYINT, false, false, "tinyint", false, false, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_account_detail", Arrays.asList(
                new ShardingSphereColumn("account_id", Types.INTEGER, false, false, "int", false, true, false, false),
                new ShardingSphereColumn("password", Types.VARCHAR, false, false, "varchar", false, true, false, false),
                new ShardingSphereColumn("amount", Types.DECIMAL, false, false, "decimal", false, true, false, false),
                new ShardingSphereColumn("status", Types.TINYINT, false, false, "tinyint", false, false, false, false)), Collections.emptyList(), Collections.emptyList()));
        return Collections.singleton(new ShardingSphereSchema(schemaName, tables, Collections.emptyList()));
    }
    
    @Override
    protected void mockDatabaseRules(final Collection<ShardingSphereRule> rules, final String schemaName, final SQLStatement sqlStatement) {
    }
}
