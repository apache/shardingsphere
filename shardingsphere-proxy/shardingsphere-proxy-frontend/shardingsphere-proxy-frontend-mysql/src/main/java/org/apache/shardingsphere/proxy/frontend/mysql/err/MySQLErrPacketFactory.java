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

package org.apache.shardingsphere.proxy.frontend.mysql.err;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobNotFoundException;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.error.SQLExceptionHandler;
import org.apache.shardingsphere.error.code.StandardSQLErrorCode;
import org.apache.shardingsphere.error.mysql.code.MySQLServerErrorCode;
import org.apache.shardingsphere.infra.util.exception.inside.ShardingSphereInsideException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.exception.CommonDistSQLErrorCode;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.exception.CommonDistSQLException;
import org.apache.shardingsphere.proxy.frontend.exception.FrontendTooManyConnectionsException;
import org.apache.shardingsphere.proxy.frontend.exception.UnsupportedPreparedStatementException;

import java.nio.charset.UnsupportedCharsetException;
import java.sql.SQLException;

/**
 * ERR packet factory for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLErrPacketFactory {
    
    /**
     * Create new instance of MySQL ERR packet.
     *
     * @param cause cause
     * @return created instance
     */
    public static MySQLErrPacket newInstance(final Exception cause) {
        if (cause instanceof SQLException) {
            SQLException sqlException = (SQLException) cause;
            return null == sqlException.getSQLState() ? new MySQLErrPacket(1, MySQLServerErrorCode.ER_INTERNAL_ERROR, getErrorMessage(sqlException))
                    : new MySQLErrPacket(1, sqlException.getErrorCode(), sqlException.getSQLState(), sqlException.getMessage());
        }
        if (cause instanceof ShardingSphereInsideException) {
            SQLException sqlException = SQLExceptionHandler.convert("MySQL", (ShardingSphereInsideException) cause);
            return new MySQLErrPacket(1, sqlException.getErrorCode(), sqlException.getSQLState(), sqlException.getMessage());
        }
        if (cause instanceof CommonDistSQLException) {
            CommonDistSQLException commonDistSQLException = (CommonDistSQLException) cause;
            return new MySQLErrPacket(1, CommonDistSQLErrorCode.valueOf(commonDistSQLException), commonDistSQLException.getVariable());
        }
        if (cause instanceof UnsupportedPreparedStatementException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_UNSUPPORTED_PS);
        }
        if (cause instanceof PipelineJobNotFoundException) {
            return new MySQLErrPacket(1, StandardSQLErrorCode.SCALING_JOB_NOT_EXIST, ((PipelineJobNotFoundException) cause).getJobId());
        }
        if (cause instanceof FrontendTooManyConnectionsException) {
            return new MySQLErrPacket(0, MySQLServerErrorCode.ER_CON_COUNT_ERROR, MySQLServerErrorCode.ER_CON_COUNT_ERROR.getErrorMessage());
        }
        if (cause instanceof UnsupportedCharsetException) {
            return new MySQLErrPacket(1, MySQLServerErrorCode.ER_UNKNOWN_CHARACTER_SET, cause.getMessage());
        }
        return new MySQLErrPacket(1, StandardSQLErrorCode.UNKNOWN_EXCEPTION, cause.getMessage());
    }
    
    private static String getErrorMessage(final SQLException cause) {
        return null == cause.getNextException() || !Strings.isNullOrEmpty(cause.getMessage()) ? cause.getMessage() : cause.getNextException().getMessage();
    }
}
