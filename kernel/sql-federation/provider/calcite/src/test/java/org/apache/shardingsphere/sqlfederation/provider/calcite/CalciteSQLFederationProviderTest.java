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

package org.apache.shardingsphere.sqlfederation.provider.calcite;

import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.compiler.context.CompilerContext;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.exception.SQLFederationProviderNotFoundException;
import org.apache.shardingsphere.sqlfederation.provider.none.NoneSQLFederationProvider;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationProvider;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalciteSQLFederationProviderTest {
    
    @Test
    void assertLoadCalciteProvider() {
        assertThat(TypedSPILoader.getService(SQLFederationProvider.class, "CALCITE"), isA(CalciteSQLFederationProvider.class));
    }
    
    @Test
    void assertDefaultProviderAndSharedContextRefresh() {
        SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(4, 64L));
        SQLFederationRule rule = new SQLFederationRule(config, Collections.emptyList());
        SQLFederationProvider provider = rule.getProvider();
        assertThat(provider, isA(CalciteSQLFederationProvider.class));
        CompilerContext beforeRefresh = ((CalciteSQLFederationProvider) provider).getCompilerContext();
        rule.refresh(Collections.emptyList(), GlobalRuleChangedType.DATABASE_CHANGED);
        assertThat(rule.getProvider(), sameInstance(provider));
        assertThat(((CalciteSQLFederationProvider) provider).getCompilerContext(), not(sameInstance(beforeRefresh)));
    }
    
    @Test
    void assertSelectNoneWhenBothProvidersInstalled() {
        SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(4, 64L), "NONE");
        assertThat(new SQLFederationRule(config, Collections.emptyList()).getProvider(), isA(NoneSQLFederationProvider.class));
    }
    
    @Test
    void assertUnknownProviderDoesNotFallBackWhenBothProvidersInstalled() {
        SQLFederationRuleConfiguration config = new SQLFederationRuleConfiguration(true, false, new SQLFederationCacheOption(4, 64L), "UNKNOWN");
        assertThrows(SQLFederationProviderNotFoundException.class, () -> new SQLFederationRule(config, Collections.emptyList()));
    }
}
