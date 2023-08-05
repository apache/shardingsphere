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

package org.apache.shardingsphere.test.it.rewrite.engine.scenario;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.test.it.rewrite.engine.SQLRewriterIT;
import org.apache.shardingsphere.test.it.rewrite.engine.SQLRewriterITSettings;
import org.apache.shardingsphere.test.it.rewrite.engine.parameter.SQLRewriteEngineTestParameters;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@SQLRewriterITSettings("scenario/sharding/case")
class ShardingSQLRewriterIT extends SQLRewriterIT {
    
    @Override
    protected YamlRootConfiguration createRootConfiguration(final SQLRewriteEngineTestParameters testParams) throws IOException {
        URL url = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(testParams.getRuleFile()), "Can not find rewrite rule yaml configuration");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
    }
    
    @Override
    protected void mockRules(final Collection<ShardingSphereRule> rules, final String schemaName, final SQLStatement sqlStatement) {
        Optional<SingleRule> singleRule = rules.stream().filter(each -> each instanceof SingleRule).map(each -> (SingleRule) each).findFirst();
        if (singleRule.isPresent() && !(sqlStatement instanceof CreateTableStatement)) {
            singleRule.get().put("db", schemaName, "t_single");
            singleRule.get().put("db", schemaName, "t_single_extend");
        }
    }
    
    @Override
    protected Map<String, ShardingSphereSchema> mockSchemas(final String schemaName) {
        Map<String, ShardingSphereTable> tables = new LinkedHashMap<>();
        tables.put("t_account", new ShardingSphereTable("t_account", Arrays.asList(
                new ShardingSphereColumn("account_id", Types.INTEGER, true, true, false, true, false, false),
                new ShardingSphereColumn("amount", Types.DECIMAL, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.TINYINT, false, false, false, false, false, false)), Collections.singletonList(new ShardingSphereIndex("status_idx_exist")),
                Collections.emptyList()));
        tables.put("t_account_detail", new ShardingSphereTable("t_account_detail", Arrays.asList(
                new ShardingSphereColumn("account_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("amount", Types.DECIMAL, false, false, false, true, false, false),
                new ShardingSphereColumn("status", Types.TINYINT, false, false, false, false, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_user", new ShardingSphereTable("t_user", Arrays.asList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("content", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_user_extend", new ShardingSphereTable("t_user_extend", Arrays.asList(
                new ShardingSphereColumn("user_id", Types.INTEGER, false, false, false, true, false, false),
                new ShardingSphereColumn("content", Types.VARCHAR, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_single", new ShardingSphereTable("t_single", Collections.singletonList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_single_extend", new ShardingSphereTable("t_single_extend", Collections.singletonList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_config", new ShardingSphereTable("t_config", Collections.singletonList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("T_ROLE", new ShardingSphereTable("T_ROLE", Collections.singletonList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("T_ROLE_ADMIN", new ShardingSphereTable("T_ROLE_ADMIN", Collections.singletonList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        tables.put("t_account_view", new ShardingSphereTable("t_account_view", Collections.singletonList(
                new ShardingSphereColumn("id", Types.INTEGER, false, false, false, true, false, false)), Collections.emptyList(), Collections.emptyList()));
        ShardingSphereSchema result = new ShardingSphereSchema(tables, Collections.emptyMap());
        return Collections.singletonMap(schemaName, result);
    }
    
    @Override
    protected void mockDataSource(final Map<String, DataSource> dataSources) {
    }
}
