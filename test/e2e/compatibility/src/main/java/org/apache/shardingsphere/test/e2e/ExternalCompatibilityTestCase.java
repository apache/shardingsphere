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

package org.apache.shardingsphere.test.e2e;

import com.google.common.base.Preconditions;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.stream.Stream;

public abstract class ExternalCompatibilityTestCase {
    
    @ParameterizedTest(name = "{0} ({1}) -> {2}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertTest(final String sqlCaseId, final String databaseType, final String sql, final String expectResult) throws IOException {
        
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            ExternalCompatibilitySettings settings = extensionContext.getRequiredTestClass().getAnnotation(ExternalCompatibilitySettings.class);
            Preconditions.checkNotNull(settings, "Annotation ExternalSQLParserITSettings is required.");
            return getTestParameters(settings).stream().map(each -> Arguments.of(each.getSqlCaseId(), each.getDatabaseType(), each.getSql(), each.getReportType()));
        }
        
        private Collection<ExternalCompatibilityTestParameter> getTestParameters(final ExternalCompatibilitySettings settings) {
            return new ExternalCompatibilityTestParameterLoader(
                    new GitHubTestParameterLoadStrategy()).load(URI.create(settings.caseURL()), URI.create(settings.resultURL()), settings.value(), settings.reportType());
        }
    }
}
