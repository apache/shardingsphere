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

package org.apache.shardingsphere.test.it.sql.parser.firebird;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.LockMode;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.Option;
import org.apache.shardingsphere.sql.parser.engine.firebird.statement.FirebirdTransactionOptions.Reservation;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.ExecuteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SetTransactionStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.InternalSQLParserIT;
import org.apache.shardingsphere.test.it.sql.parser.internal.InternalSQLParserITSettings;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@InternalSQLParserITSettings("Firebird")
class InternalFirebirdParserIT extends InternalSQLParserIT {
    
    @Test
    void assertExecuteBlockOutput() {
        SQLParserEngine parser = new SQLParserEngine("Firebird", new CacheOption(128, 1024L));
        SQLStatementVisitorEngine visitor = new SQLStatementVisitorEngine("Firebird");
        ExecuteStatement actualWithOutput = (ExecuteStatement) visitor.visit(parser.parse("EXECUTE BLOCK RETURNS (V INTEGER) AS BEGIN V = 1; SUSPEND; END;", false));
        ExecuteStatement actualWithoutSuspend = (ExecuteStatement) visitor.visit(parser.parse("EXECUTE BLOCK RETURNS (V INTEGER) AS BEGIN V = 1; END;", false));
        ExecuteStatement actualWithoutOutput = (ExecuteStatement) visitor.visit(parser.parse("EXECUTE BLOCK AS BEGIN END;", false));
        ExecuteStatement actualProcedure = (ExecuteStatement) visitor.visit(parser.parse("EXECUTE PROCEDURE foo_procedure", false));
        assertTrue(actualWithOutput.isReturnsRows());
        assertTrue(actualWithoutSuspend.isReturnsRows());
        assertFalse(actualWithoutOutput.isReturnsRows());
        assertFalse(actualProcedure.isReturnsRows());
    }
    
    @Test
    void assertSetTransactionOptions() {
        SQLParserEngine parser = new SQLParserEngine("Firebird", new CacheOption(128, 1024L));
        SQLStatementVisitorEngine visitor = new SQLStatementVisitorEngine("Firebird");
        String[] statements = {"SET TRANSACTION", "SET TRANSACTION READ ONLY", "SET TRANSACTION READ WRITE NO WAIT",
                "SET TRANSACTION NO WAIT ISOLATION LEVEL READ COMMITTED RECORD_VERSION", "SET TRANSACTION ISOLATION LEVEL SNAPSHOT TABLE STABILITY",
                "SET TRANSACTION SNAPSHOT AT NUMBER 123", "SET TRANSACTION LOCK TIMEOUT 2", "SET TRANSACTION RESERVING T_PLAIN FOR PROTECTED WRITE",
                "SET TRANSACTION RESERVING \"X\"\"Y\" FOR WRITE"};
        for (String each : statements) {
            assertTrue(visitor.visit(parser.parse(each, false)) instanceof SetTransactionStatement, each);
        }
    }
    
    @Test
    void assertDuplicateSetTransactionOption() {
        SQLParserEngine parser = new SQLParserEngine("Firebird", new CacheOption(128, 1024L));
        SQLStatementVisitorEngine visitor = new SQLStatementVisitorEngine("Firebird");
        SetTransactionStatement actualStatement = (SetTransactionStatement) visitor.visit(parser.parse("SET TRANSACTION WAIT READ ONLY NO WAIT READ WRITE", false));
        assertThat(actualStatement.getAttributes().getAttribute(FirebirdTransactionOptions.class).getDuplicateClause(), is("[NO] WAIT"));
    }
    
    @Test
    void assertDuplicateSetTransactionOptionPosition() {
        SQLParserEngine parser = new SQLParserEngine("Firebird", new CacheOption(128, 1024L));
        SQLStatementVisitorEngine visitor = new SQLStatementVisitorEngine("Firebird");
        String before = "SET TRANSACTION WAIT NO WAIT RESERVING T_PLAIN";
        String after = "SET TRANSACTION RESERVING T_PLAIN WAIT NO WAIT";
        FirebirdTransactionOptions actualBefore = ((SetTransactionStatement) visitor.visit(parser.parse(before, false))).getAttributes().getAttribute(FirebirdTransactionOptions.class);
        FirebirdTransactionOptions actualAfter = ((SetTransactionStatement) visitor.visit(parser.parse(after, false))).getAttributes().getAttribute(FirebirdTransactionOptions.class);
        assertTrue(actualBefore.getValidationPosition() < before.indexOf("T_PLAIN"));
        assertTrue(actualAfter.getValidationPosition() > after.indexOf("T_PLAIN"));
    }
    
    @Test
    void assertReservationNameErrors() {
        SQLParserEngine parser = new SQLParserEngine("Firebird", new CacheOption(128, 1024L));
        SQLStatementVisitorEngine visitor = new SQLStatementVisitorEngine("Firebird");
        String empty = "SET TRANSACTION RESERVING \"\"";
        FirebirdTransactionOptions actualEmpty = ((SetTransactionStatement) visitor.visit(parser.parse(empty, false))).getAttributes().getAttribute(FirebirdTransactionOptions.class);
        assertThat(actualEmpty.getReservations().iterator().next().getTables().iterator().next().getTableName().getIdentifier().getValue(), is(""));
        String qualified = "SET TRANSACTION RESERVING T /* . */ . /* . */ X";
        FirebirdTransactionOptions actualQualified = ((SetTransactionStatement) visitor.visit(parser.parse(qualified, false))).getAttributes().getAttribute(FirebirdTransactionOptions.class);
        assertThat(actualQualified.getInvalidSyntaxToken(), is("."));
        assertThat(actualQualified.getValidationPosition(), is(qualified.indexOf(". /*")));
    }
    
