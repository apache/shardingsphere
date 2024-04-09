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

package org.apache.shardingsphere.sql.parser.core.database.visitor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatementType;

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
        return createParseTreeVisitor(facade, visitorRule.getType());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static SQLStatementVisitor createParseTreeVisitor(final SQLStatementVisitorFacade visitorFacade, final SQLStatementType type) {
        switch (type) {
            case DML:
                return visitorFacade.getDMLVisitorClass().getConstructor().newInstance();
            case DDL:
                return visitorFacade.getDDLVisitorClass().getConstructor().newInstance();
            case TCL:
                return visitorFacade.getTCLVisitorClass().getConstructor().newInstance();
            case DCL:
                return visitorFacade.getDCLVisitorClass().getConstructor().newInstance();
            case DAL:
                return visitorFacade.getDALVisitorClass().getConstructor().newInstance();
            case RL:
                return visitorFacade.getRLVisitorClass().getConstructor().newInstance();
            default:
                throw new SQLParsingException(type.name());
        }
    }
}
