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
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;
import org.apache.shardingsphere.sql.parser.spi.SQLVisitorFacade;

/**
 * SQL visitor facade factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLVisitorFacadeFactory {
    
    static {
        ShardingSphereServiceLoader.register(SQLVisitorFacade.class);
    }
    
    /**
     * Get instance of visitor facade.
     * 
     * @param databaseType database type
     * @param visitorType visitor type
     * @return got instance
     */
    public static SQLVisitorFacade getInstance(final String databaseType, final String visitorType) {
        return TypedSPIRegistry.getRegisteredService(SQLVisitorFacade.class, String.join(".", databaseType, visitorType));
    }
}
