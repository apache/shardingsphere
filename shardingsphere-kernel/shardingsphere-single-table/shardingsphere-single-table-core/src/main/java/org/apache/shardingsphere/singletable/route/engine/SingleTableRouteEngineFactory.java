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

package org.apache.shardingsphere.singletable.route.engine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;

import java.util.Collection;
import java.util.Optional;

/**
 * Single table route engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleTableRouteEngineFactory {
    
    /**
     * Create new instance of single table route engine.
     *
     * @param singleTableNames single table names
     * @param sqlStatement sql statement
     * @return created instance
     */
    public static Optional<SingleTableRouteEngine> newInstance(final Collection<QualifiedTable> singleTableNames, final SQLStatement sqlStatement) {
        // TODO Consider to add route logic for more statements
        if (!singleTableNames.isEmpty()) {
            return Optional.of(new SingleTableStandardRouteEngine(singleTableNames, sqlStatement));
        }
        if (isSchemaDDLStatement(sqlStatement)) {
            return Optional.of(new SingleTableDatabaseBroadcastRouteEngine());
        }
        return Optional.empty();
    }
    
    private static boolean isSchemaDDLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof CreateSchemaStatement || sqlStatement instanceof AlterSchemaStatement || sqlStatement instanceof DropSchemaStatement;
    }
}
