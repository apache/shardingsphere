package io.shardingjdbc.core.constant;

/**
 * Created by wangqisen on 2017/12/19.
 * <p>
 * 条件关系类型
 */
public enum ConditionRelationType {

    // 单一条件对象没有关系类型
    NONE("","无关系"),
    AND("&", "与关系"),
    OR("|", "或关系");

    private String conditionRelation;
    private String conditionRelationDesc;

    ConditionRelationType(String conditionRelation, String conditionRelationDesc) {
        this.conditionRelation = conditionRelation;
        this.conditionRelationDesc = conditionRelationDesc;
    }
}
