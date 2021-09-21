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

package org.apache.shardingsphere.encrypt.rewrite.util;

import java.util.Properties;

/**
 * Encrypt properties builder.
 */
public final class EncryptPropertiesBuilder {

    /**
     * Construct properties from schema, owner, table and column.
     * 
     * @param schema schema
     * @param owner  owner
     * @param table  table
     * @param column column
     * @return Properties which include schema, owner, table and column keys.
     */
    public static Properties getProperties(final String schema, final String owner, final String table, final String column) {
        Properties result = new Properties();
        result.put("schema", schema);
        result.put("owner", owner);
        result.put("table", table);
        result.put("column", column);
        return result;
    }
}
