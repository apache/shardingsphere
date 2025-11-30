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

package org.apache.shardingsphere.test.it.sql.parser.external.loader.template;

import org.apache.shardingsphere.test.it.sql.parser.external.ExternalSQLTestParameter;

import java.util.Collection;
import java.util.List;

/**
 * External test parameter load template.
 */
public interface ExternalTestParameterLoadTemplate {
    
    /**
     * Load test parameters.
     *
     * @param sqlCaseFileName SQL case file name
     * @param sqlCaseFileContent SQL case file content
     * @param resultFileContent result file content
     * @param databaseType database type
     * @param reportType report type
     * @return loaded test parameters
     */
    Collection<ExternalSQLTestParameter> load(String sqlCaseFileName, List<String> sqlCaseFileContent, List<String> resultFileContent, String databaseType, String reportType);
}
