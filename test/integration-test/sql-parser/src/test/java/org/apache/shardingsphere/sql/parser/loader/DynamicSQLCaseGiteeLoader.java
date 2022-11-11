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
import java.util.Map;

public class DynamicSQLCaseGiteeLoader extends DynamicLoadingSQLParserParameterizedTest implements DynamicSQLCaseLoaderStrategy {
    
    public DynamicSQLCaseGiteeLoader() {
        super("", "", "", new SQLParserCSVResultProcessor(""));
    }
    
    /**
     * Get test parameters.
     *
     * @param sqlCaseURI the URI of sql case
     *
     * @return Test cases from Gitee.
     **/
    public Collection<Object[]> getTestParameters(final URI sqlCaseURI) {
        Collection<Object[]> result = new LinkedList<>();
        for (Map<String, String> each : getResponse("https://gitee.com/api/v5/repos/", sqlCaseURI)) {
            String sqlCaseFileName = each.get("name").split("\\.")[0];
            String sqlCaseFileContent = getContent(URI.create(each.get("download_url")));
            result.addAll(getSQLCases(sqlCaseFileName, sqlCaseFileContent));
        }
        if (result.isEmpty()) {
            result.add(new Object[]{"", ""});
        }
        return result;
    }
}
