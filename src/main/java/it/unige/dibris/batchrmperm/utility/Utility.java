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
    private static final Set<String> mostUsedAndDangerousPrivacy = new HashSet<>(21);

    static {
        mostUsedNotDangerousPerms.add("android.permission.RECEIVE_BOOT_COMPLETED");
        mostUsedNotDangerousPerms.add("android.permission.ACCESS_NETWORK_STATE");
        mostUsedNotDangerousPerms.add("android.permission.ACCESS_WIFI_STATE");
        mostUsedNotDangerousPerms.add("android.permission.CHANGE_WIFI_STATE");
        mostUsedNotDangerousPerms.add("android.permission.GET_TASKS");
        mostUsedNotDangerousPerms.add("android.permission.WAKE_LOCK");
        mostUsedNotDangerousPerms.add("android.permission.INTERNET");
        mostUsedNotDangerousPerms.add("android.permission.VIBRATE");

        mostUsedAndDangerousPrivacy.add("android.permission.RECEIVE_BOOT_COMPLETED");
        mostUsedAndDangerousPrivacy.add("android.permission.ACCESS_FINE_LOCATION");
        mostUsedAndDangerousPrivacy.add("android.permission.ACCESS_COARSE_LOCATION");
        mostUsedAndDangerousPrivacy.add("android.permission.WRITE_CONTACTS");
        mostUsedAndDangerousPrivacy.add("android.permission.READ_CONTACTS");
        mostUsedAndDangerousPrivacy.add("android.permission.READ_CALENDAR");
        mostUsedAndDangerousPrivacy.add("android.permission.WRITE_CALENDAR");
        mostUsedAndDangerousPrivacy.add("android.permission.READ_SMS");
        mostUsedAndDangerousPrivacy.add("android.permission.RECEIVE_SMS");
        mostUsedAndDangerousPrivacy.add("android.permission.SEND_SMS");
        mostUsedAndDangerousPrivacy.add("android.permission.RECEIVE_MMS");
        mostUsedAndDangerousPrivacy.add("android.permission.RECEIVE_WAP_PUSH");
        mostUsedAndDangerousPrivacy.add("android.permission.RECORD_AUDIO");
        mostUsedAndDangerousPrivacy.add("android.permission.CAMERA");
        mostUsedAndDangerousPrivacy.add("android.permission.GET_ACCOUNTS");
        mostUsedAndDangerousPrivacy.add("android.permission.READ_PHONE_STATE");
        mostUsedAndDangerousPrivacy.add("android.permission.USE_SIP");
        mostUsedAndDangerousPrivacy.add("android.permission.CALL_PHONE");
        mostUsedAndDangerousPrivacy.add("android.permission.READ_CALL_LOG");
        mostUsedAndDangerousPrivacy.add("android.permission.WRITE_CALL_LOG");
        mostUsedAndDangerousPrivacy.add("android.permission.PROCESS_OUTGOING_CALLS");
    }

    public static Set<String> getMostUsedAndDangerousPrivacy() {
        return mostUsedAndDangerousPrivacy;
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
