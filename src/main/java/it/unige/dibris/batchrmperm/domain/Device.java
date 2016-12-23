package it.unige.dibris.batchrmperm.domain;


import java.io.File;


public class Device {
    private String id;
    private File folder;

    public Device(String id) {
        this.id = id;
        switch (id) {
            //TODO
            case "F9NPFX069418":
                this.folder = new File("/media/simo/HDEsterno/AApks/aptoide");
                break;
            case "F9NPFX069627":
                this.folder = new File("/media/simo/HDEsterno/AApks/googleplay");
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown device id: '%s'", id));
        }
        if (!this.folder.exists())
            throw new IllegalArgumentException(String.format("The folder '%s' doesn't exists", folder));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }
}
