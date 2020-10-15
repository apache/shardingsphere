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

package org.apache.shardingsphere.infra.metadata.model.physical.jdbc.handler.impl;

import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.metadata.model.physical.jdbc.handler.DatabaseMetaDataDialectHandler;

/**
 * Database meta data dialect handler of PostgreSQL.
 */
@Getter
@Setter
public final class PostgreSQLDatabaseMetaDataDialectHandler implements DatabaseMetaDataDialectHandler {
    
    private Properties props;

    @Override
    public Pair<String, String> getDelimiter() {
        return Pair.of("\"", "\"");
    }

    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
