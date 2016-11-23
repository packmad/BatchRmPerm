package it.unige.dibris.batchrmperm.domain;

import javax.persistence.Entity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Entity
public class ApkOriginal extends Apk {


    ApkOriginal() {}

    public ApkOriginal(File apkFile) throws IOException {
        super(apkFile);
    }

    public ApkOriginal(Path apkPath) throws IOException {
        super(apkPath);
    }

}
