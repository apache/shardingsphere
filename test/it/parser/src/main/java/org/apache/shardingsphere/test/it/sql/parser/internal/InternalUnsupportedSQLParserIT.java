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

package org.apache.shardingsphere.test.it.sql.parser.internal;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.SQLCases;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.registry.UnsupportedSQLCasesRegistry;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.sql.type.SQLCaseType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class InternalUnsupportedSQLParserIT {
    
    private static final SQLCases SQL_CASES = UnsupportedSQLCasesRegistry.getInstance().getCases();
    
    @ParameterizedTest(name = "{0} ({1}) -> {2}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertUnsupportedSQL(final String sqlCaseId, final SQLCaseType sqlCaseType, final String databaseType) {
        String sql = SQL_CASES.getSQL(sqlCaseId, sqlCaseType, Collections.emptyList());
        CacheOption cacheOption = new CacheOption(128, 1024L);
        assertThrows(SQLParsingException.class, () -> new SQLParserEngine("H2".equals(databaseType) ? "MySQL" : databaseType, cacheOption).parse(sql, false));
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            InternalSQLParserITSettings settings = context.getRequiredTestClass().getAnnotation(InternalSQLParserITSettings.class);
            Preconditions.checkNotNull(settings, "Annotation InternalSQLParserITSettings is required.");
            return getTestParameters(Arrays.asList(settings.value())).stream();
        }
        
        private Collection<Arguments> getTestParameters(final Collection<String> databaseTypes) {
            return SQL_CASES.generateTestParameters(databaseTypes).stream()
                    .map(each -> Arguments.arguments(each.getSqlCaseId(), each.getSqlCaseType(), each.getDatabaseType())).collect(Collectors.toList());
        }
    }
}
