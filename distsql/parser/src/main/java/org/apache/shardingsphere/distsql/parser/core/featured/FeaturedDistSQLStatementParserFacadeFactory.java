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

package org.apache.shardingsphere.distsql.parser.core.featured;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.engine.spi.FeaturedDistSQLStatementParserFacade;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

import java.util.Collection;

/**
 * Featured dist SQL statement parser facade factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeaturedDistSQLStatementParserFacadeFactory {
    
    static {
        ShardingSphereServiceLoader.register(FeaturedDistSQLStatementParserFacade.class);
    }
    
    /**
     * Get instance of featured dist SQL statement parser facade.
     * 
     * @param type feature type
     * @return got instance
     */
    public static FeaturedDistSQLStatementParserFacade getInstance(final String type) {
        return TypedSPIRegistry.getRegisteredService(FeaturedDistSQLStatementParserFacade.class, type);
    }
    
    /**
     * Get all instances of featured dist SQL statement parser facade.
     * 
     * @return got instances
     */
    public static Collection<FeaturedDistSQLStatementParserFacade> getAllInstances() {
        return ShardingSphereServiceLoader.getServiceInstances(FeaturedDistSQLStatementParserFacade.class);
    }
}
