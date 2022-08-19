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

package org.apache.shardingsphere.dialect.mysql.mapper;

import org.apache.shardingsphere.dialect.exception.SQLDialectException;
import org.apache.shardingsphere.dialect.exception.connection.TooManyConnectionsException;
import org.apache.shardingsphere.dialect.exception.data.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.dialect.exception.syntax.database.DatabaseDropNotExistsException;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.dialect.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.dialect.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.dialect.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.dialect.mysql.vendor.MySQLVendorError;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class MySQLDialectExceptionMapperTest {
    
    private Collection<Object[]> getConvertParameters() {
        return Arrays.asList(new Object[][]{
                {UnknownDatabaseException.class, MySQLVendorError.ER_NO_DB_ERROR},
                {NoDatabaseSelectedException.class, MySQLVendorError.ER_NO_DB_ERROR},
                {DatabaseCreateExistsException.class, MySQLVendorError.ER_DB_CREATE_EXISTS_ERROR},
                {DatabaseDropNotExistsException.class, MySQLVendorError.ER_DB_DROP_NOT_EXISTS_ERROR},
                {TableExistsException.class, MySQLVendorError.ER_TABLE_EXISTS_ERROR},
                {NoSuchTableException.class, MySQLVendorError.ER_NO_SUCH_TABLE},
                {InsertColumnsAndValuesMismatchedException.class, MySQLVendorError.ER_WRONG_VALUE_COUNT_ON_ROW},
                {TableModifyInTransactionException.class, MySQLVendorError.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE},
                {TooManyConnectionsException.class, MySQLVendorError.ER_CON_COUNT_ERROR},
        });
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertConvert() {
        MySQLDialectExceptionMapper mySQLDialect = new MySQLDialectExceptionMapper();
        for (Object[] item : getConvertParameters()) {
            assertThat(mySQLDialect.convert(mock((Class<SQLDialectException>) item[0])).getErrorCode(), is(((MySQLVendorError) item[1]).getVendorCode()));
        }
    }
}
