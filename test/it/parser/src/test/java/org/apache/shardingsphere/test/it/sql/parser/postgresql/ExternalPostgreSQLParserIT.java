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

package org.apache.shardingsphere.test.it.sql.parser.postgresql;

import org.apache.shardingsphere.test.it.sql.parser.external.ExternalCaseSettings;
import org.apache.shardingsphere.test.it.sql.parser.external.ExternalSQLParserIT;
import org.apache.shardingsphere.test.it.sql.parser.external.loader.template.type.StandardExternalTestParameterLoadTemplate;

@ExternalCaseSettings(value = "PostgreSQL", caseURL = ExternalPostgreSQLParserIT.CASE_URL, resultURL = ExternalPostgreSQLParserIT.RESULT_URL,
        template = StandardExternalTestParameterLoadTemplate.class)
class ExternalPostgreSQLParserIT extends ExternalSQLParserIT {
    
    static final String CASE_URL = "https://github.com/postgres/postgres/tree/master/src/test/regress/sql";
    
    static final String RESULT_URL = "https://github.com/postgres/postgres/tree/master/src/test/regress/expected";
}
