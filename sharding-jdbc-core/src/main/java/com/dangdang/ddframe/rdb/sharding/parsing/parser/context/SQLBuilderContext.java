package com.dangdang.ddframe.rdb.sharding.parsing.parser.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL构建器上下文.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class SQLBuilderContext {
    
    private final String originalSQL;
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    /**
     * 生成SQL构建器.
     *
     * @param tables 表对象
     * @return SQL构建器
     */
    public SQLBuilder toSqlBuilder(final List<TableContext> tables) {
        SQLBuilder result = new SQLBuilder();
        if (sqlTokens.isEmpty()) {
            result.append(originalSQL);
            return result;
        }
        int count = 0;
        Collections.sort(sqlTokens, new Comparator<SQLToken>() {
            
            @Override
            public int compare(final SQLToken o1, final SQLToken o2) {
                return o1.getBeginPosition() - o2.getBeginPosition();
            }
        });
        for (SQLToken each : sqlTokens) {
            if (0 == count) {
                result.append(originalSQL.substring(0, each.getBeginPosition()));
            }
            if (each instanceof TableToken) {
                String tableName = ((TableToken) each).getTableName();
                boolean found = false;
                for (TableContext tableContext : tables) {
                    if (tableContext.getName().equalsIgnoreCase(tableName)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    result.appendToken(tableName);
                    int beginPosition = each.getBeginPosition() + ((TableToken) each).getOriginalLiterals().length();
                    int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                    result.append(originalSQL.substring(beginPosition, endPosition));
                } else {
                    result.append(((TableToken) each).getOriginalLiterals());
                    int beginPosition = each.getBeginPosition() + ((TableToken) each).getOriginalLiterals().length();
                    int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                    result.append(originalSQL.substring(beginPosition, endPosition));
                }
            } else if (each instanceof ItemsToken) {
                for (String item : ((ItemsToken) each).getItems()) {
                    result.append(", ");
                    result.append(item);
                }
                int beginPosition = each.getBeginPosition();
                int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                result.append(originalSQL.substring(beginPosition, endPosition));
            } else if (each instanceof RowCountLimitToken) {
                result.appendToken(RowCountLimitToken.COUNT_NAME, ((RowCountLimitToken) each).getRowCount() + "");
                int beginPosition = each.getBeginPosition() + (((RowCountLimitToken) each).getRowCount() + "").length();
                int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                result.append(originalSQL.substring(beginPosition, endPosition));
            } else if (each instanceof OffsetLimitToken) {
                result.appendToken(OffsetLimitToken.OFFSET_NAME, ((OffsetLimitToken) each).getOffset() + "");
                int beginPosition = each.getBeginPosition() + (((OffsetLimitToken) each).getOffset() + "").length();
                int endPosition = sqlTokens.size() - 1 == count ? originalSQL.length() : sqlTokens.get(count + 1).getBeginPosition();
                result.append(originalSQL.substring(beginPosition, endPosition));
            }
            count++;
        }
        return result;
    }
}
