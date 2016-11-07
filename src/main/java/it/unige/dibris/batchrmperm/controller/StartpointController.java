package it.unige.dibris.batchrmperm.controller;


import it.unige.dibris.batchrmperm.service.BatchWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@EnableAsync
@RestController
@RequestMapping("/start")
public class StartpointController {

    @Autowired
    BatchWork batchWork;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<?> startTheWork() {
        try {
            batchWork.doTheWork();
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
