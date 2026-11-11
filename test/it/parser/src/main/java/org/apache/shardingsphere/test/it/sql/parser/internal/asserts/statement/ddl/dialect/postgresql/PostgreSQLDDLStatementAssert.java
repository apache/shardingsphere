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

package org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.collation.DropCollationStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.domain.AlterDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.domain.DropDomainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.function.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.operator.AlterOperatorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.procedure.CreateProcedureStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.sequence.AlterSequenceStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.server.CreateServerStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.type.AlterTypeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterMaterializedViewStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterDefaultPrivilegesStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterLargeObjectStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterOperatorClassStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterOperatorFamilyStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterSystemStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLAlterUserMappingStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLClusterStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateOperatorClassStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateOperatorFamilyStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateTransformStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLCreateUserMappingStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDeclareStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLDropUserMappingStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLImportForeignSchemaStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLListenStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLNotifyStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLOpenStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLSecurityLabelStmtStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.PostgreSQLUnlistenStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.aggregate.PostgreSQLCreateAggregateStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.conversion.PostgreSQLDropConversionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.event.PostgreSQLAlterEventTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.event.PostgreSQLCreateEventTriggerStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.foreigntable.PostgreSQLAlterForeignTableStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.index.PostgreSQLReindexStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.publication.PostgreSQLAlterPublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.publication.PostgreSQLCreatePublicationStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.statistics.PostgreSQLCreateStatisticsStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.subscription.PostgreSQLAlterSubscriptionStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.ddl.subscription.PostgreSQLCreateSubscriptionStatement;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLClusterStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLDeclareStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLListenStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLNotifyStmtStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLOpenStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLReindexStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLSecurityLabelStmtStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.ddl.dialect.postgresql.type.PostgreSQLUnlistenStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.asserts.statement.dml.standard.type.MergeStatementAssert;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.SQLParserTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.oracle.OracleAlterMaterializedViewStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLAlterDefaultPrivilegesTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLAlterLargeObjectStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLAlterOperatorClassStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLAlterOperatorFamilyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLAlterSystemStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLAlterUserMappingStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLClusterStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLCreateOperatorClassStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLCreateOperatorFamilyStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLCreateTransformStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLCreateUserMappingStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLDeclareStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLDropDomainStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLDropUserMappingStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLImportForeignSchemaStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLListenStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLNotifyStmtStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLOpenStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLReindexStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLSecurityLabelStmtStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.PostgreSQLUnlistenStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.aggregate.PostgreSQLCreateAggregateStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.collation.PostgreSQLDropCollationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.conversion.PostgreSQLDropConversionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.event.PostgreSQLAlterEventTriggerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.event.PostgreSQLCreateEventTriggerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.foreigntable.PostgreSQLAlterForeignTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.publication.PostgreSQLAlterPublicationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.publication.PostgreSQLCreatePublicationStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.statistics.PostgreSQLCreateStatisticsStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.subscription.PostgreSQLAlterSubscriptionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.dialect.postgresql.subscription.PostgreSQLCreateSubscriptionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.PreparedStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.domain.AlterDomainStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.function.CreateFunctionStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.operator.AlterOperatorStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.sequence.AlterSequenceStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.server.CreateServerStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.table.CreateTableStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.ddl.standard.type.AlterTypeStatementTestCase;
import org.apache.shardingsphere.test.it.sql.parser.internal.cases.parser.jaxb.statement.plsql.CreateProcedureTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DDL statement assert for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLDDLStatementAssert {
    
    /**
     * Assert DDL statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual DDL statement
     * @param expected expected parser result
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final DDLStatement actual, final SQLParserTestCase expected) {
        if ("PostgreSQL".equals(actual.getDatabaseType().getType())) {
            assertStatementType(assertContext, actual, expected);
            if (actual instanceof CreateTableStatement) {
                int count = 0;
                for (ConstraintDefinitionSegment each : ((CreateTableStatement) actual).getConstraintDefinitions()) {
                    assertThat(assertContext.getText("Constraint definition primary key column size assertion error: "), each.getPrimaryKeyColumns().size(),
                            is(((CreateTableStatementTestCase) expected).getConstraintDefinitions().get(count++).getPrimaryKeyColumns().size()));
                }
            }
        }
        if (actual instanceof PostgreSQLDeclareStatement) {
            PostgreSQLDeclareStatementAssert.assertIs(assertContext, (PostgreSQLDeclareStatement) actual, (PostgreSQLDeclareStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLClusterStatement) {
            PostgreSQLClusterStatementAssert.assertIs(assertContext, (PostgreSQLClusterStatement) actual, (PostgreSQLClusterStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLListenStatement) {
            PostgreSQLListenStatementAssert.assertIs(assertContext, (PostgreSQLListenStatement) actual, (PostgreSQLListenStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLUnlistenStatement) {
            PostgreSQLUnlistenStatementAssert.assertIs(assertContext, (PostgreSQLUnlistenStatement) actual, (PostgreSQLUnlistenStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLNotifyStmtStatement) {
            PostgreSQLNotifyStmtStatementAssert.assertIs(assertContext, (PostgreSQLNotifyStmtStatement) actual, (PostgreSQLNotifyStmtStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLReindexStatement) {
            PostgreSQLReindexStatementAssert.assertIs(assertContext, (PostgreSQLReindexStatement) actual, (PostgreSQLReindexStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLSecurityLabelStmtStatement) {
            PostgreSQLSecurityLabelStmtStatementAssert.assertIs(assertContext, (PostgreSQLSecurityLabelStmtStatement) actual, (PostgreSQLSecurityLabelStmtStatementTestCase) expected);
        } else if (actual instanceof PostgreSQLOpenStatement) {
            PostgreSQLOpenStatementAssert.assertIs(assertContext, (PostgreSQLOpenStatement) actual, (PostgreSQLOpenStatementTestCase) expected);
        }
    }
    
    private static void assertStatementType(final SQLCaseAssertContext assertContext, final DDLStatement actual, final SQLParserTestCase expected) {
        if (expected instanceof PostgreSQLCreateUserMappingStatementTestCase) {
            assertThat(assertContext.getText("Create user mapping statement type assertion error."), actual, isA(PostgreSQLCreateUserMappingStatement.class));
        } else if (expected instanceof PostgreSQLAlterUserMappingStatementTestCase) {
            assertThat(assertContext.getText("Alter user mapping statement type assertion error."), actual, isA(PostgreSQLAlterUserMappingStatement.class));
        } else if (expected instanceof PostgreSQLDropUserMappingStatementTestCase) {
            assertThat(assertContext.getText("Drop user mapping statement type assertion error."), actual, isA(PostgreSQLDropUserMappingStatement.class));
        } else if (expected instanceof PostgreSQLCreateTransformStatementTestCase) {
            assertThat(assertContext.getText("Create transform statement type assertion error."), actual, isA(PostgreSQLCreateTransformStatement.class));
        } else if (expected instanceof PostgreSQLAlterLargeObjectStatementTestCase) {
            assertThat(assertContext.getText("Alter large object statement type assertion error."), actual, isA(PostgreSQLAlterLargeObjectStatement.class));
        } else if (expected instanceof PostgreSQLAlterSystemStatementTestCase) {
            assertThat(assertContext.getText("Alter system statement type assertion error."), actual, isA(PostgreSQLAlterSystemStatement.class));
        } else if (expected instanceof PostgreSQLCreateStatisticsStatementTestCase) {
            assertThat(assertContext.getText("Create statistics statement type assertion error."), actual, isA(PostgreSQLCreateStatisticsStatement.class));
        } else if (expected instanceof PostgreSQLCreateSubscriptionStatementTestCase) {
            assertThat(assertContext.getText("Create subscription statement type assertion error."), actual, isA(PostgreSQLCreateSubscriptionStatement.class));
        } else if (expected instanceof PostgreSQLCreateOperatorClassStatementTestCase) {
            assertThat(assertContext.getText("Create operator class statement type assertion error."), actual, isA(PostgreSQLCreateOperatorClassStatement.class));
        } else if (expected instanceof PostgreSQLAlterOperatorClassStatementTestCase) {
            assertThat(assertContext.getText("Alter operator class statement type assertion error."), actual, isA(PostgreSQLAlterOperatorClassStatement.class));
        } else if (expected instanceof PostgreSQLCreateOperatorFamilyStatementTestCase) {
            assertThat(assertContext.getText("Create operator family statement type assertion error."), actual, isA(PostgreSQLCreateOperatorFamilyStatement.class));
        } else if (expected instanceof PostgreSQLAlterOperatorFamilyStatementTestCase) {
            assertThat(assertContext.getText("Alter operator family statement type assertion error."), actual, isA(PostgreSQLAlterOperatorFamilyStatement.class));
        } else if (expected instanceof PostgreSQLAlterEventTriggerStatementTestCase) {
            assertThat(assertContext.getText("Alter event trigger statement type assertion error."), actual, isA(PostgreSQLAlterEventTriggerStatement.class));
        } else if (expected instanceof PostgreSQLImportForeignSchemaStatementTestCase) {
            assertThat(assertContext.getText("Import foreign schema statement type assertion error."), actual, isA(PostgreSQLImportForeignSchemaStatement.class));
        } else if (expected instanceof PostgreSQLCreatePublicationStatementTestCase) {
            assertThat(assertContext.getText("Create publication statement type assertion error."), actual, isA(PostgreSQLCreatePublicationStatement.class));
        } else if (expected instanceof PostgreSQLAlterPublicationStatementTestCase) {
            assertThat(assertContext.getText("Alter publication statement type assertion error."), actual, isA(PostgreSQLAlterPublicationStatement.class));
        } else if (expected instanceof CreateServerStatementTestCase) {
            assertThat(assertContext.getText("Create server statement type assertion error."), actual, isA(CreateServerStatement.class));
        } else if (expected instanceof PostgreSQLAlterForeignTableStatementTestCase) {
            assertThat(assertContext.getText("Alter foreign table statement type assertion error."), actual, isA(PostgreSQLAlterForeignTableStatement.class));
        } else if (expected instanceof OracleAlterMaterializedViewStatementTestCase) {
            assertThat(assertContext.getText("Alter materialized view statement type assertion error."), actual, isA(AlterMaterializedViewStatement.class));
        } else if (expected instanceof PostgreSQLAlterDefaultPrivilegesTestCase) {
            assertThat(assertContext.getText("Alter default privileges statement type assertion error."), actual, isA(PostgreSQLAlterDefaultPrivilegesStatement.class));
        } else if (expected instanceof PostgreSQLCreateAggregateStatementTestCase) {
            assertThat(assertContext.getText("Create aggregate statement type assertion error."), actual, isA(PostgreSQLCreateAggregateStatement.class));
        } else if (expected instanceof PostgreSQLCreateEventTriggerStatementTestCase) {
            assertThat(assertContext.getText("Create event trigger statement type assertion error."), actual, isA(PostgreSQLCreateEventTriggerStatement.class));
        } else if (expected instanceof PostgreSQLAlterSubscriptionStatementTestCase) {
            assertThat(assertContext.getText("Alter subscription statement type assertion error."), actual, isA(PostgreSQLAlterSubscriptionStatement.class));
        } else if (expected instanceof PostgreSQLReindexStatementTestCase) {
            assertThat(assertContext.getText("Reindex statement type assertion error."), actual, isA(PostgreSQLReindexStatement.class));
        } else if (expected instanceof AlterDomainStatementTestCase) {
            assertThat(assertContext.getText("Alter domain statement type assertion error."), actual, isA(AlterDomainStatement.class));
        } else if (expected instanceof AlterTypeStatementTestCase) {
            assertThat(assertContext.getText("Alter type statement type assertion error."), actual, isA(AlterTypeStatement.class));
        } else if (expected instanceof AlterSequenceStatementTestCase) {
            assertThat(assertContext.getText("Alter sequence statement type assertion error."), actual, isA(AlterSequenceStatement.class));
        } else if (expected instanceof AlterOperatorStatementTestCase) {
            assertThat(assertContext.getText("Alter operator statement type assertion error."), actual, isA(AlterOperatorStatement.class));
        } else if (expected instanceof PostgreSQLDropCollationStatementTestCase) {
            assertThat(assertContext.getText("Drop collation statement type assertion error."), actual, isA(DropCollationStatement.class));
        } else if (expected instanceof PostgreSQLDropConversionStatementTestCase) {
            assertThat(assertContext.getText("Drop conversion statement type assertion error."), actual, isA(PostgreSQLDropConversionStatement.class));
        } else if (expected instanceof PostgreSQLDropDomainStatementTestCase) {
            assertThat(assertContext.getText("Drop domain statement type assertion error."), actual, isA(DropDomainStatement.class));
        } else if (expected instanceof CreateFunctionStatementTestCase) {
            assertThat(assertContext.getText("Create function statement type assertion error."), actual, isA(CreateFunctionStatement.class));
        } else if (expected instanceof CreateProcedureTestCase) {
            assertThat(assertContext.getText("Create procedure statement type assertion error."), actual, isA(CreateProcedureStatement.class));
        } else if (expected instanceof PreparedStatementTestCase) {
            assertThat(assertContext.getText("Prepare statement type assertion error."), actual, isA(PrepareStatement.class));
            PrepareStatement prepareStatement = (PrepareStatement) actual;
            if (null == ((PreparedStatementTestCase) expected).getMerge()) {
                assertFalse(prepareStatement.getMerge().isPresent(), assertContext.getText("Actual merge statement should not exist."));
            } else {
                assertTrue(prepareStatement.getMerge().isPresent(), assertContext.getText("Actual merge statement should exist."));
                MergeStatementAssert.assertIs(assertContext, prepareStatement.getMerge().get(), ((PreparedStatementTestCase) expected).getMerge());
            }
        }
    }
}
