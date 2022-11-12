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

package org.apache.shardingsphere.sql.parser.loader;

import org.apache.shardingsphere.sql.parser.base.DynamicLoadingSQLParserParameterizedTest;
import org.apache.shardingsphere.sql.parser.result.SQLParserCSVResultProcessor;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Dynamic SQL case local loader.
 */
public final class DynamicSQLCaseLocalLoader extends DynamicLoadingSQLParserParameterizedTest implements DynamicSQLCaseLoaderStrategy {
    
    public DynamicSQLCaseLocalLoader() {
        super("", "", "", new SQLParserCSVResultProcessor(""));
    }
    
    /**
     * Get test parameters.
     *
     * @param sqlCaseTestURI the URI of sql test case
     *
     * @param sqlCaseResultURI the URI of sql result case
     *
     * @return Test cases from localhost.
     **/
    public Collection<Object[]> getTestParameters(final URI sqlCaseTestURI, final URI sqlCaseResultURI) {
        Collection<Object[]> result = new LinkedList<>(getSQLCases("localFile", getContent(sqlCaseTestURI), getContent(sqlCaseResultURI)));
        if (result.isEmpty()) {
            result.add(new Object[]{"", ""});
        }
        return result;
    }
}
