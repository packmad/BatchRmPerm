package it.unige.dibris.batchrmperm.domain.result;


public class PermissionFreq implements Comparable<PermissionFreq> {
    private String permissionName;
    private double freq;

    public PermissionFreq(String permissionName, double freq) {
        this.permissionName = permissionName;
        this.freq = freq;
    }

    public double getFreq() {
        return freq;
    }

    @Override
    public int compareTo(PermissionFreq pf) {
        return -Double.compare(this.freq, pf.getFreq());
    }

    @Override
    public String toString() {
        return String.format("%s %f", permissionName, freq);
    }
}
