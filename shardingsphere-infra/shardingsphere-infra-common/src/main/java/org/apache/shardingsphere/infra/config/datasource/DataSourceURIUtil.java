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

package org.apache.shardingsphere.infra.config.datasource;

import com.google.common.base.Splitter;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Data source URI util.
 */
public final class DataSourceURIUtil {
    
    private static final String JDBC_URL_PREFIXE = "jdbc:";
    
    /**
     * Parse jdbc URL to URI object.
     *
     * @param jdbcUrl jdbc URL
     * @return URI object
     */
    public static URI parseToURI(final String jdbcUrl) {
        if (jdbcUrl.toLowerCase().startsWith(JDBC_URL_PREFIXE)) {
            return URI.create(jdbcUrl.substring(5));
        }
        return null;
    }
    
    /**
     * Get query map from jdbc URL.
     *
     * @param jdbcUrl jdbc URL
     * @return param map of query
     */
    public static Map<String, String> getQueryMapFromUrl(final String jdbcUrl) {
        URI uri = parseToURI(jdbcUrl);
        if (null != uri && null != uri.getQuery() && !uri.getQuery().isEmpty()) {
            return Splitter.on("&").withKeyValueSeparator("=").split(uri.getQuery());
        }
        return new HashMap<>(16, 1);
    }
}
