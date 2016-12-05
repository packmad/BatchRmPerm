package it.unige.dibris.batchrmperm.controller;


import it.unige.dibris.batchrmperm.domain.ApkCustom;
import it.unige.dibris.batchrmperm.domain.ApkOriginal;
import it.unige.dibris.batchrmperm.domain.Device;
import it.unige.dibris.batchrmperm.engine.ExecuteCmd;
import it.unige.dibris.batchrmperm.repository.ApkCustomRepository;
import it.unige.dibris.batchrmperm.service.BatchWork;
import it.unige.dibris.batchrmperm.service.MalwarePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@EnableAsync
@RestController
public class StartpointController {


    @Autowired
    ApkCustomRepository apkCustomRepository;

    @Autowired
    BatchWork batchWork;

    @Autowired
    MalwarePermission malwarePermission;

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
            summDurationSec += apkCustom.getRemovalTimeSec();
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
            varDurationSec += Math.pow(((double) apkCustom.getRemovalTimeSec() - avgDurationSec), 2);
            varRatios += Math.pow((apkCustom.getApkOriginal().getFileSize() / apkCustom.getFileSize() - avgRatios), 2);
        }
        stdDeviationRemSec = Math.sqrt(varDurationSec / (totApks - 1));
        stdDeviationRatios = Math.sqrt(varRatios / (totApks - 1));

        System.out.println("<--- End getting results");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "malw", method = RequestMethod.GET)
    public ResponseEntity<?> malwarePerm() {
        malwarePermission.doTheWork();
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
