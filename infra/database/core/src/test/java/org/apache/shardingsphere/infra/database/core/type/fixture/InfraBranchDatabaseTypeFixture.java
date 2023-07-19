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

package org.apache.shardingsphere.infra.database.core.type.fixture;

import org.apache.shardingsphere.infra.database.core.type.BranchDatabaseType;
import org.apache.shardingsphere.infra.database.spi.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class InfraBranchDatabaseTypeFixture implements BranchDatabaseType {
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.BACK_QUOTE;
    }
    
    @Override
    public Collection<String> getJdbcUrlPrefixes() {
        return Collections.singleton("jdbc:infra.fixture:branch:");
    }
    
    @Override
    public DataSourceMetaData getDataSourceMetaData(final String url, final String username) {
        return new DataSourceMetaDataFixture(url);
    }
    
    @Override
    public Map<String, Collection<String>> getSystemDatabaseSchemaMap() {
        return new HashMap<>();
    }
    
    @Override
    public Collection<String> getSystemSchemas() {
        return Collections.emptyList();
    }
    
    @Override
    public DatabaseType getTrunkDatabaseType() {
        return TypedSPILoader.getService(DatabaseType.class, "INFRA.TRUNK.FIXTURE");
    }
    
    @Override
    public String getType() {
        return "INFRA.BRANCH.FIXTURE";
    }
}
