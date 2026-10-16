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

package org.apache.shardingsphere.proxy.frontend.firebird;

import lombok.Getter;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBlobContinuationBpbException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchBpbTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentExceedsBlobException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSegmentTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchSmallDataException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BlobFilterNotFoundException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBpbVersionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.ParameterConversionException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.TransliterationFailedException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.UnknownBatchBlobIdException;
import org.apache.shardingsphere.database.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.database.protocol.firebird.codec.FirebirdPacketCodecEngine;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.authentication.FirebirdAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.command.FirebirdCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.firebird.resource.FirebirdConnectionResourceManager;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;

import java.util.Arrays;
import java.util.Collection;

/**
 * Frontend engine for Firebird.
 */
@Getter
public final class FirebirdFrontendEngine implements DatabaseProtocolFrontendEngine {
    
    private static final Collection<Class<? extends SQLDialectException>> BATCH_BLOB_EXCEPTION_TYPES = Arrays.asList(
            BatchBlobContinuationBpbException.class,
            BatchBpbTooBigException.class,
            BatchSegmentExceedsBlobException.class,
            BatchSegmentTooBigException.class,
            BatchSmallDataException.class,
            BlobFilterNotFoundException.class,
            InvalidBpbVersionException.class,
            ParameterConversionException.class,
            TransliterationFailedException.class,
            UnknownBatchBlobIdException.class);
    
    private final AuthenticationEngine authenticationEngine = new FirebirdAuthenticationEngine();
    
    private final FirebirdCommandExecuteEngine commandExecuteEngine = new FirebirdCommandExecuteEngine();
    
    private final DatabasePacketCodecEngine codecEngine = new FirebirdPacketCodecEngine();
    
    @Override
    public void release(final ConnectionSession connectionSession) {
        FirebirdConnectionResourceManager.getInstance().unregisterConnection(connectionSession.getConnectionId());
    }
    
    @Override
    public void handleException(final ConnectionSession connectionSession, final Exception exception) {
        if (connectionSession.getTransactionStatus().isInTransaction() && !connectionSession.getConnectionContext().getTransactionContext().isExceptionOccur()
                && !(exception instanceof InTransactionException) && !isBatchBlobException(exception)) {
            connectionSession.getConnectionContext().getTransactionContext().setExceptionOccur(true);
        }
    }
    
    private boolean isBatchBlobException(final Exception exception) {
        return BATCH_BLOB_EXCEPTION_TYPES.stream().anyMatch(each -> each.isInstance(exception));
    }
    
    @Override
    public String getDatabaseType() {
        return "Firebird";
    }
}
