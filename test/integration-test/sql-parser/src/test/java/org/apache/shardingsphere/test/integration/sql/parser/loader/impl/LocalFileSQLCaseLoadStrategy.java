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

package org.apache.shardingsphere.test.integration.sql.parser.loader.impl;

import org.apache.shardingsphere.test.integration.sql.parser.loader.SQLCaseLoadStrategy;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * SQL case loader with local file.
 */
public final class LocalFileSQLCaseLoadStrategy implements SQLCaseLoadStrategy {
    
    @Override
    public Collection<Map<String, String>> loadSQLCases(final URI uri) {
        // TODO
        return Collections.emptyList();
    }
    
    @Override
    public Map<String, String> loadSQLCaseResults(final URI uri) {
        // TODO
        return Collections.emptyMap();
    }
}
