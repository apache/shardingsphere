package org.apache.shardingsphere.infra.connection.util;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;

import java.util.Collection;

/**
 * ShardingSphere meta data validate utility class.
 */
@NoArgsConstructor
public final class SQLStatementTransparentUtils {

    public static boolean isTransparentStatement(final ShardingSphereRuleMetaData ruleMetaData, final SQLStatementContext sqlStatementContext) {
        Collection<TableContainedRule> tableContainedRules = ruleMetaData.findRules(TableContainedRule.class);
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            for (TableContainedRule tableContainedRule : tableContainedRules) {
                if (tableContainedRule.getEnhancedTableMapper().contains(each.toLowerCase())) {
                    return false;
                }
            }
        }
        return true;
    }
}
