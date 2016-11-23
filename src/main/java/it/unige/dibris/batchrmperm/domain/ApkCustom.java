package it.unige.dibris.batchrmperm.domain;

import javax.annotation.Nonnull;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;

@Entity
public class ApkCustom extends Apk {

    @OneToOne(cascade = CascadeType.ALL)
    private ApkOriginal apkOriginal;

    @Lob
    @Column(length = 8192)
    private String rmPermOutput;

    @NotNull
    private long removalTimeSec;

    @NotNull
    private double sizeWrtOriginal;

    ApkCustom() {}

    public ApkCustom(ApkOriginal apkOriginal, File customizedApk, long removalTimeSec) throws IOException {
        super(customizedApk);
        this.removalTimeSec = removalTimeSec;
        this.apkOriginal = apkOriginal;
        this.sizeWrtOriginal = this.getFileSize() - apkOriginal.getFileSize();
    }

    public String getRmPermOutput() {
        return rmPermOutput;
    }

    public void setRmPermOutput(String rmPermOutput) {
        this.rmPermOutput = rmPermOutput;
    }

    public long getRemovalTimeSec() {
        return removalTimeSec;
    }

    public void setRemovalTimeSec(long removalTimeSec) {
        this.removalTimeSec = removalTimeSec;
    }

    public ApkOriginal getApkOriginal() {
        return apkOriginal;
    }

    public void setApkOriginal(ApkOriginal apkOriginal) {
        this.apkOriginal = apkOriginal;
    }

    public double getSizeWrtOriginal() {
        return sizeWrtOriginal;
    }

    public void setSizeWrtOriginal(double sizeWrtOriginal) {
        this.sizeWrtOriginal = sizeWrtOriginal;
    }
}
