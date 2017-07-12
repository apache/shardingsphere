/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.integrate.sql;

public interface DatabaseTestSQL {
    
    String getSelectCountAliasSql();
    
    String getSelectSumAliasSql();
    
    String getSelectMaxAliasSql();
    
    String getSelectMinAliasSql();
    
    String getSelectAvgAliasSql();
    
    String getSelectCountWithBindingTableSql();
    
    String getSelectCountWithBindingTableAndWithoutJoinSql();
    
    String getInsertWithAllPlaceholdersSql();
    
    String getInsertWithPartialPlaceholdersSql();
    
    String getInsertWithoutPlaceholderSql();
    
    String getInsertWithAutoIncrementColumnSql();
    
    String getUpdateWithoutAliasSql();
    
    String getUpdateWithAliasSql();
    
    String getUpdateWithoutShardingValueSql();
    
    String getDeleteWithoutAliasSql();
    
    String getDeleteWithoutShardingValueSql();
    
    String getAssertSelectWithStatusSql();
    
    String getAssertSelectShardingTablesWithStatusSql();
    
    String getSelectSumWithGroupBySql();
    
    String getSelectSumWithOrderByAndGroupBySql();
    
    String getSelectSumWithOrderByDescAndGroupBySql();
    
    String getSelectCountWithGroupBySql();
    
    String getSelectMaxWithGroupBySql();
    
    String getSelectMinWithGroupBySql();
    
    String getSelectAvgWithGroupBySql();
    
    String getSelectEqualsWithSingleTableSql();
    
    String getSelectBetweenWithSingleTableSql();
    
    String getSelectInWithSingleTableSql();
    
    String getSelectOrderByWithAliasSql();
    
    String getSelectPagingWithOffsetAndRowCountSql();
    
    String getSelectPagingWithRowCountSql();
    
    String getSelectPagingWithOffsetSql();
    
    String getSelectLikeWithCountSql();
    
    String getSelectGroupWithBindingTableSql();
    
    String getSelectGroupWithBindingTableAndConfigSql();
    
    String getSelectGroupWithoutGroupedColumnSql();
    
    String getSelectWithNoShardingTableSql();
    
    String getSelectForFullTableNameWithSingleTableSql();
    
    String getSelectWithBindingTableSql();
    
    String getSelectIteratorSql();
    
    String getSelectSubquerySingleTableWithParenthesesSql();
    
    String getSelectSubqueryMultiTableWithParenthesesSql();
    
    String getSelectGroupByUserIdSql();
    
    String getSelectUserIdByStatusSql();
    
    String getSelectUserIdByInStatusSql();
    
    String getSelectUserIdByStatusOrderByUserIdSql();
    
    String getSelectAllOrderSql();
    
    String getSelectUserIdWhereOrderIdInSql();
}
