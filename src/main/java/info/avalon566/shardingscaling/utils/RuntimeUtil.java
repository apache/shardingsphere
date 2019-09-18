package info.avalon566.shardingscaling.utils;

import info.avalon566.shardingscaling.Engine;

import java.io.File;

/**
 * @author avalon566
 */
public final class RuntimeUtil {

    public static String getBasePath() {
        return new File(Engine.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
    }
}
