package it.unige.dibris.batchrmperm.service;


import it.saonzo.rmperm.RmPermissions;
import it.unige.dibris.batchrmperm.BatchRmPermApplication;
import it.unige.dibris.batchrmperm.domain.Apk;
import it.unige.dibris.batchrmperm.domain.ApkCustom;
import it.unige.dibris.batchrmperm.domain.ApkOriginal;
import it.unige.dibris.batchrmperm.domain.Permission;
import it.unige.dibris.batchrmperm.engine.ExecuteCmd;
import it.unige.dibris.batchrmperm.repository.ApkCustomRepository;
import it.unige.dibris.batchrmperm.repository.ApkOriginalRepository;

import it.unige.dibris.batchrmperm.repository.PermissionRepository;
import it.unige.dibris.batchrmperm.utility.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BatchWork {
    //private static final String APKS_FOLDER = "/media/simo/HDEsterno/ApkSamples/";
    //private static final String APKS_FOLDER = "/home/simo/AndroidStudioProjects/MyApplication/app/build/outputs/apk";
    //private static final File FAILS_FOLDER = new File(APKS_FOLDER, "Fails");
    private static final String dexWithCustomMethods = "/home/simo/IdeaProjects/BatchRmPerm/src/main/resources/custom.dex";

    private Pattern pattern = Pattern.compile("^Failure \\[(\\w*)\\]$");

    @Autowired
    ApkCustomRepository apkCustomRepository;

    @Autowired
    ApkOriginalRepository apkOriginalRepository;

    @Autowired
    PermissionRepository permissionRepository;


    @Async
    public void doTheWork(String device) throws IOException {
        String APKS_FOLDER = null;

        switch (device) {
            case "41008d8247677000":
                APKS_FOLDER = "/media/simo/HDEsterno/AApks/aptoide";
                break;
            case "F9NPFX069627":
                APKS_FOLDER = "/media/simo/HDEsterno/AApks/uptodown";
                break;
        }
        File RMPERM_FOLDER = new File(APKS_FOLDER, "rmperm");
        //File FAILS_FOLDER = new File(RMPERM_FOLDER, "fail");


        System.out.println("--> Start thread=" + Thread.currentThread().getName());
        ExecuteCmd executeCmd = new ExecuteCmd(device);
        try {
            RMPERM_FOLDER.mkdir();
            //FAILS_FOLDER.mkdir();
            if (ExecuteCmd.devicesAttached().isEmpty()) {
                ExecuteCmd.startEmulator();
            }
        } catch (Exception e) { // unsatisfied requirement
            e.printStackTrace();
            BatchRmPermApplication.close();
        }

        List<Path> apksInFolder = Utility.listApkFiles(Paths.get(APKS_FOLDER));
        for (Path apkPath : apksInFolder) {
            try {
                System.out.println("--> Start apk=" + apkPath.toString());
                ApkOriginal apkOriginal = new ApkOriginal(apkPath);

                RmPermConsole console = new RmPermConsole();
                File customizedApk = new File(RMPERM_FOLDER, "tmpApkFile.apk");
                Duration deltaTime = Duration.ZERO;
                Instant beginTime = Instant.now();
                RmPermissions rmPerm = new RmPermissions(console, Utility.getMostUsedAndDangerousPrivacy(),
                        apkOriginal.getPath().toString(), customizedApk.toString(), dexWithCustomMethods);
                rmPerm.removePermissions();
                deltaTime = Duration.between(beginTime, Instant.now());
                ApkCustom apkCustom = new ApkCustom(apkOriginal, customizedApk, deltaTime.getSeconds());

                tryToInstall(executeCmd, apkCustom);
                if (apkCustom.isInstallSuccess()) {
                    apkCustom.setMonkeyResult(executeCmd.testMonkey(apkCustom.getPackName())); // dyn test on custom apk
                    if (apkCustom.isMonkeyCrash()) {
                        tryToInstall(executeCmd, apkOriginal);
                        if (apkOriginal.isInstallSuccess()) {
                            apkOriginal.setMonkeyResult(executeCmd.testMonkey(apkOriginal.getPackName())); // dyn test on original apk

                        }
                    }
                }
                else {
                    tryToInstall(executeCmd, apkOriginal);
                }
                executeCmd.returnToHomeScreen();
                executeCmd.uninstallApk(apkOriginal.getPackName());

                setPermissionFromDb(apkOriginal);
                setPermissionFromDb(apkCustom);
                apkCustomRepository.save(apkCustom);


                System.out.println("<-- End apk=" + apkPath.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("<-- End thread=" + Thread.currentThread().getName());
    }


    private synchronized void tryToInstall(ExecuteCmd executeCmd, Apk apk) throws IOException, InterruptedException {
        List<String> output = executeCmd.installApk(apk.getPath().toString());
        if (output.isEmpty()) {
            System.err.println("No install output!");
            return;
        }
        String last = output.get(output.size() - 1);
        if (last.equals("Success")) {
            apk.setInstallSuccess(true);
            System.out.println(Arrays.toString(output.toArray()));
        } else {
            apk.setInstallSuccess(false);
            System.err.println(Arrays.toString(output.toArray()));
            Matcher matcher = pattern.matcher(last);
            if (matcher.matches()) {
                apk.setInstallFailReason(matcher.group(1));
            }
        }
    }

    private synchronized void setPermissionFromDb(Apk apk) {
        Set<String> apkTmpSet = apk.getTmpPermSet();
        HashSet<Permission> out = new HashSet<>(apkTmpSet.size());
        for (String permStr : apkTmpSet) {
            Permission p = permissionRepository.findOne(permStr);
            if (p == null) {
                p = new Permission(permStr);
                permissionRepository.save(p);
            }
            out.add(p);
        }
        apk.setPermissions(out);
    }

}