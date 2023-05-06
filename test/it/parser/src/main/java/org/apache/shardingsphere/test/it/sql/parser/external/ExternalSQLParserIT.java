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
import org.apache.shardingsphere.sql.parser.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.test.it.sql.parser.external.env.SQLParserExternalITEnvironment;
import org.apache.shardingsphere.test.it.sql.parser.external.result.SQLParseResultReporter;
import org.apache.shardingsphere.test.it.sql.parser.external.result.SQLParseResultReporterCreator;
import org.apache.shardingsphere.test.loader.AbstractTestParameterLoader;
import org.apache.shardingsphere.test.loader.ExternalCaseSettings;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ExternalSQLParserIT {
    
    @ParameterizedTest(name = "{0} ({1}) -> {2}")
    @EnabledIf("isEnabled")
    @MethodSource("provideArguments")
    void assertParseSQL(final String sqlCaseId, final String databaseType, final String sql, final String reportType) throws IOException {
        boolean isSuccess = true;
        try (
                SQLParseResultReporter resultReporter = TypedSPILoader.getService(SQLParseResultReporterCreator.class, reportType)
                        .create(databaseType, SQLParserExternalITEnvironment.getInstance().getResultPath())) {
            try {
                ParseASTNode parseASTNode = new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false);
                new SQLStatementVisitorEngine(databaseType, true).visit(parseASTNode);
            } catch (final ShardingSphereExternalException | ClassCastException | NullPointerException | IllegalArgumentException | IndexOutOfBoundsException ignore) {
                isSuccess = false;
            }
            resultReporter.printResult(sqlCaseId, databaseType, isSuccess, sql);
        }
    }
    
    private static boolean isEnabled() {
        return SQLParserExternalITEnvironment.getInstance().isSqlParserITEnabled();
    }
    
    private Stream<Arguments> provideArguments() {
        ExternalCaseSettings settings = this.getClass().getAnnotation(ExternalCaseSettings.class);
        Preconditions.checkNotNull(settings, "Annotation ExternalSQLParserITSettings is required.");
        return getTestParameterLoader()
                .load(URI.create(settings.caseURL()), URI.create(settings.resultURL()), settings.value(), settings.reportType())
                .stream().map(each -> Arguments.of(each.getSqlCaseId(), each.getDatabaseType(), each.getSql(), each.getReportType()));
    }
    
    protected abstract AbstractTestParameterLoader<ExternalSQLParserTestParameter> getTestParameterLoader();
}
