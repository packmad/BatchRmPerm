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
public abstract class Apk {
    @Id
    @GeneratedValue
    @JsonIgnore
    protected Long id;

    @NotNull
    protected String packName;

    @NotNull
    @Column(unique = true)
    protected String sha256Hash;

    @NotNull
    protected String md5Hash;

    @NotNull
    protected Path path;

    @NotNull
    protected double fileSize;

    @ManyToMany(cascade = CascadeType.REFRESH)
    protected Set<Permission> permissions;

    @Transient
    protected Set<String> tmpPermSet;

    // The following fields are only written if the dynamic analysis is performed

    protected boolean installSuccess;

    @Transient
    protected String installFailReason;


    protected boolean monkeyCrash;
    protected long monkeySeed;

    @Transient
    private String monkeyOutput;


    Apk() {}

    public Apk(File apkFile) throws IOException {
        this(apkFile.toPath());
    }

    public Apk(Path apkPath) throws IOException {
        File apkFile = new File(apkPath.toString());
        ApkMeta apkMeta = new ApkParser(apkFile).getApkMeta();
        packName = apkMeta.getPackageName();
        tmpPermSet = new HashSet<>(apkMeta.getUsesPermissions());
        sha256Hash = Files.hash(apkFile, Hashing.sha256()).toString();
        md5Hash = Files.hash(apkFile, Hashing.md5()).toString();
        fileSize = apkFile.length();
        Path renamed = apkPath.resolveSibling(String.format("%s_%s.apk", packName, md5Hash));
        java.nio.file.Files.move(apkPath, renamed);
        path = renamed;
    }

    public void setMonkeyResult(List<String> monkeyOutput) {
        this.monkeyOutput = String.join("\n", monkeyOutput);
        String lastLine = monkeyOutput.get(monkeyOutput.size()-1);
        if (lastLine.contains("System appears to have crashed")) {
            setMonkeyCrash(true);
            setMonkeySeed(Long.parseLong((lastLine.substring(lastLine.indexOf("seed ") + 5, lastLine.length()))));
        }
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

    public Set<String> getTmpPermSet() {
        return tmpPermSet;
    }

    public void setTmpPermSet(Set<String> tmpPermSet) {
        this.tmpPermSet = tmpPermSet;
    }

    public double getFileSize() {
        return fileSize;
    }

    public void setFileSize(double fileSize) {
        this.fileSize = fileSize;
    }

    public long getMonkeySeed() {
        return monkeySeed;
    }

    public void setMonkeySeed(long monkeySeed) {
        this.monkeySeed = monkeySeed;
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

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public String getInstallFailReason() {
        return installFailReason;
    }

    public void setInstallFailReason(String installFailReason) {
        this.installFailReason = installFailReason;
    }


}
