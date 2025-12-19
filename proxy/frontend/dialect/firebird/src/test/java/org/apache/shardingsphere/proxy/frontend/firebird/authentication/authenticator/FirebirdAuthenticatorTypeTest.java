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

package org.apache.shardingsphere.proxy.frontend.firebird.authentication.authenticator;

import org.apache.shardingsphere.authentication.Authenticator;
import org.apache.shardingsphere.authentication.AuthenticatorFactory;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdAuthenticatorTypeTest {
    
    @Mock
    private AuthorityRule rule;
    
    @Mock
    private ShardingSphereUser shardingsphereUser;
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertAuthenticator(final String name, final String authenticatorType, final String expectedAuthenticatorClassName) {
        when(rule.getAuthenticatorType(any())).thenReturn(authenticatorType);
        Authenticator actual = new AuthenticatorFactory<>(FirebirdAuthenticatorType.class, rule).newInstance(shardingsphereUser);
        assertThat(actual.getClass().getSimpleName(), is(expectedAuthenticatorClassName));
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(
                    Arguments.of("default", "", "FirebirdSRPAuthenticator"),
                    Arguments.of("SRP", "SRP", "FirebirdSRPAuthenticator"),
                    Arguments.of("srp256", "SRP256", "FirebirdSRPAuthenticator"),
                    Arguments.of("legacy", "LEGACY_AUTH", "FirebirdLegacyAuthenticator"));
        }
    }
}
