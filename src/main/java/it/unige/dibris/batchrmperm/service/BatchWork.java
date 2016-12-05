package it.unige.dibris.batchrmperm.service;


import it.saonzo.rmperm.RmPermissions;
import it.unige.dibris.batchrmperm.BatchRmPermApplication;
import it.unige.dibris.batchrmperm.domain.*;
import it.unige.dibris.batchrmperm.engine.ExecuteCmd;
import it.unige.dibris.batchrmperm.repository.ApkCustomRepository;
import it.unige.dibris.batchrmperm.repository.ApkOriginalRepository;
import it.unige.dibris.batchrmperm.repository.PermissionRepository;
import it.unige.dibris.batchrmperm.utility.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static final String dexWithCustomMethods = "/home/simo/IdeaProjects/BatchRmPerm/src/main/resources/custom.dex"; //TODO
    @Autowired
    private ApkCustomRepository apkCustomRepository;
    @Autowired
    private ApkOriginalRepository apkOriginalRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    private Pattern pattern = Pattern.compile("^Failure \\[(\\w*)\\]$");

    @Async
    public void doTheWork(Device device) throws IOException {
        System.out.println("--> Start thread=" + Thread.currentThread().getName());

        String apksFolder = device.getFolder().toString();
        File customizedFolder = new File(apksFolder, "custom");
        ExecuteCmd executeCmd = new ExecuteCmd(device);
        try {
            customizedFolder.mkdir();
            if (ExecuteCmd.devicesAttached().isEmpty()) {
                ExecuteCmd.startEmulator();
            }
        } catch (Exception e) { // unsatisfied requirement
            e.printStackTrace();
            BatchRmPermApplication.close();
        }

        List<Path> apksInFolder = Utility.listApkFiles(Paths.get(apksFolder));
        for (Path apkPath : apksInFolder) {
            try {
                System.out.println("--> Start apk=" + apkPath.toString());
                ApkOriginal apkOriginal = new ApkOriginal(apkPath);

                RmPermConsole console = new RmPermConsole();
                File customizedApk = new File(customizedFolder, "tmpApkFile.apk");
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