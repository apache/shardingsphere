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

package org.apache.shardingsphere.test.it.sql.parser.external.loader.template.type;

import org.apache.shardingsphere.test.it.sql.parser.external.ExternalSQLTestParameter;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.template.ExternalTestParameterLoadTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * One case per file external test parameter load template.
 */
public final class OneCasePerFileExternalTestParameterLoadTemplate implements ExternalTestParameterLoadTemplate {
    
    @Override
    public Collection<ExternalSQLTestParameter> load(final String sqlCaseFileName, final List<String> sqlCaseFileContent, final List<String> resultFileContent,
                                                     final String databaseType, final String reportType) {
        removePrefixedComments(sqlCaseFileContent);
        String sql = String.join(System.lineSeparator(), sqlCaseFileContent).trim();
        return Collections.singleton(new ExternalSQLTestParameter(sqlCaseFileName, databaseType, sql, reportType));
    }
    
    private void removePrefixedComments(final List<String> sqlCaseFileContent) {
        Iterator<String> iterator = sqlCaseFileContent.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().startsWith("--")) {
                return;
            }
            iterator.remove();
        }
    }
}
