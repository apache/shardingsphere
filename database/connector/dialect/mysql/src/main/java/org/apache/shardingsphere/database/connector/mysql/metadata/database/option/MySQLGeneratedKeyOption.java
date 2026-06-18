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

package org.apache.shardingsphere.database.connector.mysql.metadata.database.option;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.keygen.DialectGeneratedKeyOption;

/**
 * Generated key option of MySQL.
 */
public final class MySQLGeneratedKeyOption extends DialectGeneratedKeyOption {
    
    public MySQLGeneratedKeyOption() {
        super("GENERATED_KEY");
    }
    
    @Override
    public boolean isGeneratedKeyTriggerValue(final Object value) {
        if (null == value) {
            return true;
        }
        if (value instanceof Number && 0L == ((Number) value).longValue()) {
            return true;
        }
        String valueStr = value.toString();
        return "0".equals(valueStr) || "NULL".equalsIgnoreCase(valueStr);
    }
}
