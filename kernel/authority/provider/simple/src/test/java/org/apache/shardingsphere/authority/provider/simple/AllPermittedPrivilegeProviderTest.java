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

package org.apache.shardingsphere.authority.provider.simple;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.config.UserConfiguration;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.spi.PrivilegeProvider;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;

class AllPermittedPrivilegeProviderTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertBuild(final String name, final PrivilegeProvider provider) {
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(
                Collections.singleton(new UserConfiguration("root", "", "%", null, false)), mock(AlgorithmConfiguration.class), Collections.emptyMap(), null);
        ShardingSpherePrivileges actual = provider.build(ruleConfig, new Grantee("root@%"));
        assertThat(actual, isA(AllPermittedPrivileges.class));
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(Arguments.of("withType", TypedSPILoader.getService(PrivilegeProvider.class, "ALL_PERMITTED")),
                    Arguments.of("withAlias", TypedSPILoader.getService(PrivilegeProvider.class, "ALL_PRIVILEGES_PERMITTED")),
                    Arguments.of("withDefault", TypedSPILoader.getService(PrivilegeProvider.class, null)));
        }
    }
}
