package it.unige.dibris.batchrmperm.controller;


import it.unige.dibris.batchrmperm.domain.ApkCustom;
import it.unige.dibris.batchrmperm.domain.ApkOriginal;
import it.unige.dibris.batchrmperm.domain.Device;
import it.unige.dibris.batchrmperm.domain.comparable.PermDeltaCardinality;
import it.unige.dibris.batchrmperm.domain.comparable.SpeedPoint;
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
        int apksGplay = 0;
        int apksAptoide = 0;
        int apksUptodown = 0;
        int customInstallSuccess = 0;
        int customInstallFail = 0;
        int customInstallSuccessButMonkeyCrash = 0;
        int customMonkeyCrashAndOriginalMonkeyCrash = 0;
        int customMonkeyCrashButOriginalMonkeySuccess = 0;
        int customInstallSuccessAndMonkeySuccess = 0;
        int customInstallFailButOriginalInstallSuccess = 0;
        int customInstallFailAndOriginalInstallFail = 0;
        double totApks = 0.0;
        double summDurationSec = 0.0;
        double summRatios = 0.0;
        double avgDurationSec = 0.0;
        double avgRatios = 0.0;
        double varDurationSec = 0.0;
        double varRatios = 0.0;
        double stdDeviationRemSec = 0.0;
        double stdDeviationRatios = 0.0;


        System.out.println("---> Start getting results");

        Iterable<ApkCustom> customApks = apkCustomRepository.findAll();

        for (ApkCustom apkCustom : customApks) {
            totApks++;
            summDurationSec += apkCustom.getRemovalTimeNanoSec();
            ApkOriginal apkOriginal = apkCustom.getApkOriginal();
            if (apkCustom.isInstallSuccess()) {
                customInstallSuccess++;
                summRatios += apkOriginal.getFileSize() / apkCustom.getFileSize();
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
            } else {
                customInstallFail++;
                if (apkOriginal.isInstallSuccess()) {
                    customInstallFailButOriginalInstallSuccess++;
                } else {
                    customInstallFailAndOriginalInstallFail++;
                }
            }
        }

        avgDurationSec = summDurationSec / totApks;
        avgRatios = summRatios / totApks;

        varDurationSec = 0.0;
        varRatios = 0.0;
        for (ApkCustom apkCustom : customApks) {
            varDurationSec += Math.pow(((double) apkCustom.getRemovalTimeNanoSec() - avgDurationSec), 2);
            varRatios += Math.pow((apkCustom.getApkOriginal().getFileSize() / apkCustom.getFileSize() - avgRatios), 2);
        }
        stdDeviationRemSec = Math.sqrt(varDurationSec / (totApks - 1));
        stdDeviationRatios = Math.sqrt(varRatios / (totApks - 1));

        System.out.println("<--- End getting results");
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "androidResult/{fileSize}/{time}", method = RequestMethod.GET)
    public ResponseEntity<?> androidResult(@PathVariable String fileSize, @PathVariable String time) {

        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "generateDat", method = RequestMethod.GET)
    public ResponseEntity<?> generateDat() {
        try {
            /*
            File speed = File.createTempFile("speed_pc", ".dat");
            File permfail = File.createTempFile("permfail", ".dat");
            File beforeafter = File.createTempFile("beforeafter", ".dat");
            */
            File speed = new File("/home/simo/gnuplotest/speed/speed_pc.dat");
            File beforeafter = new File("/home/simo/gnuplotest/beforeafter/beforeafter.dat");
            if (speed.exists())
                speed.delete();
            if (beforeafter.exists())
                beforeafter.delete();
            System.out.println("---> Start generating dat");

            Iterable<ApkCustom> customApks = apkCustomRepository.findAll();

            List<SpeedPoint> speedPoints = new ArrayList<SpeedPoint>();
            List<PermDeltaCardinality> permDeltas = new ArrayList<PermDeltaCardinality>();
            for (ApkCustom apkCustom : customApks) {
                double seconds = ((double) apkCustom.getRemovalTimeNanoSec() / 1000000000.0);
                speedPoints.add(new SpeedPoint(seconds, apkCustom.getApkOriginal().getFileSize()));
                permDeltas.add(new PermDeltaCardinality(
                        apkCustom.getApkOriginal().getPermissions().size(),
                        apkCustom.getPermissions().size()));
            }
            System.out.println("--- Sorting result");
            Collections.sort(speedPoints);
            Collections.sort(permDeltas);
            PrintWriter printWriter = new PrintWriter(speed);
            for (SpeedPoint sp : speedPoints)
                printWriter.println(sp);
            printWriter = new PrintWriter(beforeafter);
            for (PermDeltaCardinality pdc : permDeltas)
                printWriter.println(pdc);

            System.out.println("<--- End generating dat");
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
