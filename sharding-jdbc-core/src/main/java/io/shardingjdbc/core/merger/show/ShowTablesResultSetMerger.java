package io.shardingjdbc.core.merger.show;

import com.google.common.base.Optional;
import io.shardingjdbc.core.constant.ShardingConstant;
import io.shardingjdbc.core.merger.common.AbstractMemoryResultSetMerger;
import io.shardingjdbc.core.merger.common.MemoryResultSetRow;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Show tables result set merger.
 *
 * @author zhangliang
 */
public final class ShowTablesResultSetMerger extends AbstractMemoryResultSetMerger {
    
    private static final Map<String, Integer> LABEL_AND_INDEX_MAP = new HashMap<>(1, 1);
    
    private final ShardingRule shardingRule;
    
    private final Iterator<MemoryResultSetRow> memoryResultSetRows;
    
    private final Set<String> tableNames = new HashSet<>();
    
    static {
        LABEL_AND_INDEX_MAP.put("Tables_in_" + ShardingConstant.LOGIC_SCHEMA_NAME, 1); 
    }
    
    public ShowTablesResultSetMerger(final ShardingRule shardingRule, final List<ResultSet> resultSets) throws SQLException {
        super(LABEL_AND_INDEX_MAP);
        this.shardingRule = shardingRule;
        memoryResultSetRows = init(resultSets);
    }
    
    private Iterator<MemoryResultSetRow> init(final List<ResultSet> resultSets) throws SQLException {
        List<MemoryResultSetRow> result = new LinkedList<>();
        for (ResultSet each : resultSets) {
            while (each.next()) {
                MemoryResultSetRow memoryResultSetRow = new MemoryResultSetRow(each);
                String actualTableName = memoryResultSetRow.getCell(1).toString();
                Optional<TableRule> tableRule = shardingRule.tryFindTableRuleByActualTable(actualTableName);
                if (!tableRule.isPresent()) {
                    result.add(memoryResultSetRow);
                } else if (tableNames.add(tableRule.get().getLogicTable())) {
                    memoryResultSetRow.setCell(1, tableRule.get().getLogicTable());
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
