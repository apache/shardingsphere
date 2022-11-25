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

package org.apache.shardingsphere.test.sql.parser.internal.cases.sql;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.enums.ParameterMarkerType;
import org.apache.shardingsphere.test.sql.parser.internal.cases.sql.jaxb.SQLCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL cases.
 */
@RequiredArgsConstructor
public final class SQLCases {
    
    private static final Pattern PARAMETER_MARKER = Pattern.compile("\\?|\\$[0-9]+");
    
    private final Map<String, SQLCase> cases;
    
    /**
     * Generate test parameters.
     *
     * @param databaseTypes database types to be generated
     * @return generated test parameters
     */
    public Collection<Object[]> generateTestParameters(final Collection<String> databaseTypes) {
        Collection<Object[]> result = new LinkedList<>();
        for (SQLCase each : cases.values()) {
            result.addAll(generateTestParameters(databaseTypes, each));
        }
        return result;
    }
    
    private Collection<Object[]> generateTestParameters(final Collection<String> databaseTypes, final SQLCase sqlCase) {
        Collection<Object[]> result = new LinkedList<>();
        for (SQLCaseType each : SQLCaseType.values()) {
            result.addAll(generateTestParameters(databaseTypes, sqlCase, each));
        }
        return result;
    }
    
    private Collection<Object[]> generateTestParameters(final Collection<String> databaseTypes, final SQLCase sqlCase, final SQLCaseType caseType) {
        Collection<Object[]> result = new LinkedList<>();
        for (String each : getDatabaseTypes(sqlCase.getDatabaseTypes())) {
            if (databaseTypes.contains(each)) {
                Object[] params = new Object[3];
                params[0] = sqlCase.getId();
                params[1] = each;
                params[2] = caseType;
                result.add(params);
            }
        }
        return result;
    }
    
    private Collection<String> getDatabaseTypes(final String databaseTypes) {
        return Strings.isNullOrEmpty(databaseTypes) ? getAllDatabaseTypes() : Splitter.on(',').trimResults().splitToList(databaseTypes);
    }
    
    private Collection<String> getAllDatabaseTypes() {
        return Arrays.asList("H2", "MySQL", "PostgreSQL", "Oracle", "SQLServer", "SQL92", "openGauss");
    }
    
    /**
     * Get SQL.
     * 
     * @param caseId SQL case ID
     * @param caseType SQL case type
     * @param params parameters
     * @return got SQL
     */
    public String getSQL(final String caseId, final SQLCaseType caseType, final List<?> params) {
        Preconditions.checkState(cases.containsKey(caseId), "Can not find SQL of ID: %s", caseId);
        String sql = cases.get(caseId).getValue();
        switch (caseType) {
            case Placeholder:
                return getPlaceholderSQL(sql);
            case Literal:
                return getLiteralSQL(sql, params);
            default:
                throw new UnsupportedOperationException(caseType.name());
        }
    }
    
    private String getPlaceholderSQL(final String sql) {
        return sql;
    }
    
    private String getLiteralSQL(final String sql, final List<?> params) {
        return params.isEmpty() ? sql : replace(sql, params);
    }
    
    private String replace(final String sql, final List<?> params) {
        Matcher matcher = PARAMETER_MARKER.matcher(sql);
        int found = 0;
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group();
            if (ParameterMarkerType.QUESTION.getMarker().equals(group)) {
                appendReplacement(++found, params, matcher, result);
            } else {
                int dollarMarker = Integer.parseInt(group.replace(ParameterMarkerType.DOLLAR.getMarker(), ""));
                appendReplacement(dollarMarker, params, matcher, result);
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }
    
    private void appendReplacement(final int markerIndex, final List<?> params, final Matcher matcher, final StringBuffer buffer) {
        Preconditions.checkArgument(markerIndex <= params.size(), "Missing replacement for `%s` at index `%s`.", PARAMETER_MARKER.pattern(), markerIndex);
        matcher.appendReplacement(buffer, Matcher.quoteReplacement(params.get(markerIndex - 1).toString()));
    }
}
