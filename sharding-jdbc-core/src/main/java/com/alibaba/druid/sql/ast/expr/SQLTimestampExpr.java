package com.alibaba.druid.sql.ast.expr;

import com.alibaba.druid.sql.ast.SQLExprImpl;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class SQLTimestampExpr extends SQLExprImpl {
    
    private String literal;
    
    private String timeZone;
    
    private boolean withTimeZone;
}
