package io.shardingjdbc.core.merger.show;

import com.google.common.base.Optional;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.merger.common.AbstractMemoryResultSetMerger;
import io.shardingjdbc.core.merger.common.MemoryResultSetRow;
import io.shardingjdbc.core.parsing.SQLParsingEngine;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Show create table result set merger.
 *
 * @author zhangliang
 */
public final class ShowCreateTableResultSetMerger extends AbstractMemoryResultSetMerger {
    
    private static final Map<String, Integer> LABEL_AND_INDEX_MAP = new HashMap<>(2, 1);
    
    private final ShardingRule shardingRule;
    
    private final Iterator<MemoryResultSetRow> memoryResultSetRows;
    
    static {
        LABEL_AND_INDEX_MAP.put("Table", 1);
        LABEL_AND_INDEX_MAP.put("Create Table", 2);
    }
    
    public ShowCreateTableResultSetMerger(final ShardingRule shardingRule, final List<ResultSet> resultSets) throws SQLException {
        super(LABEL_AND_INDEX_MAP);
        this.shardingRule = shardingRule;
        memoryResultSetRows = init(resultSets);
    }
    
    private Iterator<MemoryResultSetRow> init(final List<ResultSet> resultSets) throws SQLException {
        List<MemoryResultSetRow> result = new LinkedList<>();
        for (ResultSet each : resultSets) {
            while (each.next()) {
                MemoryResultSetRow memoryResultSetRow = new MemoryResultSetRow(each);
                String tableName = memoryResultSetRow.getCell(1).toString();
                Optional<TableRule> tableRule = shardingRule.tryFindTableRuleByActualTable(tableName);
                if (tableRule.isPresent()) {
                    String logicTableName = tableRule.get().getLogicTable();
                    memoryResultSetRow.setCell(1, logicTableName);
                    String createTableDDL = memoryResultSetRow.getCell(2).toString();
                    SQLParsingEngine sqlParsingEngine = new SQLParsingEngine(DatabaseType.MySQL, createTableDDL, shardingRule);
                    String actualTableName = sqlParsingEngine.parse().getTables().getSingleTableName();
                    if (actualTableName.startsWith("`")) {
                        logicTableName = "`" + logicTableName + "`";
                    }
                    memoryResultSetRow.setCell(2, createTableDDL.replaceFirst(actualTableName, logicTableName));
                    result.add(memoryResultSetRow);
                }
            }
        }
        if (!result.isEmpty()) {
            setCurrentResultSetRow(result.get(0));
        }
        return result.iterator();
    }
    
    @Override
    public boolean next() throws SQLException {
        if (memoryResultSetRows.hasNext()) {
            setCurrentResultSetRow(memoryResultSetRows.next());
            return true;
        }
        return false;
    }
}
