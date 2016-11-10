package it.unige.dibris.batchrmperm.utility;


import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utility {
    private static final Set<String> mostUsedNotDangerousPerms = new HashSet<>(10);

    static {
        mostUsedNotDangerousPerms.add("android.permission.ACCESS_NETWORK_STATE");
        mostUsedNotDangerousPerms.add("android.permission.RECEIVE_BOOT_COMPLETED");
        mostUsedNotDangerousPerms.add("android.permission.CHANGE_WIFI_STATE");
        mostUsedNotDangerousPerms.add("android.permission.INTERNET");
        mostUsedNotDangerousPerms.add("android.permission.WAKE_LOCK");
        mostUsedNotDangerousPerms.add("android.permission.VIBRATE");
        mostUsedNotDangerousPerms.add("android.permission.READ_PHONE_STATE");
        mostUsedNotDangerousPerms.add("android.permission.GET_TASKS");
        mostUsedNotDangerousPerms.add("android.permission.ACCESS_WIFI_STATE");
        mostUsedNotDangerousPerms.add("android.permission.GET_TASKS");
    }

    public static Set<String> getMostUsedNotDangerousPerms() {
        return mostUsedNotDangerousPerms;
    }

    public static List<Path> listApkFiles(Path dir) throws IOException {
        List<Path> result = new ArrayList<>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{apk}");
        for (Path entry: stream) {
            result.add(entry);
        }
        return result;
    }

}
