package info.avalon566.shardingscaling.sync.mysql.binlog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author avalon566
 */
public class MySQLPasswordEncryptor {
    public static final byte[] scramble411(byte[] pass, byte[] seed) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] pass1 = md.digest(pass);
        md.reset();
        byte[] pass2 = md.digest(pass1);
        md.reset();
        md.update(seed);
        byte[] pass3 = md.digest(pass2);
        for (int i = 0; i < pass3.length; i++) {
            pass3[i] = (byte) (pass3[i] ^ pass1[i]);
        }
        return pass3;
    }
}
