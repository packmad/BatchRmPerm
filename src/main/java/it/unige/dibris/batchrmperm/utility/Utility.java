package it.unige.dibris.batchrmperm.utility;


import it.unige.dibris.batchrmperm.domain.Permission;
import it.unige.dibris.batchrmperm.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utility {
    private static final Set<String> dangerousPermissions = new HashSet<>(24);

    static {
        dangerousPermissions.add("android.permission.WRITE_CONTACTS");
        dangerousPermissions.add("android.permission.ACCESS_FINE_LOCATION");
        dangerousPermissions.add("android.permission.RECORD_AUDIO");
        dangerousPermissions.add("android.permission.READ_CALL_LOG");
        dangerousPermissions.add("android.permission.READ_EXTERNAL_STORAGE");
        dangerousPermissions.add("android.permission.CALL_PHONE");
        dangerousPermissions.add("android.permission.CAMERA");
        dangerousPermissions.add("android.permission.READ_CONTACTS");
        dangerousPermissions.add("android.permission.PROCESS_OUTGOING_CALLS");
        dangerousPermissions.add("android.permission.READ_SMS");
        dangerousPermissions.add("android.permission.WRITE_EXTERNAL_STORAGE");
        dangerousPermissions.add("android.permission.WRITE_CALL_LOG");
        dangerousPermissions.add("android.permission.ACCESS_COARSE_LOCATION");
        dangerousPermissions.add("android.permission.WRITE_CALENDAR");
        dangerousPermissions.add("android.permission.RECEIVE_MMS");
        dangerousPermissions.add("android.permission.READ_CALENDAR");
        dangerousPermissions.add("android.permission.USE_SIP");
        dangerousPermissions.add("android.permission.READ_PHONE_STATE");
        dangerousPermissions.add("android.permission.SEND_SMS");
        dangerousPermissions.add("com.android.voicemail.permission.ADD_VOICEMAIL");
        dangerousPermissions.add("android.permission.BODY_SENSORS");
        dangerousPermissions.add("android.permission.RECEIVE_WAP_PUSH");
        dangerousPermissions.add("android.permission.GET_ACCOUNTS");
        dangerousPermissions.add("android.permission.RECEIVE_SMS");
    }

    @Autowired
    PermissionRepository permissionRepository;

    public static Set<String> getDangerousPermissions() {
        return dangerousPermissions;
    }

    public static List<Path> listApkFiles(Path dir) throws IOException {
        List<Path> result = new ArrayList<>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{apk}");
        for (Path entry: stream) {
            result.add(entry);
        }
        return result;
    }

    public static Set<Permission> createPermissionSet(List<String> permList) {
        HashSet<Permission> out = new HashSet<>(permList.size());
        for(String s : permList) {
            Permission p = new Permission(s);
            out.add(p);
        }
        return out;
    }
}
