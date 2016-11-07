package it.unige.dibris.batchrmperm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import net.dongliu.apk.parser.ApkParser;
import net.dongliu.apk.parser.bean.ApkMeta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Entity
public class Apk {
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NotNull
    private String packName;

    @NotNull
    @Column(unique = true)
    private String sha256Hash;

    @NotNull
    private String md5Hash;

    @NotNull
    private Path path;

    @NotNull
    private boolean installSuccess;

    private String failureReason;

    Apk() {}

    public Apk(Path apkPath) throws IOException {
        File apkFile = new File(apkPath.toString());
        ApkMeta apkMeta = new ApkParser(apkFile).getApkMeta();
        packName = apkMeta.getPackageName();
        sha256Hash = Files.hash(apkFile, Hashing.sha256()).toString();
        md5Hash = Files.hash(apkFile, Hashing.md5()).toString();
        installSuccess = false;
        failureReason = "NO_REASON";
        Path renamed = apkPath.resolveSibling(String.format("%s_%s.apk", packName, md5Hash));
        java.nio.file.Files.move(apkPath, renamed);
        path = renamed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public boolean isInstallSuccess() {
        return installSuccess;
    }

    public void setInstallSuccess(boolean installSuccess) {
        this.installSuccess = installSuccess;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Apk{" +
                "packName='" + packName + '\'' +
                ", md5Hash='" + md5Hash + '\'' +
                ", path=" + path +
                ", installSuccess=" + installSuccess +
                ", failureReason='" + failureReason + '\'' +
                '}';
    }
}
