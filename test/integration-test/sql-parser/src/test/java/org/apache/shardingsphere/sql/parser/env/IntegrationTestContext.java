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

package org.apache.shardingsphere.sql.parser.env;

import org.apache.shardingsphere.sql.parser.loader.DynamicSQLCaseLoaderStrategy;
import org.apache.shardingsphere.sql.parser.loader.GitHubCaseLoader;
import org.apache.shardingsphere.sql.parser.loader.GiteeCaseLoader;
import org.apache.shardingsphere.sql.parser.loader.LocalCaseLoader;

import java.net.URI;
import java.util.Collection;

public class IntegrationTestContext {
    private final DynamicSQLCaseLoaderStrategy strategy;

    public IntegrationTestContext(final LoaderType loaderType) {
        switch (loaderType) {
            case GITHUB:
                this.strategy = new GitHubCaseLoader();
                return;
            case GITEE:
                this.strategy = new GiteeCaseLoader();
                return;
            default:
                this.strategy = new LocalCaseLoader();
        }
    }

    /**
     * Get test parameters.
     *
     * @param sqlCaseURI the URI of sql case
     *
     * @return Test cases from with strategy
     */
    public Collection<Object[]> getTestParameters(final URI sqlCaseURI) {
        return strategy.getTestParameters(sqlCaseURI);
    }
}
