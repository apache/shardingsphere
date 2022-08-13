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

package org.apache.shardingsphere.error.mysql.mapper;

import org.apache.shardingsphere.error.vendor.VendorError;
import org.apache.shardingsphere.error.vendor.StandardVendorError;
import org.apache.shardingsphere.error.mapper.DialectSQLExceptionMapper;
import org.apache.shardingsphere.error.mysql.code.MyVendorError;
import org.apache.shardingsphere.infra.exception.dialect.DBCreateExistsException;
import org.apache.shardingsphere.infra.exception.dialect.DBDropNotExistsException;
import org.apache.shardingsphere.infra.exception.dialect.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.infra.exception.dialect.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.NoSuchTableException;
import org.apache.shardingsphere.infra.exception.dialect.TableExistsException;
import org.apache.shardingsphere.infra.exception.dialect.TableModifyInTransactionException;
import org.apache.shardingsphere.infra.exception.dialect.TooManyConnectionsException;
import org.apache.shardingsphere.infra.exception.dialect.UnknownDatabaseException;
import org.apache.shardingsphere.infra.util.exception.inside.InsideDialectSQLException;

import java.sql.SQLException;

/**
 * MySQL exception mapper.
 */
public final class MySQLExceptionMapper implements DialectSQLExceptionMapper {
    
    @Override
    public SQLException convert(final InsideDialectSQLException dialectSQLException) {
        if (dialectSQLException instanceof TableModifyInTransactionException) {
            return toSQLException(MyVendorError.ER_ERROR_ON_MODIFYING_GTID_EXECUTED_TABLE, ((TableModifyInTransactionException) dialectSQLException).getTableName());
        }
        if (dialectSQLException instanceof InsertColumnsAndValuesMismatchedException) {
            return toSQLException(MyVendorError.ER_WRONG_VALUE_COUNT_ON_ROW, ((InsertColumnsAndValuesMismatchedException) dialectSQLException).getMismatchedRowNumber());
        }
        if (dialectSQLException instanceof UnknownDatabaseException) {
            return null != ((UnknownDatabaseException) dialectSQLException).getDatabaseName()
                    ? toSQLException(MyVendorError.ER_BAD_DB_ERROR, ((UnknownDatabaseException) dialectSQLException).getDatabaseName())
                    : toSQLException(MyVendorError.ER_NO_DB_ERROR);
        }
        if (dialectSQLException instanceof NoDatabaseSelectedException) {
            return toSQLException(MyVendorError.ER_NO_DB_ERROR);
        }
        if (dialectSQLException instanceof DBCreateExistsException) {
            return toSQLException(MyVendorError.ER_DB_CREATE_EXISTS_ERROR, ((DBCreateExistsException) dialectSQLException).getDatabaseName());
        }
        if (dialectSQLException instanceof DBDropNotExistsException) {
            return toSQLException(MyVendorError.ER_DB_DROP_NOT_EXISTS_ERROR, ((DBDropNotExistsException) dialectSQLException).getDatabaseName());
        }
        if (dialectSQLException instanceof TableExistsException) {
            return toSQLException(MyVendorError.ER_TABLE_EXISTS_ERROR, ((TableExistsException) dialectSQLException).getTableName());
        }
        if (dialectSQLException instanceof NoSuchTableException) {
            return toSQLException(MyVendorError.ER_NO_SUCH_TABLE, ((NoSuchTableException) dialectSQLException).getTableName());
        }
        if (dialectSQLException instanceof TooManyConnectionsException) {
            return toSQLException(MyVendorError.ER_CON_COUNT_ERROR);
        }
        return toSQLException(StandardVendorError.UNKNOWN_EXCEPTION, dialectSQLException.getMessage());
    }
    
    private SQLException toSQLException(final VendorError vendorError, final Object... messageArguments) {
        return new SQLException(String.format(vendorError.getReason(), messageArguments), vendorError.getSqlState().getValue(), vendorError.getVendorCode());
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
