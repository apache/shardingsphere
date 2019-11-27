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

package org.apache.shardingsphere.shardingscaling.mysql;

import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.AbstractJdbcReader;

import java.util.Map;

/**
 * MySQL JDBC Reader.
 *
 * @author avalon566
 */
public final class MySQLJdbcReader extends AbstractJdbcReader {

    public MySQLJdbcReader(final RdbmsConfiguration rdbmsConfiguration) {
        super(rdbmsConfiguration);
    }

    private String formatMysqlParams(final Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(entry.getKey());
            if (null != entry.getValue()) {
                result.append("=").append(entry.getValue());
            }
            result.append("&");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    private String fixMysqlParams(final Map<String, String> parameters) {
        if (!parameters.containsKey("yearIsDateType")) {
            parameters.put("yearIsDateType", "false");
        }
        return formatMysqlParams(parameters);
    }
}
