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

package org.apache.shardingsphere.sql.parser.engine.firebird.statement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttribute;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Explicit options of a Firebird SET TRANSACTION statement.
 */
@HighFrequencyInvocation
@Getter
public final class FirebirdTransactionOptions implements SQLStatementAttribute {
    
    private final Collection<Option> options = new ArrayList<>();
    
    private final Collection<String> lockTimeouts = new ArrayList<>();
    
    private final Collection<String> snapshotNumbers = new ArrayList<>();
    
    private final Collection<Reservation> reservations = new ArrayList<>();
    
    @Setter
    @Nullable
    private String duplicateClause;
    
    @Setter
    @Nullable
    private Integer invalidLockTimeoutLine;
    
    @Setter
    @Nullable
    private Integer invalidLockTimeoutColumn;
    
    @Setter
    @Nullable
    private String invalidLockTimeoutToken;
    
    @Setter
    @Nullable
    private String invalidSyntaxToken;
    
    @Setter
    @Nullable
    private Integer invalidSyntaxLine;
    
    @Setter
    @Nullable
    private Integer invalidSyntaxColumn;
    
    @Setter
    @Nullable
    private Integer validationPosition;
    
    /**
     * Transaction option without a numeric or table argument.
     */
    public enum Option {
        READ_ONLY, READ_WRITE, WAIT, NO_WAIT, SNAPSHOT, SNAPSHOT_TABLE_STABILITY, READ_COMMITTED, READ_COMMITTED_VERSION,
        READ_COMMITTED_NO_VERSION, READ_COMMITTED_CONSISTENCY, REPEATABLE_READ, SERIALIZABLE, NO_AUTO_UNDO, IGNORE_LIMBO,
        // CHECKSTYLE:OFF
        RESTART_REQUESTS, AUTO_COMMIT, AUTO_RELEASE_TEMP_BLOBID, RESERVING;
        // CHECKSTYLE:ON
    }
    
    /**
     * Table reservation and its requested lock mode.
     */
    @HighFrequencyInvocation
    @RequiredArgsConstructor
    @Getter
    public static final class Reservation {
        
        private final Collection<SimpleTableSegment> tables;
        
        private final LockMode lockMode;
    }
    
    /**
     * Explicit lock mode for a table reservation.
     */
    public enum LockMode {
        // CHECKSTYLE:OFF
        DEFAULT, READ, WRITE, SHARED_READ, SHARED_WRITE, PROTECTED_READ, PROTECTED_WRITE;
        // CHECKSTYLE:ON
    }
}
