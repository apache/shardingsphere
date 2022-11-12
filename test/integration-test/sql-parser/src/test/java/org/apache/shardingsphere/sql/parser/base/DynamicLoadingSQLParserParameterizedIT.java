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

package org.apache.shardingsphere.sql.parser.base;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.util.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.result.SQLParserResultProcessor;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Properties;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class DynamicLoadingSQLParserParameterizedIT {
    
    private final String sqlCaseId;
    
    private final String sql;
    
    private final String databaseType;
    
    // TODO this will refactor as an abstract
    private final SQLParserResultProcessor resultGenerator;
    
    protected static String loadContent(final URI casesURI) {
        try {
            InputStreamReader in = new InputStreamReader(casesURI.toURL().openStream());
            return new BufferedReader(in).lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException ex) {
            log.warn("Load SQL cases failed, reason is: ", ex);
            return "";
        }
    }
    
    @Test
    public final void assertParseSQL() {
        String result = "success";
        try {
            ParseASTNode parseASTNode = new SQLParserEngine(databaseType, new CacheOption(128, 1024L)).parse(sql, false);
            new SQLVisitorEngine(databaseType, "STATEMENT", true, new Properties()).visit(parseASTNode);
        } catch (final ShardingSphereExternalException | ClassCastException | NullPointerException | IllegalArgumentException | IndexOutOfBoundsException ignore) {
            result = "failed";
            log.warn("ParserError: " + sqlCaseId + " value: " + sql + " db-type: " + databaseType);
        }
        resultGenerator.processResult(sqlCaseId, databaseType, result, sql);
    }
}
