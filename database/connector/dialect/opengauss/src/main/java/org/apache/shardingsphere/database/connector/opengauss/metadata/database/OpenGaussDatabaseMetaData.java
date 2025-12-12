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

package org.apache.shardingsphere.database.connector.opengauss.metadata.database;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.index.DialectIndexOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table.DialectDriverQuerySystemCatalogOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.version.DialectProtocolVersionOption;
import org.apache.shardingsphere.database.connector.opengauss.metadata.database.option.OpenGaussDataTypeOption;
import org.apache.shardingsphere.database.connector.opengauss.metadata.database.option.OpenGaussDriverQuerySystemCatalogOption;
import org.apache.shardingsphere.database.connector.opengauss.metadata.database.option.OpenGaussSchemaOption;

import java.sql.Connection;
import java.util.Collections;
import java.util.Optional;

/**
 * Database meta data of openGauss.
 */
public final class OpenGaussDatabaseMetaData implements DialectDatabaseMetaData {
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return QuoteCharacter.QUOTE;
    }
    
    @Override
    public IdentifierPatternType getIdentifierPatternType() {
        return IdentifierPatternType.LOWER_CASE;
    }
    
    @Override
    public NullsOrderType getDefaultNullsOrderType() {
        return NullsOrderType.HIGH;
    }
    
    @Override
    public DialectDataTypeOption getDataTypeOption() {
        return new OpenGaussDataTypeOption();
    }
    
    @Override
    public Optional<DialectDriverQuerySystemCatalogOption> getDriverQuerySystemCatalogOption() {
        return Optional.of(new OpenGaussDriverQuerySystemCatalogOption());
    }
    
    @Override
    public DialectSchemaOption getSchemaOption() {
        return new OpenGaussSchemaOption();
    }
    
    @Override
    public DialectIndexOption getIndexOption() {
        return new DialectIndexOption(true);
    }
    
    @Override
    public DialectTransactionOption getTransactionOption() {
        return new DialectTransactionOption(true, false, false, true, false, Connection.TRANSACTION_READ_COMMITTED, true, true, Collections.singleton("org.opengauss.xa.PGXADataSource"));
    }
    
    @Override
    public DialectProtocolVersionOption getProtocolVersionOption() {
        return new DialectProtocolVersionOption("9.2.4");
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
