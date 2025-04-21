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

package org.apache.shardingsphere.infra.database.oracle.metadata.database;

import org.apache.shardingsphere.infra.database.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.connection.DialectConnectionOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.index.DialectIndexOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.infra.database.core.metadata.database.metadata.option.table.TableNamePatternType;
import org.apache.shardingsphere.infra.database.oracle.metadata.database.option.OracleDataTypeOption;
import org.apache.shardingsphere.infra.database.oracle.metadata.database.option.OracleSchemaOption;

/**
 * Database meta data of Oracle.
 */
public final class OracleDatabaseMetaData implements DialectDatabaseMetaData {
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.QUOTE;
    }
    
    @Override
    public NullsOrderType getDefaultNullsOrderType() {
        return NullsOrderType.HIGH;
    }
    
    @Override
    public DialectDataTypeOption getDataTypeOption() {
        return new OracleDataTypeOption();
    }
    
    @Override
    public DialectSchemaOption getSchemaOption() {
        return new OracleSchemaOption();
    }
    
    @Override
    public DialectIndexOption getIndexOption() {
        return new DialectIndexOption(true);
    }
    
    @Override
    public TableNamePatternType getTableNamePatternType() {
        return TableNamePatternType.UPPER_CASE;
    }
    
    @Override
    public DialectConnectionOption getConnectionOption() {
        return new DialectConnectionOption(true, false);
    }
    
    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
}
