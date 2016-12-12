package com.alibaba.druid.sql.lexer;

/**
 * 符号未正确结束的异常.
 *
 * @author zhangliang
 */
public final class UnterminatedSignException extends RuntimeException {
    
    private static final long serialVersionUID = 8575890835166900925L;
    
    private static String MESSAGE = "Illegal input, unterminated '%s'.";
    
    public UnterminatedSignException(final char terminatedSign) {
        super(String.format(MESSAGE, terminatedSign));
    }
    
    public UnterminatedSignException(final String terminatedSign) {
        super(String.format(MESSAGE, terminatedSign));
    }
}
