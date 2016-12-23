package it.unige.dibris.batchrmperm.domain.result;

import java.util.*;

/**
 * Created by simo on 21/12/16.
 */
public class PermissionCounter {

    public Map<String, Integer> counter = new HashMap<String, Integer>();
    private int totApks = 0;

    public void count(String perm) {
        if (counter.containsKey(perm)) {
            counter.put(perm, counter.get(perm) + 1);
        } else
            counter.put(perm, 1);
    }

    public void incTotApks() {
        totApks++;
    }

    public List<PermissionFreq> getOrderedListByFreq() {
        ArrayList<PermissionFreq> out = new ArrayList<>(counter.size());
        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            String key = entry.getKey().replace("android.permission.", "");
            double freq = entry.getValue().doubleValue() * 100.0 / totApks;
            if (freq > 1)
                out.add(new PermissionFreq(key, freq));
        }
        Collections.sort(out);
        return out;
    }
}
