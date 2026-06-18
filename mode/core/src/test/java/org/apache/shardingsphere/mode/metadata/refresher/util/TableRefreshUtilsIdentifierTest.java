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

package org.apache.shardingsphere.mode.metadata.refresher.util;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TableRefreshUtilsIdentifierTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetActualTableNameUsesExistingTableName() {
        assertThat(TableRefreshUtils.getActualTableName(createDatabase(), "foo_schema", new IdentifierValue("foo_tbl"),
                new ConfigurationProperties(new Properties())), is("Foo_Tbl"));
    }
    
    @Test
    void assertGetActualTableNamesUsesExistingTableNames() {
        assertThat(TableRefreshUtils.getActualTableNames(createDatabase(), "foo_schema", Arrays.asList(new IdentifierValue("foo_tbl"), new IdentifierValue("bar_tbl")),
                new ConfigurationProperties(new Properties())), is(Arrays.asList("Foo_Tbl", "Bar_Tbl")));
    }
    
    @Test
    void assertGetActualViewNamesUsesExistingViewNames() {
        assertThat(TableRefreshUtils.getActualViewNames(createDatabase(), "foo_schema", Arrays.asList(new IdentifierValue("foo_view"), new IdentifierValue("bar_view")),
                new ConfigurationProperties(new Properties())), is(Arrays.asList("Foo_View", "Bar_View")));
    }
    
    @Test
    void assertGetActualViewNameUsesExistingViewName() {
        assertThat(TableRefreshUtils.getActualViewName(createDatabase(), "foo_schema", new IdentifierValue("foo_view"),
                new ConfigurationProperties(new Properties())), is("Foo_View"));
    }
    
    @Test
    void assertGetActualIndexNameUsesExistingIndexName() {
        assertThat(TableRefreshUtils.getActualIndexName(createDatabase(), "foo_schema", "foo_tbl", new IdentifierValue("idx_foo"),
                new ConfigurationProperties(new Properties())), is("Idx_Foo"));
    }
    
    @Test
    void assertGetActualColumnNamesUsesExistingColumnNames() {
        assertThat(TableRefreshUtils.getActualColumnNames(createDatabase(), "foo_schema", "foo_tbl",
                Arrays.asList(new IdentifierValue("order_id"), new IdentifierValue("user_id")), new ConfigurationProperties(new Properties())),
                is(Arrays.asList("Order_ID", "User_ID")));
    }
    
    @Test
    void assertFindActualTableNameByIndexUsesExistingIndexName() {
        assertThat(TableRefreshUtils.findActualTableNameByIndex(createDatabase(), "foo_schema", new IdentifierValue("idx_foo"),
                new ConfigurationProperties(new Properties())).get(), is("Foo_Tbl"));
    }
    
    @Test
    void assertGetActualTableNameWithSensitiveProps() {
        Properties props = new Properties();
        props.setProperty("metadata-identifier-case-sensitivity", "SENSITIVE");
        assertThat(TableRefreshUtils.getActualTableName(createDatabase(), "Foo_Schema", new IdentifierValue("Foo_Tbl", QuoteCharacter.QUOTE),
                new ConfigurationProperties(props)), is("Foo_Tbl"));
    }
    
    @Test
    void assertGetTableLoadCandidateNameUsesNormalizedRule() {
        assertThat(TableRefreshUtils.getTableLoadCandidateName(createDatabase(), new IdentifierValue("Foo_Tbl"),
                new ConfigurationProperties(new Properties())), is("foo_tbl"));
    }
    
    @Test
    void assertGetTableLoadCandidateNameUsesSensitiveRule() {
        Properties props = new Properties();
        props.setProperty("metadata-identifier-case-sensitivity", "SENSITIVE");
        assertThat(TableRefreshUtils.getTableLoadCandidateName(createDatabase(), new IdentifierValue("Foo_Tbl"),
                new ConfigurationProperties(props)), is("foo_tbl"));
    }
    
    @Test
    void assertGetViewLoadCandidateNameUsesNormalizedRule() {
        assertThat(TableRefreshUtils.getViewLoadCandidateName(createDatabase(), new IdentifierValue("Foo_View"),
                new ConfigurationProperties(new Properties())), is("foo_view"));
    }
    
    @Test
    void assertGetViewLoadCandidateNameUsesSensitiveRule() {
        Properties props = new Properties();
        props.setProperty("metadata-identifier-case-sensitivity", "SENSITIVE");
        assertThat(TableRefreshUtils.getViewLoadCandidateName(createDatabase(), new IdentifierValue("Foo_View"),
                new ConfigurationProperties(props)), is("foo_view"));
    }
    
    private ShardingSphereDatabase createDatabase() {
        return createDatabase(databaseType);
    }
    
    private ShardingSphereDatabase createDatabase(final DatabaseType databaseType) {
        ShardingSphereSchema schema = new ShardingSphereSchema("Foo_Schema", databaseType,
                Arrays.asList(new ShardingSphereTable("Foo_Tbl",
                        Arrays.asList(new ShardingSphereColumn("Order_ID", 0, false, false, false, true, false, true),
                                new ShardingSphereColumn("User_ID", 0, false, false, false, true, false, true)),
                        Collections.singletonList(new ShardingSphereIndex("Idx_Foo", Collections.singletonList("Order_ID"), false)),
                        Collections.emptyList()),
                        new ShardingSphereTable("Bar_Tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())),
                Arrays.asList(new ShardingSphereView("Foo_View", "SELECT 1"), new ShardingSphereView("Bar_View", "SELECT 1")));
        return new ShardingSphereDatabase("foo_db", databaseType, new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), Collections.singletonList(schema), new ConfigurationProperties(new Properties()));
    }
}
