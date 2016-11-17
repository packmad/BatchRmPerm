package it.unige.dibris.batchrmperm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import net.dongliu.apk.parser.ApkParser;
import net.dongliu.apk.parser.bean.ApkMeta;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @NotNull
    private boolean monkeyCrash;

    @ElementCollection
    private Set<String> permissions;

    @Lob
    @Column(length = 8192)
    private String rmPermOutput;

    @Lob
    @Column(length = 8192)
    private String monkeyOutput;

    Apk() {}

    public Apk(Path apkPath, String rmPermOutput) throws IOException {
        this(apkPath);
        this.rmPermOutput = rmPermOutput;
    }

    public Apk(Path apkPath) throws IOException {
        File apkFile = new File(apkPath.toString());
        ApkMeta apkMeta = new ApkParser(apkFile).getApkMeta();
        packName = apkMeta.getPackageName();
        permissions = new HashSet<>(apkMeta.getUsesPermissions());
        sha256Hash = Files.hash(apkFile, Hashing.sha256()).toString();
        md5Hash = Files.hash(apkFile, Hashing.md5()).toString();
        installSuccess = false;
        monkeyOutput = "NO_REASON";
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

    public String getMonkeyOutput() {
        return monkeyOutput;
    }

    public void setMonkeyOutput(String monkeyOutput) {
        this.monkeyOutput = monkeyOutput;
    }

    public void setMonkeyOutput(List<String> monkeyOutput) {
        setMonkeyCrash(monkeyOutput.get(monkeyOutput.size()-1).contains("System appears to have crashed"));
        this.monkeyOutput = String.join("§", monkeyOutput);
    }


    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public boolean isMonkeyCrash() {
        return monkeyCrash;
    }

    public void setMonkeyCrash(boolean monkeyCrash) {
        this.monkeyCrash = monkeyCrash;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public String getRmPermOutput() {
        return rmPermOutput;
    }

    public void setRmPermOutput(String rmPermOutput) {
        this.rmPermOutput = rmPermOutput;
    }

    @Override
    public String toString() {
        return "Apk{" +
                "packName='" + packName + '\'' +
                ", md5Hash='" + md5Hash + '\'' +
                ", path=" + path +
                ", installSuccess=" + installSuccess +
                ", monkeyCrash=" + monkeyCrash +
                ", failureReason='" + monkeyOutput + '\'' +
                '}';
    }
}
