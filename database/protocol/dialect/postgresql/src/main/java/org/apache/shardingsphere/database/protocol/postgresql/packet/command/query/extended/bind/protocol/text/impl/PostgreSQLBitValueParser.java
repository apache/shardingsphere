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
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

/**
 * Bit value parser of PostgreSQL.
 */
public final class PostgreSQLBitValueParser implements PostgreSQLTextValueParser<PGobject> {
    
    @Override
    public PGobject parse(final String value) {
        try {
            PGobject result = new PGobject();
            result.setType("bit");
            result.setValue(value);
            return result;
        } catch (final SQLException ex) {
            throw new SQLWrapperException(ex);
        }
    }
}
