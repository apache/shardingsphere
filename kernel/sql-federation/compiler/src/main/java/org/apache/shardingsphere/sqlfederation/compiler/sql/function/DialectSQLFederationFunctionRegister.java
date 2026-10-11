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

package org.apache.shardingsphere.sqlfederation.compiler.sql.function;

import org.apache.calcite.schema.SchemaPlus;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.util.Collection;
import java.util.Collections;

/**
 * Dialect SQL federation function register.
 */
@SingletonSPI
public interface DialectSQLFederationFunctionRegister extends DatabaseTypedSPI {
    
    /**
     * Register function.
     *
     * @param schemaPlus schema plus
     * @param schemaName schema name
     */
    void registerFunction(SchemaPlus schemaPlus, String schemaName);
    
    /**
     * Get names of functions that are unsupported by SQL Federation.
     *
     * <p>Function names are matched case-insensitively.</p>
     *
     * @return names of functions that are unsupported by SQL Federation
     */
    default Collection<String> getUnsupportedFunctionNames() {
        return Collections.emptyList();
    }
}
