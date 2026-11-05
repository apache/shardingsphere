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

package org.apache.shardingsphere.sql.parser.engine.firebird.visitor.statement.type;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.TransactionOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.TransactionReservationContext;
import org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.TransactionTableNameContext;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.LockMode;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.Option;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.Reservation;
import org.apache.shardingsphere.sql.parser.engine.firebird.visitor.statement.FirebirdStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * TCL statement visitor for Firebird.
 */
public final class FirebirdTCLStatementVisitor extends FirebirdStatementVisitor implements TCLStatementVisitor {
    
    public FirebirdTCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    @HighFrequencyInvocation
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        FirebirdTransactionOptions options = new FirebirdTransactionOptions();
        Set<String> seen = new HashSet<>();
        for (TransactionOptionContext each : ctx.transactionOption()) {
            String clause = getClause(each);
            if (null != each.numberSign() && !hasValidationError(options)) {
                TerminalNode sign = null == each.numberSign().PLUS_() ? each.numberSign().MINUS_() : each.numberSign().PLUS_();
                setInvalidSyntax(options, sign);
            }
            if (null != each.TIMEOUT() && !hasValidationError(options)) {
                validateLockTimeout(each, options);
            }
            if (null != each.AT() && !hasValidationError(options) && isInvalidSnapshotNumber(each.NUMBER_().getText())) {
                setInvalidSyntax(options, each.NUMBER_());
            }
            if (!hasValidationError(options)) {
                Optional<TerminalNode> invalidToken = null == each.VERSION() ? getInvalidIsolationToken(each) : Optional.of(each.VERSION());
                invalidToken.ifPresent(token -> setInvalidSyntax(options, token));
            }
            if (null != each.AT() && !seen.add("SNAPSHOT AT NUMBER") && !hasValidationError(options)) {
                options.setDuplicateClause("SNAPSHOT AT NUMBER");
                options.setValidationPosition(each.getStop().getStopIndex());
            }
            if (!seen.add(clause) && !hasValidationError(options)) {
                options.setDuplicateClause(clause);
                options.setValidationPosition(each.getStop().getStopIndex());
            }
            collectOption(each, options);
        }
        return new SetTransactionStatement(getDatabaseType(), null, null, null, options);
    }
    
    private String getClause(final TransactionOptionContext ctx) {
        if (null != ctx.ONLY() || null != ctx.WRITE()) {
            return "READ {ONLY | WRITE}";
        }
        if (null != ctx.WAIT()) {
            return "[NO] WAIT";
        }
        if (null != ctx.SNAPSHOT() || null != ctx.levelOfIsolation() || null != ctx.COMMITTED() || null != ctx.UNCOMMITTED()) {
            return "ISOLATION LEVEL";
        }
        if (null != ctx.UNDO()) {
            return "NO AUTO UNDO";
        }
        if (null != ctx.LIMBO()) {
            return "IGNORE LIMBO";
        }
        if (null != ctx.REQUESTS()) {
            return "RESTART REQUESTS";
        }
        if (null != ctx.COMMIT()) {
            return "AUTO COMMIT";
        }
        if (null != ctx.BLOBID()) {
            return "AUTO RELEASE TEMP BLOBID";
        }
        return null == ctx.TIMEOUT() ? "RESERVING" : "LOCK TIMEOUT";
    }
    
    private boolean hasValidationError(final FirebirdTransactionOptions options) {
        return null != options.getDuplicateClause() || null != options.getInvalidLockTimeoutLine() || null != options.getInvalidSyntaxToken();
    }
    
    private void setInvalidSyntax(final FirebirdTransactionOptions options, final TerminalNode token) {
        options.setInvalidSyntaxToken(token.getText());
        options.setInvalidSyntaxLine(token.getSymbol().getLine());
        options.setInvalidSyntaxColumn(token.getSymbol().getCharPositionInLine() + 1);
        options.setValidationPosition(token.getSymbol().getStartIndex());
    }
    
    private void validateLockTimeout(final TransactionOptionContext ctx, final FirebirdTransactionOptions options) {
        String value = ctx.NUMBER_().getText();
        try {
            long number = Long.parseLong(value);
            if (number <= Short.MAX_VALUE) {
                return;
            }
            if (number > Integer.MAX_VALUE) {
                options.setInvalidLockTimeoutToken(value);
            }
        } catch (final NumberFormatException ex) {
            options.setInvalidLockTimeoutToken(value);
        }
        options.setInvalidLockTimeoutLine(ctx.NUMBER_().getSymbol().getLine());
        options.setInvalidLockTimeoutColumn(ctx.NUMBER_().getSymbol().getCharPositionInLine() + 1);
        options.setValidationPosition(ctx.NUMBER_().getSymbol().getStartIndex());
    }
    
    private boolean isInvalidSnapshotNumber(final String value) {
        try {
            Long.parseLong(value);
            return false;
        } catch (final NumberFormatException ex) {
            return true;
        }
    }
    
    private Optional<TerminalNode> getInvalidIsolationToken(final TransactionOptionContext ctx) {
        if (null == ctx.levelOfIsolation()) {
            return Optional.empty();
        }
        return Optional.ofNullable(null == ctx.levelOfIsolation().REPEATABLE() ? ctx.levelOfIsolation().SERIALIZABLE() : ctx.levelOfIsolation().REPEATABLE());
    }
    
    private void collectOption(final TransactionOptionContext ctx, final FirebirdTransactionOptions options) {
        if (null != ctx.ONLY()) {
            options.getOptions().add(Option.READ_ONLY);
        } else if (null != ctx.WRITE()) {
            options.getOptions().add(Option.READ_WRITE);
        } else if (null != ctx.WAIT()) {
            options.getOptions().add(null == ctx.NO() ? Option.WAIT : Option.NO_WAIT);
        } else if (null != ctx.SNAPSHOT()) {
            options.getOptions().add(null == ctx.TABLE() ? Option.SNAPSHOT : Option.SNAPSHOT_TABLE_STABILITY);
            if (null != ctx.AT()) {
                options.getSnapshotNumbers().add(ctx.NUMBER_().getText());
            }
        } else if (null != ctx.levelOfIsolation()) {
            if (null != ctx.levelOfIsolation().REPEATABLE()) {
                options.getOptions().add(Option.REPEATABLE_READ);
            } else if (null != ctx.levelOfIsolation().SERIALIZABLE()) {
                options.getOptions().add(Option.SERIALIZABLE);
            } else {
                options.getOptions().add(Option.READ_COMMITTED);
            }
        } else if (null != ctx.COMMITTED() || null != ctx.UNCOMMITTED()) {
            options.getOptions().add(getReadCommittedOption(ctx));
        } else if (null != ctx.UNDO()) {
            options.getOptions().add(Option.NO_AUTO_UNDO);
        } else if (null != ctx.LIMBO()) {
            options.getOptions().add(Option.IGNORE_LIMBO);
        } else if (null != ctx.REQUESTS()) {
            options.getOptions().add(Option.RESTART_REQUESTS);
        } else if (null != ctx.COMMIT()) {
            options.getOptions().add(Option.AUTO_COMMIT);
        } else if (null != ctx.BLOBID()) {
            options.getOptions().add(Option.AUTO_RELEASE_TEMP_BLOBID);
        } else if (null != ctx.TIMEOUT()) {
            options.getLockTimeouts().add(ctx.NUMBER_().getText());
        } else if (null != ctx.RESERVING()) {
            options.getOptions().add(Option.RESERVING);
            for (TransactionReservationContext each : ctx.transactionReservation()) {
                options.getReservations().add(new Reservation(getTransactionTables(each, options), getLockMode(each)));
            }
        }
    }
    
    private Option getReadCommittedOption(final TransactionOptionContext ctx) {
        if (null != ctx.CONSISTENCY()) {
            return Option.READ_COMMITTED_CONSISTENCY;
        }
        if (null == ctx.RECORD_VERSION()) {
            return Option.READ_COMMITTED;
        }
        return null == ctx.NO() ? Option.READ_COMMITTED_VERSION : Option.READ_COMMITTED_NO_VERSION;
    }
    
    private Collection<SimpleTableSegment> getTransactionTables(final TransactionReservationContext ctx, final FirebirdTransactionOptions options) {
        Collection<SimpleTableSegment> result = new ArrayList<>(ctx.transactionTableName().size());
        for (TransactionTableNameContext each : ctx.transactionTableName()) {
            result.add(getTransactionTable(each, options));
        }
        return result;
    }
    
    private SimpleTableSegment getTransactionTable(final TransactionTableNameContext ctx, final FirebirdTransactionOptions options) {
        TableNameContext table = ctx.tableName();
        if (null == table) {
            return new SimpleTableSegment(new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue("\"\"")));
        }
        TerminalNode dot = table.DOT_();
        if (null != dot && (null == options.getValidationPosition() || dot.getSymbol().getStartIndex() < options.getValidationPosition())) {
            setInvalidSyntax(options, dot);
        }
        return (SimpleTableSegment) visit(table);
    }
    
    private LockMode getLockMode(final TransactionReservationContext ctx) {
        if (null == ctx.FOR()) {
            return LockMode.DEFAULT;
        }
        if (null != ctx.SHARED()) {
            return null == ctx.READ() ? LockMode.SHARED_WRITE : LockMode.SHARED_READ;
        }
        if (null != ctx.PROTECTED()) {
            return null == ctx.READ() ? LockMode.PROTECTED_WRITE : LockMode.PROTECTED_READ;
        }
        return null == ctx.READ() ? LockMode.WRITE : LockMode.READ;
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new CommitStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new RollbackStatement(getDatabaseType());
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new SavepointStatement(getDatabaseType(), null);
    }
}
