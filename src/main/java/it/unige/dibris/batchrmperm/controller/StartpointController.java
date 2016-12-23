package it.unige.dibris.batchrmperm.controller;


import it.unige.dibris.batchrmperm.domain.ApkCustom;
import it.unige.dibris.batchrmperm.domain.ApkOriginal;
import it.unige.dibris.batchrmperm.domain.Device;
import it.unige.dibris.batchrmperm.domain.Permission;
import it.unige.dibris.batchrmperm.domain.result.DangerousPermissionCounter;
import it.unige.dibris.batchrmperm.domain.result.PermissionCounter;
import it.unige.dibris.batchrmperm.domain.result.PermissionFreq;
import it.unige.dibris.batchrmperm.domain.result.SpeedPoint;
import it.unige.dibris.batchrmperm.engine.ExecuteCmd;
import it.unige.dibris.batchrmperm.repository.ApkCustomRepository;
import it.unige.dibris.batchrmperm.service.BatchWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@EnableAsync
@RestController
public class StartpointController {


    @Autowired
    ApkCustomRepository apkCustomRepository;

    @Autowired
    BatchWork batchWork;


    @RequestMapping(value = "start/{device}", method = RequestMethod.GET)
    public ResponseEntity<?> startTheWork(@PathVariable String device) {
        try {
            for (String dev : ExecuteCmd.devicesAttached()) {
                if (dev.equals(device)) {
                    batchWork.doTheWork(new Device(device));
                    return new ResponseEntity<>(HttpStatus.OK);
                }
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "getResult", method = RequestMethod.GET)
    public ResponseEntity<?> getResult() {
        int customInstallSuccess = 0;
        int customInstallFail = 0;
        int customInstallSuccessButMonkeyCrash = 0;
        int customMonkeyCrashAndOriginalMonkeyCrash = 0;
        int customMonkeyCrashButOriginalMonkeySuccess = 0;
        int customInstallSuccessAndMonkeySuccess = 0;
        int customInstallFailButOriginalInstallSuccess = 0;
        int customInstallFailAndOriginalInstallFail = 0;
        int noRedirect = 0;
        double totApks = 0.0;


        System.out.println("---> Start getting results");

        Iterable<ApkCustom> customApks = apkCustomRepository.findAll();

        for (ApkCustom apkCustom : customApks) {
            totApks++;

            ApkOriginal apkOriginal = apkCustom.getApkOriginal();


            if (apkCustom.isInstallSuccess()) {
                customInstallSuccess++;

                if (apkCustom.isMonkeyCrash()) {
                    customInstallSuccessButMonkeyCrash++;


                    if (apkOriginal.isMonkeyCrash()) {
                        customMonkeyCrashAndOriginalMonkeyCrash++;
                    } else {
                        customMonkeyCrashButOriginalMonkeySuccess++;
                    }
                } else {
                    customInstallSuccessAndMonkeySuccess++;
                }

                if (apkCustom.getRmPermOutput().contains("but I don't have a redirection for it")) {
                    noRedirect++;
                }

            } else {
                customInstallFail++;
                if (apkOriginal.isInstallSuccess()) {
                    customInstallFailButOriginalInstallSuccess++;
                } else {
                    customInstallFailAndOriginalInstallFail++;
                }
            }
        }
        int installableApks = (int) (totApks - customInstallFailAndOriginalInstallFail);
        double installSuccess = customInstallSuccess * 100.0 / installableApks;

        System.out.println("<--- End getting results");
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "generateDat", method = RequestMethod.GET)
    public ResponseEntity<?> generateDat() {
        try {
            File dangerousDistribution = new File("/home/simo/gnuplotest/permfail/dangerous.dat");
            if (dangerousDistribution.exists())
                dangerousDistribution.delete();
            DangerousPermissionCounter dangerousPermissionCounter = new DangerousPermissionCounter();

            File speedPc = new File("/home/simo/gnuplotest/speed/speed_pc.dat");
            if (speedPc.exists())
                speedPc.delete();
            List<SpeedPoint> speedPoints = new ArrayList<SpeedPoint>();

            File permDistrib = new File("/home/simo/gnuplotest/permDistrib/permDistrib.dat");
            if (permDistrib.exists())
                permDistrib.delete();
            PermissionCounter permissionCounter = new PermissionCounter();


            System.out.println("---> Start generating dat");

            Iterable<ApkCustom> customApks = apkCustomRepository.findAll();

            for (ApkCustom apkCustom : customApks) {

                for (Permission p : apkCustom.getApkOriginal().getPermissions()) {
                    dangerousPermissionCounter.increment(p.getPermissionName());
                    permissionCounter.count(p.getPermissionName());
                }
                dangerousPermissionCounter.incTotApks();
                permissionCounter.incTotApks();

                if (apkCustom.getApkOriginal().getDexSize() > 0.0 && apkCustom.getDexSize() > 0.0) {
                    speedPoints.add(new SpeedPoint(apkCustom.getRemovalTimeNanoSec(), apkCustom.getApkOriginal().getDexSize()));
                }

            }

            PrintWriter pw;

            pw = new PrintWriter(dangerousDistribution);
            pw.println("perm freq");
            for (PermissionFreq pf : dangerousPermissionCounter.getOrderedListByFreq()) {
                pw.println(pf);
            }
            pw.close();

            pw = new PrintWriter(permDistrib);
            pw.println("perm freq");
            for (PermissionFreq pf : permissionCounter.getOrderedListByFreq()) {
                pw.println(pf);
            }
            pw.close();

            Collections.sort(speedPoints);
            pw = new PrintWriter(speedPc);
            for (SpeedPoint sp : speedPoints)
                pw.println(sp);
            pw.close();

            System.out.println("<--- End generating dat");
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "generateTable", method = RequestMethod.GET)
    public ResponseEntity<?> generateTable() {

        List<Integer> sumBefore = new ArrayList<>();
        List<Integer> sumAfter = new ArrayList<>();
        List<Integer> sumDelta = new ArrayList<>();
        List<Double> sumApkSizeRatio = new ArrayList<>();
        List<Double> sumDexSizeRatio = new ArrayList<>();
        List<Double> sumRemovalTime = new ArrayList<>();

        Iterable<ApkCustom> customApks = apkCustomRepository.findAll();
        double totApks = 0.0;

        for (ApkCustom apkCustom : customApks) {
            if (!apkCustom.isInstallSuccess() && !apkCustom.getApkOriginal().isInstallSuccess())
                continue; // non-relevant
            totApks++;
            String packageName = apkCustom.getPackName();

            int before = apkCustom.getApkOriginal().getPermissions().size();
            sumBefore.add(before);

            int after = apkCustom.getPermissions().size();
            sumAfter.add(after);

            int delta = before - after;

            if (delta >= 0 && delta < 25) { // we have few (12) apps with strange delta :(
                sumDelta.add(delta);
            }

            double apkSizeRatio = apkCustom.getApkOriginal().getFileSize() / apkCustom.getFileSize();
            if (!Double.isNaN(apkSizeRatio))
                sumApkSizeRatio.add(apkSizeRatio);

            double dexSizeRatio = apkCustom.getApkOriginal().getDexSize() / apkCustom.getDexSize();
            if (!Double.isNaN(dexSizeRatio)) {
                sumDexSizeRatio.add(dexSizeRatio);
            }

            double removalTime = apkCustom.getRemovalTimeNanoSec() / 1000000000.0;
            if (!Double.isNaN(removalTime))
                sumRemovalTime.add(removalTime);

            boolean install = apkCustom.isInstallSuccess();
            boolean monkey = apkCustom.isMonkeyCrash();
            /*
            System.out.println(String.format("%s & %d & %d & %d & %.2f & %.2f & %.2f & %b & %b",
                    packageName, before, after, delta, apkSizeRatio, dexSizeRatio, removalTime, install, monkey));
                    */

        }

        System.out.println("--- ");

        double avgBefore = sumBefore.stream().mapToDouble(a -> a).average().getAsDouble();
        double avgAfter = sumAfter.stream().mapToDouble(a -> a).average().getAsDouble();
        double avgDelta = sumDelta.stream().mapToDouble(a -> a).average().getAsDouble();
        double avgApkSizeRatio = sumApkSizeRatio.stream().mapToDouble(a -> a).average().getAsDouble();
        double avgDexSizeRatio = sumDexSizeRatio.stream().mapToDouble(a -> a).average().getAsDouble();
        double avgRemovalTime = sumRemovalTime.stream().mapToDouble(a -> a).average().getAsDouble();

        int maxBefore = Collections.max(sumBefore);
        int minBefore = Collections.min(sumBefore);

        int maxAfter = Collections.max(sumAfter);
        int minAfter = Collections.min(sumAfter);

        int maxDelta = Collections.max(sumDelta);
        int minDelta = Collections.min(sumDelta);

        double maxApkSizeRatio = Collections.max(sumApkSizeRatio);
        double minApkSizeRatio = Collections.min(sumApkSizeRatio);

        double maxDexSizeRatio = Collections.max(sumDexSizeRatio);
        double minDexSizeRatio = Collections.min(sumDexSizeRatio);


        List<Double> sumPowBefore = new ArrayList<>();
        List<Double> sumPowAfter = new ArrayList<>();
        List<Double> sumPowDelta = new ArrayList<>();
        List<Double> sumPowApkSizeRatio = new ArrayList<>();
        List<Double> sumPowDexSizeRatio = new ArrayList<>();
        List<Double> sumPowRemovalTime = new ArrayList<>();
        for (ApkCustom apkCustom : customApks) {
            int before = apkCustom.getApkOriginal().getPermissions().size();
            sumPowBefore.add(Math.pow(before - avgBefore, 2));

            int after = apkCustom.getPermissions().size();
            sumPowAfter.add(Math.pow(after - avgAfter, 2));

            int delta = before - after;
            sumPowDelta.add(Math.pow(delta - avgDelta, 2));

            double apkSizeRatio = apkCustom.getApkOriginal().getFileSize() / apkCustom.getFileSize();
            if (!Double.isNaN(apkSizeRatio))
                sumPowApkSizeRatio.add(Math.pow(apkSizeRatio - avgApkSizeRatio, 2));

            double dexSizeRatio = apkCustom.getApkOriginal().getDexSize() / apkCustom.getDexSize();
            if (!Double.isNaN(dexSizeRatio))
                sumPowDexSizeRatio.add(Math.pow(dexSizeRatio - avgDexSizeRatio, 2));

            double removalTime = apkCustom.getRemovalTimeNanoSec() / 1000000000.0;
            if (!Double.isNaN(removalTime))
                sumPowRemovalTime.add(Math.pow(removalTime - avgRemovalTime, 2));
        }

        double stdBefore = Math.sqrt(sumPowBefore.stream().mapToDouble(a -> a).average().getAsDouble());
        double stdAfter = Math.sqrt(sumPowAfter.stream().mapToDouble(a -> a).average().getAsDouble());
        double stdDelta = Math.sqrt(sumPowDelta.stream().mapToDouble(a -> a).average().getAsDouble());
        double stdApkSizeRatio = Math.sqrt(sumPowApkSizeRatio.stream().mapToDouble(a -> a).average().getAsDouble());
        double stdDexSizeRatio = Math.sqrt(sumPowDexSizeRatio.stream().mapToDouble(a -> a).average().getAsDouble());
        double stdRemovalTime = Math.sqrt(sumPowRemovalTime.stream().mapToDouble(a -> a).average().getAsDouble());

        return new ResponseEntity<>(HttpStatus.OK);
    }


}
