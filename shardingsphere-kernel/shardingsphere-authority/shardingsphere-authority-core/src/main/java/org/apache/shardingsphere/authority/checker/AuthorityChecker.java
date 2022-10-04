

package org.apache.shardingsphere.authority.checker;

import org.apache.shardingsphere.authority.constant.AuthorityOrder;
import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.check.SQLCheckResult;
import org.apache.shardingsphere.infra.executor.check.SQLChecker;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateFunctionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Authority checker.
 */
public final class AuthorityChecker implements SQLChecker<AuthorityRule> {
    
    @Override
    public boolean check(final String databaseName, final Grantee grantee, final AuthorityRule rule) {
        return null == grantee || rule.findPrivileges(grantee).map(optional -> optional.hasPrivileges(databaseName)).orElse(false);
    }
    
    @Override
    public SQLCheckResult check(final SQLStatementContext<?> sqlStatementContext, final List<Object> parameters, final Grantee grantee,
                                final String currentDatabase, final Map<String, ShardingSphereDatabase> databases, final AuthorityRule rule) {
        if (null == grantee) {
            return new SQLCheckResult(true, "");
        }
        Optional<ShardingSpherePrivileges> privileges = rule.findPrivileges(grantee);
        if (!privileges.isPresent()) {
            return new SQLCheckResult(false, String.format("Access denied for user '%s'@'%s'", grantee.getUsername(), grantee.getHostname()));
        }
        if (null != currentDatabase && !privileges.filter(optional -> optional.hasPrivileges(currentDatabase)).isPresent()) {
            return new SQLCheckResult(false, String.format("Unknown database '%s'", currentDatabase));
        }
        PrivilegeType privilegeType = getPrivilege(sqlStatementContext.getSqlStatement());
        return privileges.map(optional -> {
            boolean isPassed = optional.hasPrivileges(Collections.singletonList(privilegeType));
            String errorMessage = isPassed || null == privilegeType ? "" : String.format("Access denied for operation %s", privilegeType.name());
            return new SQLCheckResult(isPassed, errorMessage);
        }).orElseGet(() -> new SQLCheckResult(false, ""));
    }
    
    @Override
    public boolean check(final Grantee grantee, final AuthorityRule rule) {
        return rule.findUser(grantee).isPresent();
    }
    
    @Override
    public boolean check(final Grantee grantee, final BiPredicate<Object, Object> validator, final Object cipher, final AuthorityRule rule) {
        Optional<ShardingSphereUser> user = rule.findUser(grantee);
        return user.filter(each -> validator.test(each, cipher)).isPresent();
    }
    
    private PrivilegeType getPrivilege(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof MySQLShowDatabasesStatement) {
            return PrivilegeType.SHOW_DB;
        }
        if (sqlStatement instanceof DMLStatement) {
            return getDMLPrivilege(sqlStatement);
        }
        if (sqlStatement instanceof DDLStatement) {
            return getDDLPrivilege(sqlStatement);
        }
        // TODO add more Privilege and SQL statement mapping
        return null;
    }
    
    private PrivilegeType getDMLPrivilege(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return PrivilegeType.SELECT;
        }
        if (sqlStatement instanceof InsertStatement) {
            return PrivilegeType.INSERT;
        }
        if (sqlStatement instanceof UpdateStatement) {
            return PrivilegeType.UPDATE;
        }
        if (sqlStatement instanceof DeleteStatement) {
            return PrivilegeType.DELETE;
        }
        return null;
    }
    
    private PrivilegeType getDDLPrivilege(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof AlterDatabaseStatement) {
            return PrivilegeType.ALTER_ANY_DATABASE;
        }
        if (sqlStatement instanceof AlterTableStatement) {
            return PrivilegeType.ALTER;
        }
        if (sqlStatement instanceof CreateDatabaseStatement) {
            return PrivilegeType.CREATE_DATABASE;
        }
        if (sqlStatement instanceof CreateTableStatement) {
            return PrivilegeType.CREATE_TABLE;
        }
        if (sqlStatement instanceof CreateFunctionStatement) {
            return PrivilegeType.CREATE_FUNCTION;
        }
        if (sqlStatement instanceof DropTableStatement || sqlStatement instanceof DropDatabaseStatement) {
            return PrivilegeType.DROP;
        }
        if (sqlStatement instanceof TruncateStatement) {
            return PrivilegeType.TRUNCATE;
        }
        return null;
    }
    
    @Override
    public int getOrder() {
        return AuthorityOrder.ORDER;
    }
    
    @Override
    public Class<AuthorityRule> getTypeClass() {
        return AuthorityRule.class;
    }
}
