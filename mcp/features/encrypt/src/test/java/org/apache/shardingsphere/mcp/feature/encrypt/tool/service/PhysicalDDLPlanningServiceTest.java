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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.workflow.model.DDLArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.DerivedColumnPlan;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicalDDLPlanningServiceTest {
    
    private final PhysicalDDLPlanningService service = new PhysicalDDLPlanningService();
    
    @Test
    void assertPlanAddColumnArtifactsWithDefaultDefinition() {
        List<DDLArtifact> actual = service.planAddColumnArtifacts("MySQL", "orders", createDerivedColumnPlan(true, true), new LinkedHashSet<>(), "");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is(
                "ALTER TABLE orders ADD COLUMN phone_cipher VARCHAR(4000), ADD COLUMN phone_assisted_query VARCHAR(4000), ADD COLUMN phone_like_query VARCHAR(4000)"));
    }
    
    @Test
    void assertPlanAddColumnArtifactsWithExistingColumns() {
        List<DDLArtifact> actual = service.planAddColumnArtifacts("MySQL", "orders", createDerivedColumnPlan(true, false),
                new LinkedHashSet<>(List.of("phone_cipher", "phone_assisted_query")), "");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertPlanAddColumnArtifactsMatchesCaseInsensitiveExistingColumns() {
        List<DDLArtifact> actual = service.planAddColumnArtifacts("MySQL", "orders", createDerivedColumnPlan(false, false), new LinkedHashSet<>(List.of("PHONE_CIPHER")), "");
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertPlanAddColumnArtifactsKeepsCaseSensitiveExistingColumnsDistinct() {
        List<DDLArtifact> actual = service.planAddColumnArtifacts("PostgreSQL", "orders", createDerivedColumnPlan(false, false), new LinkedHashSet<>(List.of("PHONE_CIPHER")), "");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is("ALTER TABLE orders ADD COLUMN phone_cipher VARCHAR(4000)"));
    }
    
    @Test
    void assertPlanAddColumnArtifactsWithCustomDefinition() {
        List<DDLArtifact> actual = service.planAddColumnArtifacts("MySQL", "orders", createDerivedColumnPlan(false, true), new LinkedHashSet<>(), "VARCHAR(64)");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is("ALTER TABLE orders ADD COLUMN phone_cipher VARCHAR(64), ADD COLUMN phone_like_query VARCHAR(64)"));
    }
    
    @Test
    void assertPlanAddColumnArtifactsFormatsDelimitedIdentifiers() {
        List<DDLArtifact> actual = service.planAddColumnArtifacts("MySQL", "`key`", createDerivedColumnPlan(false, false), new LinkedHashSet<>(), "");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is("ALTER TABLE `key` ADD COLUMN phone_cipher VARCHAR(4000)"));
    }
    
    @Test
    void assertPlanAddColumnArtifactsFormatsSpecialCharacterIdentifiers() {
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan(false, false);
        derivedColumnPlan.setCipherColumnName("phone cipher");
        List<DDLArtifact> actual = service.planAddColumnArtifacts("MySQL", "order detail", derivedColumnPlan, new LinkedHashSet<>(), "");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is("ALTER TABLE `order detail` ADD COLUMN `phone cipher` VARCHAR(4000)"));
    }
    
    @Test
    void assertPlanAddColumnArtifactsFormatsPostgreSQLIdentifiers() {
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan(false, false);
        derivedColumnPlan.setCipherColumnName("phone cipher");
        List<DDLArtifact> actual = service.planAddColumnArtifacts("PostgreSQL", "order detail", derivedColumnPlan, new LinkedHashSet<>(), "");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is("ALTER TABLE \"order detail\" ADD COLUMN \"phone cipher\" VARCHAR(4000)"));
    }
    
    @Test
    void assertPlanAddColumnArtifactsPreservesPostgreSQLDelimitedIdentifiers() {
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan(false, false);
        derivedColumnPlan.setCipherColumnName("\"Phone_Cipher\"");
        List<DDLArtifact> actual = service.planAddColumnArtifacts("PostgreSQL", "\"Orders\"", derivedColumnPlan, new LinkedHashSet<>(), "");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getSql(), is("ALTER TABLE \"Orders\" ADD COLUMN \"Phone_Cipher\" VARCHAR(4000)"));
    }
    
    @Test
    void assertPlanAddColumnArtifactsRejectsLineTerminatorTable() {
        MCPInvalidRequestException actualException = assertThrows(MCPInvalidRequestException.class,
                () -> service.planAddColumnArtifacts("MySQL", "orders\ndrop", createDerivedColumnPlan(false, false), new LinkedHashSet<>(), ""));
        assertThat(actualException.getMessage(), is("table `orders\ndrop` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    @Test
    void assertPlanAddColumnArtifactsRejectsLineTerminatorDerivedColumn() {
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan(false, false);
        derivedColumnPlan.setCipherColumnName("phone\ncipher");
        MCPInvalidRequestException actualException = assertThrows(MCPInvalidRequestException.class,
                () -> service.planAddColumnArtifacts("MySQL", "orders", derivedColumnPlan, new LinkedHashSet<>(), ""));
        assertThat(actualException.getMessage(), is("cipher_column `phone\ncipher` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    private DerivedColumnPlan createDerivedColumnPlan(final boolean assistedQuery, final boolean likeQuery) {
        DerivedColumnPlan result = new DerivedColumnPlan();
        result.setCipherColumnRequired(true);
        result.setCipherColumnName("phone_cipher");
        result.setAssistedQueryColumnRequired(assistedQuery);
        result.setAssistedQueryColumnName("phone_assisted_query");
        result.setLikeQueryColumnRequired(likeQuery);
        result.setLikeQueryColumnName("phone_like_query");
        return result;
    }
}
