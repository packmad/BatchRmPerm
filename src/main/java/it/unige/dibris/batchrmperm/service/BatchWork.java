package it.unige.dibris.batchrmperm.service;


import it.saonzo.rmperm.RmPermissions;
import it.unige.dibris.batchrmperm.BatchRmPermApplication;
import it.unige.dibris.batchrmperm.domain.Apk;
import it.unige.dibris.batchrmperm.engine.ExecuteCmd;
import it.unige.dibris.batchrmperm.repository.ApkRepository;

import it.unige.dibris.batchrmperm.utility.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.io.*;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BatchWork {
    //private static final String APKS_FOLDER = "/media/simo/HDEsterno/ApkSamples/";
    //private static final String APKS_FOLDER = "/home/simo/AndroidStudioProjects/MyApplication/app/build/outputs/apk";
    //private static final File FAILS_FOLDER = new File(APKS_FOLDER, "Fails");
    private static final String dexWithCustomMethods = "/home/simo/IdeaProjects/BatchRmPerm/src/main/resources/custom.dex";

    @Autowired
    ApkRepository apkRepository;


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
        File FAILS_FOLDER = new File(RMPERM_FOLDER, "fail");
        System.out.println("--> Start thread=" + Thread.currentThread().getName());
        ExecuteCmd executeCmd = new ExecuteCmd(device);
        try {
            RMPERM_FOLDER.mkdir();
            FAILS_FOLDER.mkdir();
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
                File outApk = new File(RMPERM_FOLDER, apkPath.getFileName().toString());
                RmPermConsole console = new RmPermConsole();

                System.out.println("--> Start rmperm=" + apkPath.toString());
                RmPermissions rmPerm = new RmPermissions(console, Utility.getMostUsedAndDangerousPrivacy(),
                        apkPath.toString(), outApk.toString(), dexWithCustomMethods);
                rmPerm.removePermissions();

                System.out.println("<-- End rmperm=" + apkPath.toString());

                Apk apk = new Apk(outApk.toPath(), String.join("§", console.getConsoleOutput()));
                List<String> output = executeCmd.installApk(apk.getPath().toString());
                if (output.isEmpty()) {
                    System.err.println(apk);
                    continue;
                }
                String last = output.get(output.size() - 1);
                if (!output.isEmpty() && last.equals("Success")) {
                    System.out.println(Arrays.toString(output.toArray()));
                    apk.setInstallSuccess(true);
                } else {
                    System.err.println(Arrays.toString(output.toArray()));
                    Pattern pattern = Pattern.compile("^Failure \\[(\\w*)\\]$");
                    Matcher matcher = pattern.matcher(last);
                    if (matcher.matches()) {
                        String reason = matcher.group(1);

                        /*
                        if (reason.equals("INSTALL_FAILED_INSUFFICIENT_STORAGE")) {
                            adbRebootEmulator();
                        }
                        */

                        System.err.println(reason);
                        apk.setInstallSuccess(false);
                        apk.setMonkeyOutput(reason);
                        java.nio.file.Files.move(apk.getPath(), Paths.get(FAILS_FOLDER.toString(), apk.getPath().getFileName().toString()));
                    }
                }
                apk.setMonkeyOutput(executeCmd.testMonkey(apk.getPackName()));
                apkRepository.save(apk);
                executeCmd.returnToHomeScreen();
                executeCmd.uninstallApk(apk.getPackName());
                System.out.println("<-- End apk=" + apkPath.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("<-- End thread=" + Thread.currentThread().getName());
    }


}