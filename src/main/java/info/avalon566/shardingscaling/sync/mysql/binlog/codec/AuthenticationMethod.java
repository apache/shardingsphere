package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

/**
 * MySQL client/server protocol Authentication Method
 *
 * MySQL Internals Manual  /  MySQL Client/Server Protocol  /  Authentication Method  /  SHA256
 * https://dev.mysql.com/doc/internals/en/sha256.html
 *
 * @author yangyi
 */
public final class AuthenticationMethod {
    
    public static final String OLD_PASSWORD_AUTHENTICATION = "mysql_old_password";
    
    public static final String SECURE_PASSWORD_AUTHENTICATION = "mysql_native_password";
    
    public static final String CLEAR_TEXT_AUTHENTICATION = "mysql_clear_password";
    
    public static final String WINDOWS_NATIVE_AUTHENTICATION = "authentication_windows_client";
    
    public static final String SHA256 = "sha256_password";
}
