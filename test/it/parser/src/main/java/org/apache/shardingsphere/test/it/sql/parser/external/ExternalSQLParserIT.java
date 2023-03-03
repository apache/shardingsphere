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
import org.apache.shardingsphere.infra.util.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.test.it.sql.parser.external.env.SQLParserExternalITEnvironment;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.ExternalSQLParserTestParameterLoader;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.strategy.impl.GitHubTestParameterLoadStrategy;
import org.apache.shardingsphere.test.it.sql.parser.external.result.SQLParseResultReporter;
import org.apache.shardingsphere.test.it.sql.parser.external.result.SQLParseResultReporterCreator;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.net.URI;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Stream;

public abstract class ExternalSQLParserIT {
    
    @ParameterizedTest(name = "{0} ({1}) -> {2}")
    @EnabledIf("isEnabled")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    public final void assertParseSQL(final String sqlCaseId, final String databaseType, final String sql, final String reportType) {
        boolean isSuccess = true;
        SQLParseResultReporter resultReporter = TypedSPILoader.getService(SQLParseResultReporterCreator.class, reportType).create(databaseType);
        try {
            ParseASTNode parseASTNode = new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false);
            new SQLVisitorEngine(databaseType, "STATEMENT", true, new Properties()).visit(parseASTNode);
        } catch (final ShardingSphereExternalException | ClassCastException | NullPointerException | IllegalArgumentException | IndexOutOfBoundsException ignore) {
            isSuccess = false;
        }
        resultReporter.printResult(sqlCaseId, databaseType, isSuccess, sql);
    }
    
    private static boolean isEnabled() {
        return SQLParserExternalITEnvironment.getInstance().isSqlParserITEnabled();
    }
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            ExternalSQLParserITSettings settings = extensionContext.getRequiredTestClass().getAnnotation(ExternalSQLParserITSettings.class);
            Preconditions.checkNotNull(settings, "Annotation ExternalSQLParserITSettings is required.");
            return getTestParameters(settings).stream().map(each -> Arguments.of(each.getSqlCaseId(), each.getDatabaseType(), each.getSql(), each.getReportType()));
        }
        
        private Collection<ExternalSQLParserTestParameter> getTestParameters(final ExternalSQLParserITSettings settings) {
            return new ExternalSQLParserTestParameterLoader(
                    new GitHubTestParameterLoadStrategy()).load(URI.create(settings.caseURL()), URI.create(settings.resultURL()), settings.value(), settings.reportType());
        }
    }
}
