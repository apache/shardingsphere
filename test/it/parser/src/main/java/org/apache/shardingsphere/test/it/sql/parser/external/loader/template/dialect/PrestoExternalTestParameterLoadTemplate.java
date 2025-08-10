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

package org.apache.shardingsphere.test.it.sql.parser.external.loader.template.dialect;

import org.apache.shardingsphere.test.it.sql.parser.external.ExternalSQLTestParameter;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.template.ExternalTestParameterLoadTemplate;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.template.type.OneCasePerFileExternalTestParameterLoadTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * External test parameter load template for Presto.
 */
public final class PrestoExternalTestParameterLoadTemplate implements ExternalTestParameterLoadTemplate {
    
    private static final String REGEX = "\\$\\{mutableTables.hive.datatype}";
    
    @Override
    public Collection<ExternalSQLTestParameter> load(final String sqlCaseFileName, final List<String> sqlCaseFileContent, final List<String> resultFileContent,
                                                     final String databaseType, final String reportType) {
        if (!sqlCaseFileName.toLowerCase().endsWith(".sql")) {
            return Collections.emptyList();
        }
        if (isSQLAndResultFile(sqlCaseFileContent)) {
            return extractSQLsFromSQLAndResultFile(sqlCaseFileContent).stream().map(each -> new ExternalSQLTestParameter(sqlCaseFileName, databaseType, each, reportType))
                    .collect(Collectors.toList());
        }
        return new OneCasePerFileExternalTestParameterLoadTemplate().load(sqlCaseFileName, sqlCaseFileContent, resultFileContent, databaseType, reportType);
    }
    
    private static boolean isSQLAndResultFile(final List<String> sqlCaseFileContent) {
        return sqlCaseFileContent.stream().anyMatch(each -> each.startsWith("--!"));
    }
    
    private static Collection<String> extractSQLsFromSQLAndResultFile(final List<String> contentLines) {
        boolean isSQLLine = false;
        StringBuilder sqlBuilder = new StringBuilder();
        Collection<String> result = new LinkedList<>();
        for (String each : contentLines) {
            if (each.startsWith("--!")) {
                if (sqlBuilder.length() > 0) {
                    result.add(sqlBuilder.toString());
                    sqlBuilder = new StringBuilder();
                }
                isSQLLine = !isSQLLine;
                continue;
            }
            if (isSQLLine) {
                sqlBuilder.append(each).append(System.lineSeparator());
                if (each.endsWith(";")) {
                    result.add(sqlBuilder.toString());
                    sqlBuilder = new StringBuilder();
                }
            }
        }
        return result.stream().map(each -> each.replaceAll(REGEX, "a")).collect(Collectors.toList());
    }
}
