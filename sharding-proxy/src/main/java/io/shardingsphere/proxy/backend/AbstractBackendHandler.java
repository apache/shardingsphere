/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.backend;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingsphere.proxy.config.ProxyContext;
import io.shardingsphere.proxy.frontend.common.FrontendHandler;
import io.shardingsphere.proxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;

import java.sql.SQLException;

/**
 * Abstract backend handler.
 *
 * @author zhangliang
 */
public abstract class AbstractBackendHandler implements BackendHandler {
    
    private static final ProxyContext PROXY_CONTEXT = ProxyContext.getInstance();
    
    @Override
    public final CommandResponsePackets execute() {
        try {
            return execute0();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            Optional<SQLException> sqlException = findSQLException(ex);
            return sqlException.isPresent()
                    ? new CommandResponsePackets(new ErrPacket(1, sqlException.get())) : new CommandResponsePackets(new ErrPacket(1, ServerErrorCode.ER_STD_UNKNOWN_EXCEPTION, ex.getMessage()));
        }
    }
    
    protected abstract CommandResponsePackets execute0() throws Exception;
    
    private Optional<SQLException> findSQLException(final Exception exception) {
        if (exception instanceof SQLException) {
            return Optional.of((SQLException) exception);
        }
        if (null == exception.getCause()) {
            return Optional.absent();
        }
        if (exception.getCause() instanceof SQLException) {
            return Optional.of((SQLException) exception.getCause());
        }
        if (null == exception.getCause().getCause()) {
            return Optional.absent();
        }
        if (exception.getCause().getCause() instanceof SQLException) {
            return Optional.of((SQLException) exception.getCause().getCause());
        }
        return Optional.absent();
    }
    
    protected final CommandResponsePackets handleUseStatement(final UseStatement useStatement, final FrontendHandler frontendHandler) {
        String schema = useStatement.getSchema();
        if (!PROXY_CONTEXT.schemaExists(schema)) {
            return new CommandResponsePackets(new ErrPacket(1, ServerErrorCode.ER_BAD_DB_ERROR, schema));
        }
        frontendHandler.setCurrentSchema(schema);
        return new CommandResponsePackets(new OKPacket(1));
    }
}
