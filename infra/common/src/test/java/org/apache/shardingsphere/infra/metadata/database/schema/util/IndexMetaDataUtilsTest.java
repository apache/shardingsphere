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

package org.apache.shardingsphere.infra.metadata.database.schema.util;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class IndexMetaDataUtilsTest {
    
    private final DatabaseType fixtureDatabaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final DatabaseType postgreSQLDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DatabaseType oracleDatabaseType = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    @Test
    void assertGetLogicIndexNameWithIndexNameSuffix() {
        assertThat(IndexMetaDataUtils.getLogicIndexName("order_index_t_order", "t_order"), is("order_index"));
    }
    
    @Test
    void assertGetLogicIndexNameWithMultiIndexNameSuffix() {
        assertThat(IndexMetaDataUtils.getLogicIndexName("order_t_order_index_t_order", "t_order"), is("order_t_order_index"));
    }
    
    @Test
    void assertGetLogicIndexNameWithoutIndexNameSuffix() {
        assertThat(IndexMetaDataUtils.getLogicIndexName("order_index", "t_order"), is("order_index"));
    }
    
    @Test
    void assertGetActualIndexNameWithActualTableName() {
        assertThat(IndexMetaDataUtils.getActualIndexName("order_index", "t_order"), is("order_index_t_order"));
    }
    
    @Test
    void assertGetShortenedActualIndexNameReducesLengthForLongActualTableName() {
        String actualTableName = "order_detail_archive_20260316_0001";
        assertThat(IndexMetaDataUtils.getShortenedActualIndexName("order_index", actualTableName).length(),
                lessThan(IndexMetaDataUtils.getLegacyActualIndexName("order_index", actualTableName).length()));
    }
    
    @Test
    void assertGetLegacyActualIndexNameWithActualTableName() {
        assertThat(IndexMetaDataUtils.getLegacyActualIndexName("order_index", "t_order"), is("order_index_t_order"));
    }
    
    @Test
    void assertGetActualIndexNameWithoutActualTableName() {
        assertThat(IndexMetaDataUtils.getActualIndexName("order_index", null), is("order_index"));
    }
    
    @Test
    void assertGetGeneratedLogicIndexNameWithShortenedIndexNameSuffix() {
        String actualIndexName = IndexMetaDataUtils.getShortenedActualIndexName("order_index", "t_order");
        assertThat(IndexMetaDataUtils.getGeneratedLogicIndexName(actualIndexName, "t_order"), is("order_index"));
    }
    
    @Test
    void assertGetGeneratedLogicIndexNameWithLegacyIndexNameSuffix() {
        assertThat(IndexMetaDataUtils.getGeneratedLogicIndexName("order_index_t_order", "t_order"), is("order_index"));
    }
    
    @Test
    void assertGetActualIndexNameKeepsLegacyFormatWhenLegacyNameFitsWithinBudget() {
        assertThat(IndexMetaDataUtils.getActualIndexName("status_idx", "t_account_0", postgreSQLDatabaseType), is("status_idx_t_account_0"));
    }
    
    @Test
    void assertGetActualIndexNameUsesHashedSuffixWhenLegacyNameExceedsBudget() {
        String logicIndexName = "named_index_boundary_case_abcdefghijklmnopqrstuvwxyz";
        String actual = IndexMetaDataUtils.getActualIndexName(logicIndexName, "t_account_0", postgreSQLDatabaseType);
        assertThat(actual, startsWith(logicIndexName));
        assertTrue(actual.matches(".*_h[0-9a-z]{8}$"));
        assertThat(IndexMetaDataUtils.getGeneratedLogicIndexName(actual, "t_account_0"), is(logicIndexName));
        assertThat(actual.getBytes(StandardCharsets.UTF_8).length, is(62));
    }
    
    @Test
    void assertGetActualIndexNameTruncatesPrefixWhenHashedNameStillExceedsBudget() {
        String logicIndexName = "very_long_named_index_boundary_case_for_sharding_length_safety_validation";
        String actual = IndexMetaDataUtils.getActualIndexName(logicIndexName, "t_account_0", postgreSQLDatabaseType);
        assertTrue(actual.matches(".*_t[0-9a-z]{8}$"));
        assertThat(actual.getBytes(StandardCharsets.UTF_8).length, is(63));
        assertTrue(IndexMetaDataUtils.isGeneratedActualIndexNameMatch(actual, logicIndexName, "t_account_0"));
    }
    
    @Test
    void assertGetActualIndexNameUsesDialectSpecificBudget() {
        String logicIndexName = "oracle_length_budget";
        String actual = IndexMetaDataUtils.getActualIndexName(logicIndexName, "t_account_0", oracleDatabaseType);
        assertTrue(actual.matches(".*_h[0-9a-z]{8}$"));
        assertThat(actual.getBytes(StandardCharsets.UTF_8).length, is(30));
    }
    
    @Test
    void assertFindGeneratedLogicIndexNameWithTruncatedActualIndexName() {
        String logicIndexName = "very_long_named_index_boundary_case_for_sharding_length_safety_validation";
        String actualIndexName = IndexMetaDataUtils.getActualIndexName(logicIndexName, "t_account_0", postgreSQLDatabaseType);
        assertThat(IndexMetaDataUtils.findGeneratedLogicIndexName(actualIndexName, "t_account_0", Collections.singleton(logicIndexName)).orElse(""),
                is(logicIndexName));
    }
    
    @Test
    void assertFindGeneratedLogicIndexNameKeepsExactCandidateWithHashLikeSuffix() {
        assertThat(IndexMetaDataUtils.findGeneratedLogicIndexName("foo_h12345678", "t_account_0", Collections.singleton("foo_h12345678")).orElse(""),
                is("foo_h12345678"));
    }
    
    @Test
    void assertFindGeneratedLogicIndexNameKeepsExactCandidateWithTruncationLikeSuffix() {
        assertThat(IndexMetaDataUtils.findGeneratedLogicIndexName("foo_t12345678", "t_account_0", Collections.singleton("foo_t12345678")).orElse(""),
                is("foo_t12345678"));
    }
    
    @Test
    void assertFindGeneratedLogicIndexNameKeepsUnprovableHashLikeSuffixWithoutCandidate() {
        assertThat(IndexMetaDataUtils.findGeneratedLogicIndexName("foo_h12345678", "t_account_0", Collections.emptyList()).orElse(""),
                is("foo_h12345678"));
    }
    
    @Test
    void assertFindGeneratedLogicIndexNameKeepsUnprovableTruncationLikeSuffixWithoutCandidate() {
        assertThat(IndexMetaDataUtils.findGeneratedLogicIndexName("foo_t12345678", "t_account_0", Collections.emptyList()).orElse(""),
                is("foo_t12345678"));
    }
    
    @Test
    void assertGetTableNames() {
        IndexSegment indexSegment = new IndexSegment(0, 0, new IndexNameSegment(0, 0, new IdentifierValue("foo_idx")));
        Collection<QualifiedTable> actual = IndexMetaDataUtils.getTableNames(buildDatabase(), fixtureDatabaseType, Collections.singleton(indexSegment));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getSchemaName(), is("foo_db"));
        assertThat(actual.iterator().next().getTableName(), is("foo_tbl"));
    }
    
    private ShardingSphereDatabase buildDatabase() {
        ShardingSphereTable table = new ShardingSphereTable(
                "foo_tbl", Collections.emptyList(), Collections.singleton(new ShardingSphereIndex("foo_idx", Collections.emptyList(), false)), Collections.emptyList());
        Collection<ShardingSphereSchema> schemas = Collections.singleton(new ShardingSphereSchema("foo_db", mock(DatabaseType.class), Collections.singleton(table), Collections.emptyList()));
        return new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), mock(ResourceMetaData.class), mock(RuleMetaData.class), schemas, new ConfigurationProperties(new Properties()));
    }
}
