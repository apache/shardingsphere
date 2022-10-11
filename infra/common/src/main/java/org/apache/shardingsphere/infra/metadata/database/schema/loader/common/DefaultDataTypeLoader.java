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

package org.apache.shardingsphere.infra.metadata.database.schema.loader.common;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

/**
 * Default data type loader.
 */
public final class DefaultDataTypeLoader extends AbstractDataTypeLoader {
    
    /**
     * Load data type.
     *
     * @param database database
     * @return data type map
     * @throws SQLException SQL exception
     */
    public Map<String, Integer> load(final DatabaseMetaData database) throws SQLException {
        return super.load(database);
    }
    
    @Override
    public String getType() {
        return "Default";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
