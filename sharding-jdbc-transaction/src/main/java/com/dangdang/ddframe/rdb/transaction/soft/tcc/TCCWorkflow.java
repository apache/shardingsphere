package com.dangdang.ddframe.rdb.transaction.soft.tcc;

/**
 * TCC工作流.
 * 
 * @author zhangliang 
 */
public enum TCCWorkflow {

    /**
     * 尝试执行业务.
     * 
     * <p>完成所有业务检查（一致性）</p>
     * <p>预留必须业务资源（准隔离性）</p>
     */
    Try,

    /**
     * 确认执行业务.
     * 
     * <p>真正执行业务</p>
     * <p>不作任何业务检查</p>
     * <p>只使用Try阶段预留的业务资源</p>
     * <p>Confirm操作满足幂等性</p>
     */
    Confirm,

    /**
     * 取消执行业务.
     * 
     * <p>释放Try阶段预留的业务资源</p>
     * <p>Cancel操作满足幂等性</p>
     */
    Cancel
}