    @Test
    void assertInvalidTransactionVersionPosition() {
        SQLParserEngine parser = new SQLParserEngine("Firebird", new CacheOption(128, 1024L));
        SQLStatementVisitorEngine visitor = new SQLStatementVisitorEngine("Firebird");
        SetTransactionStatement actualStatement = (SetTransactionStatement) visitor.visit(parser.parse("SET TRANSACTION READ COMMITTED VERSION", false));
        FirebirdTransactionOptions actualOptions = actualStatement.getAttributes().getAttribute(FirebirdTransactionOptions.class);
        assertThat(actualOptions.getInvalidSyntaxToken(), is("VERSION"));
        assertThat(actualOptions.getInvalidSyntaxLine(), is(1));
        assertThat(actualOptions.getInvalidSyntaxColumn(), is(32));
        SetTransactionStatement actualInvalidIsolation = (SetTransactionStatement) visitor.visit(parser.parse("SET TRANSACTION ISOLATION LEVEL REPEATABLE READ", false));
        FirebirdTransactionOptions actualIsolationOptions = actualInvalidIsolation.getAttributes().getAttribute(FirebirdTransactionOptions.class);
        assertThat(actualIsolationOptions.getInvalidSyntaxToken(), is("REPEATABLE"));
        assertThat(actualIsolationOptions.getInvalidSyntaxColumn(), is(33));
        SetTransactionStatement actualInvalidSnapshot = (SetTransactionStatement) visitor.visit(parser.parse("SET TRANSACTION SNAPSHOT AT NUMBER 1.5", false));
        FirebirdTransactionOptions actualSnapshotOptions = actualInvalidSnapshot.getAttributes().getAttribute(FirebirdTransactionOptions.class);
        assertThat(actualSnapshotOptions.getInvalidSyntaxToken(), is("1.5"));
        assertThat(actualSnapshotOptions.getInvalidSyntaxColumn(), is(36));
        SetTransactionStatement actualSignedTimeout = (SetTransactionStatement) visitor.visit(parser.parse("SET TRANSACTION LOCK TIMEOUT -1", false));
        FirebirdTransactionOptions actualSignedOptions = actualSignedTimeout.getAttributes().getAttribute(FirebirdTransactionOptions.class);
        assertThat(actualSignedOptions.getInvalidSyntaxToken(), is("-"));
        assertThat(actualSignedOptions.getInvalidSyntaxColumn(), is(30));
    }
    
    @Test
    void assertInvalidTransactionLockTimeoutPosition() {
        SQLParserEngine parser = new SQLParserEngine("Firebird", new CacheOption(128, 1024L));
        SQLStatementVisitorEngine visitor = new SQLStatementVisitorEngine("Firebird");
        SetTransactionStatement actualStatement = (SetTransactionStatement) visitor.visit(parser.parse("SET TRANSACTION LOCK TIMEOUT 32768", false));
        FirebirdTransactionOptions actualOptions = actualStatement.getAttributes().getAttribute(FirebirdTransactionOptions.class);
        assertThat(actualOptions.getInvalidLockTimeoutLine(), is(1));
        assertThat(actualOptions.getInvalidLockTimeoutColumn(), is(30));
        SetTransactionStatement actualOverflow = (SetTransactionStatement) visitor.visit(parser.parse("SET TRANSACTION LOCK TIMEOUT 2147483648", false));
        assertThat(actualOverflow.getAttributes().getAttribute(FirebirdTransactionOptions.class).getInvalidLockTimeoutToken(), is("2147483648"));
    }
    
    @Test
    void assertSetTransactionOptionValues() {
        SQLParserEngine parser = new SQLParserEngine("Firebird", new CacheOption(128, 1024L));
        SQLStatementVisitorEngine visitor = new SQLStatementVisitorEngine("Firebird");
        String sql = "SET TRANSACTION READ ONLY WAIT ISOLATION LEVEL READ COMMITTED RECORD_VERSION LOCK TIMEOUT 2 RESERVING T_PLAIN FOR PROTECTED READ";
        SetTransactionStatement actualStatement = (SetTransactionStatement) visitor.visit(parser.parse(sql, false));
        FirebirdTransactionOptions actual = actualStatement.getAttributes().getAttribute(FirebirdTransactionOptions.class);
        assertThat(actual.getOptions(), is(Arrays.asList(Option.READ_ONLY, Option.WAIT, Option.READ_COMMITTED_VERSION, Option.RESERVING)));
        SetTransactionStatement actualNoVersion = (SetTransactionStatement) visitor.visit(parser.parse("SET TRANSACTION READ COMMITTED NO RECORD_VERSION", false));
        assertThat(actualNoVersion.getAttributes().getAttribute(FirebirdTransactionOptions.class).getOptions(), is(Collections.singletonList(Option.READ_COMMITTED_NO_VERSION)));
        assertThat(actual.getLockTimeouts(), is(Collections.singletonList("2")));
        Reservation actualReservation = actual.getReservations().iterator().next();
        assertThat(actualReservation.getLockMode(), is(LockMode.PROTECTED_READ));
        assertThat(actualReservation.getTables().iterator().next().getTableName().getIdentifier().getValue(), is("T_PLAIN"));
    }
}
