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

package org.apache.shardingsphere.test.it.sql.parser.external;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.test.it.sql.parser.external.env.SQLParserExternalITEnvironment;
import org.apache.shardingsphere.test.it.sql.parser.external.result.SQLParseResultReporter;
import org.apache.shardingsphere.test.it.sql.parser.external.result.SQLParseResultReporterCreator;
import org.apache.shardingsphere.test.loader.AbstractTestParameterLoader;
import org.apache.shardingsphere.test.loader.ExternalCaseSettings;
import org.apache.shardingsphere.test.loader.ExternalSQLParserTestParameter;
import org.apache.shardingsphere.test.loader.strategy.TestParameterLoadStrategy;
import org.apache.shardingsphere.test.loader.strategy.impl.GitHubTestParameterLoadStrategy;
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

public abstract class ExternalSQLParserIT {
    
    @ParameterizedTest(name = "{0} ({1}) -> {2}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertParseSQL(final String sqlCaseId, final String databaseType, final String sql, final String reportType) throws IOException {
        boolean isSuccess = false;
        try (
                SQLParseResultReporter resultReporter = TypedSPILoader.getService(SQLParseResultReporterCreator.class, reportType)
                        .create(databaseType, SQLParserExternalITEnvironment.getInstance().getResultPath())) {
            try {
                ParseASTNode parseASTNode = new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false);
                new SQLStatementVisitorEngine(databaseType, true).visit(parseASTNode);
                isSuccess = true;
            } finally {
                resultReporter.printResult(sqlCaseId, databaseType, isSuccess, sql);
            }
        }
    }
    
    private static boolean isEnabled() {
        return SQLParserExternalITEnvironment.getInstance().isSqlParserITEnabled();
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            ExternalCaseSettings settings = extensionContext.getRequiredTestClass().getAnnotation(ExternalCaseSettings.class);
            Preconditions.checkNotNull(settings, "Annotation ExternalSQLParserITSettings is required.");
            return getTestParameters(settings).stream().map(each -> Arguments.of(each.getSqlCaseId(), each.getDatabaseType(), each.getSql(), each.getReportType()));
        }
        
        @SneakyThrows
        private Collection<ExternalSQLParserTestParameter> getTestParameters(final ExternalCaseSettings settings) {
            AbstractTestParameterLoader<ExternalSQLParserTestParameter> loader = settings.caseLoader().getConstructor(TestParameterLoadStrategy.class)
                    .newInstance(new GitHubTestParameterLoadStrategy());
            return loader.load(URI.create(settings.caseURL()), URI.create(settings.resultURL()), settings.value(), settings.reportType());
        }
    }
}
