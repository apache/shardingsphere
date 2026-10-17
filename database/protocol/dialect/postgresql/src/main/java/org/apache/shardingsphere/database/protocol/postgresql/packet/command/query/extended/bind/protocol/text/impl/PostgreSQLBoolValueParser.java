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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.PostgreSQLTextValueParser;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

/**
 * Bool value parser of PostgreSQL.
 */
public final class PostgreSQLBoolValueParser implements PostgreSQLTextValueParser<Boolean> {
    
    private static final Collection<String> TRUE_VALUES = new HashSet<>(Arrays.asList("t", "tr", "tru", "true", "y", "ye", "yes", "on", "1"));
    
    private static final Collection<String> FALSE_VALUES = new HashSet<>(Arrays.asList("f", "fa", "fal", "fals", "false", "n", "no", "of", "off", "0"));
    
    private static final String INVALID_TEXT_REPRESENTATION_SQL_STATE = "22P02";
    
    @Override
    public Boolean parse(final String value) {
        String formattedValue = value.trim().toLowerCase(Locale.ROOT);
        if (TRUE_VALUES.contains(formattedValue)) {
            return true;
        }
        ShardingSpherePreconditions.checkContains(FALSE_VALUES, formattedValue,
                () -> new SQLWrapperException(new SQLException(String.format("invalid input syntax for type boolean: \"%s\"", value), INVALID_TEXT_REPRESENTATION_SQL_STATE)));
        return false;
    }
}
