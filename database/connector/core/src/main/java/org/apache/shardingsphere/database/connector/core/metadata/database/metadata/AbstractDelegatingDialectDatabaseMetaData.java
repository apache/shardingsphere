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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.IdentifierPatternType;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.altertable.DialectAlterTableOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.column.DialectColumnOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.connection.DialectConnectionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.function.DialectFunctionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.index.DialectIndexOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.join.DialectJoinOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.keygen.DialectGeneratedKeyOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sql.DialectSQLOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sqlbatch.DialectSQLBatchOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table.DialectDriverQuerySystemCatalogOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.version.DialectProtocolVersionOption;

import java.util.Objects;
import java.util.Optional;

/**
 * Delegating dialect database meta data.
 *
 * <p>Subclasses own database type identity and may override dialect-specific behavior.</p>
 */
public abstract class AbstractDelegatingDialectDatabaseMetaData implements DialectDatabaseMetaData {
    
    private final DialectDatabaseMetaData delegate;
    
    /**
     * Construct a delegating dialect database meta data.
     *
     * @param delegate dialect database meta data delegate
     * @throws NullPointerException if delegate is null
     */
    protected AbstractDelegatingDialectDatabaseMetaData(final DialectDatabaseMetaData delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }
    
    @Override
    public QuoteCharacter getQuoteCharacter() {
        return delegate.getQuoteCharacter();
    }
    
    @Override
    public IdentifierPatternType getIdentifierPatternType() {
        return delegate.getIdentifierPatternType();
    }
    
    @Override
    public NullsOrderType getDefaultNullsOrderType() {
        return delegate.getDefaultNullsOrderType();
    }
    
    @Override
    public DialectDataTypeOption getDataTypeOption() {
        return delegate.getDataTypeOption();
    }
    
    @Override
    public Optional<DialectDriverQuerySystemCatalogOption> getDriverQuerySystemCatalogOption() {
        return delegate.getDriverQuerySystemCatalogOption();
    }
    
    @Override
    public DialectSchemaOption getSchemaOption() {
        return delegate.getSchemaOption();
    }
    
    @Override
    public DialectColumnOption getColumnOption() {
        return delegate.getColumnOption();
    }
    
    @Override
    public DialectIndexOption getIndexOption() {
        return delegate.getIndexOption();
    }
    
    @Override
    public DialectConnectionOption getConnectionOption() {
        return delegate.getConnectionOption();
    }
    
    @Override
    public DialectTransactionOption getTransactionOption() {
        return delegate.getTransactionOption();
    }
    
    @Override
    public DialectJoinOption getJoinOption() {
        return delegate.getJoinOption();
    }
    
    @Override
    public DialectPaginationOption getPaginationOption() {
        return delegate.getPaginationOption();
    }
    
    @Override
    public Optional<DialectGeneratedKeyOption> getGeneratedKeyOption() {
        return delegate.getGeneratedKeyOption();
    }
    
    @Override
    public Optional<DialectAlterTableOption> getAlterTableOption() {
        return delegate.getAlterTableOption();
    }
    
    @Override
    public DialectSQLBatchOption getSQLBatchOption() {
        return delegate.getSQLBatchOption();
    }
    
    @Override
    public DialectSQLOption getSQLOption() {
        return delegate.getSQLOption();
    }
    
    @Override
    public DialectProtocolVersionOption getProtocolVersionOption() {
        return delegate.getProtocolVersionOption();
    }
    
    @Override
    public DialectFunctionOption getFunctionOption() {
        return delegate.getFunctionOption();
    }
    
    @Override
    public boolean isTableVariableIdentifier(final String value, final QuoteCharacter quoteCharacter) {
        return delegate.isTableVariableIdentifier(value, quoteCharacter);
    }
    
    @Override
    public abstract String getDatabaseType();
}
