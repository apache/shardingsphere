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

package org.apache.shardingsphere.sql.parser.core.visitor;

import org.apache.shardingsphere.sql.parser.spi.SQLVisitorFacade;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * SQL visitor facade registry.
 */
public final class SQLVisitorFacadeRegistry {
    
    private static final SQLVisitorFacadeRegistry INSTANCE = new SQLVisitorFacadeRegistry();
    
    private final Map<String, SQLVisitorFacade> facades = new LinkedHashMap<>();
    
    private SQLVisitorFacadeRegistry() {
        for (SQLVisitorFacade each : ServiceLoader.load(SQLVisitorFacade.class)) {
            facades.put(getQualifiedType(each.getDatabaseType(), each.getVisitorType()), each);
        }
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static SQLVisitorFacadeRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get SQL visitor facade.
     * 
     * @param databaseType database type
     * @param visitorType visitor type
     * @return SQL visitor facade
     */
    public SQLVisitorFacade getSQLVisitorFacade(final String databaseType, final String visitorType) {
        String qualifiedType = getQualifiedType(databaseType, visitorType);
        if (facades.containsKey(qualifiedType)) {
            return facades.get(qualifiedType);
        }
        throw new UnsupportedOperationException(String.format("Cannot support '%s' visitor with database '%s'", visitorType, databaseType));
    }
    
    private String getQualifiedType(final String databaseType, final String visitorType) {
        return String.join(".", databaseType, visitorType);
    }
}
