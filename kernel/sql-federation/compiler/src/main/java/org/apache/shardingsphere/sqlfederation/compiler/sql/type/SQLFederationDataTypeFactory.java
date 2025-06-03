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

package org.apache.shardingsphere.sqlfederation.compiler.sql.type;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataTypeFactory;

/**
 * SQL federation data type factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLFederationDataTypeFactory {
    
    private static final RelDataTypeFactory DATA_TYPE_FACTORY = new JavaTypeFactoryImpl();
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static RelDataTypeFactory getInstance() {
        return DATA_TYPE_FACTORY;
    }
}
