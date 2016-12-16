package it.unige.dibris.batchrmperm.domain.comparable;


public class PermDeltaCardinality implements Comparable<PermDeltaCardinality> {
    private int before;
    private int after;
    private int delta;

    public PermDeltaCardinality(int origPerms, int customPerms) {
        this.delta = origPerms - customPerms;
        this.after = customPerms;
        this.before = origPerms;
    }

    public int getDelta() {
        return delta;
    }

    @Override
    public int compareTo(PermDeltaCardinality pdc) {
        return Integer.compare(this.delta, pdc.getDelta());
    }

    @Override
    public String toString() {
        return String.format("%d %d %d", delta, after, delta);
    }
}
