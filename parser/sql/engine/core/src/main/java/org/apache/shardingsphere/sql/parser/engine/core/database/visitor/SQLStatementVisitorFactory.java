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

package org.apache.shardingsphere.sql.parser.engine.core.database.visitor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.SQLStatementType;

/**
 * SQL statement visitor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementVisitorFactory {
    
    /**
     * Create new instance of SQL visitor.
     *
     * @param databaseType database type
     * @param visitorRule SQL visitor rule
     * @return created instance
     */
    public static SQLStatementVisitor newInstance(final DatabaseType databaseType, final SQLVisitorRule visitorRule) {
        SQLStatementVisitorFacade facade = DatabaseTypedSPILoader.getService(SQLStatementVisitorFacade.class, databaseType);
        return createParseTreeVisitor(databaseType, facade, visitorRule.getType());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static SQLStatementVisitor createParseTreeVisitor(final DatabaseType databaseType, final SQLStatementVisitorFacade visitorFacade, final SQLStatementType type) {
        switch (type) {
            case DML:
                return visitorFacade.getDMLVisitorClass().getConstructor(DatabaseType.class).newInstance(databaseType);
            case DDL:
                return visitorFacade.getDDLVisitorClass().getConstructor(DatabaseType.class).newInstance(databaseType);
            case TCL:
                return visitorFacade.getTCLVisitorClass().getConstructor(DatabaseType.class).newInstance(databaseType);
            case LCL:
                return visitorFacade.getLCLVisitorClass().getConstructor(DatabaseType.class).newInstance(databaseType);
            case DCL:
                return visitorFacade.getDCLVisitorClass().getConstructor(DatabaseType.class).newInstance(databaseType);
            case DAL:
                return visitorFacade.getDALVisitorClass().getConstructor(DatabaseType.class).newInstance(databaseType);
            default:
                throw new SQLParsingException(type.name());
        }
    }
}
