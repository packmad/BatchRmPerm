package it.unige.dibris.batchrmperm.domain.result;


import java.util.*;

public class DangerousPermissionCounter {

    public Map<String, Integer> counter = new HashMap<String, Integer>();
    private double totApks = 0.0;

    public DangerousPermissionCounter() {
        counter.put("android.permission.WRITE_CONTACTS", 0);
        counter.put("android.permission.ACCESS_FINE_LOCATION", 0);
        counter.put("android.permission.RECORD_AUDIO", 0);
        counter.put("android.permission.READ_CALL_LOG", 0);
        counter.put("android.permission.READ_EXTERNAL_STORAGE", 0);
        counter.put("android.permission.CALL_PHONE", 0);
        counter.put("android.permission.CAMERA", 0);
        counter.put("android.permission.READ_CONTACTS", 0);
        counter.put("android.permission.PROCESS_OUTGOING_CALLS", 0);
        counter.put("android.permission.READ_SMS", 0);
        counter.put("android.permission.WRITE_EXTERNAL_STORAGE", 0);
        counter.put("android.permission.WRITE_CALL_LOG", 0);
        counter.put("android.permission.ACCESS_COARSE_LOCATION", 0);
        counter.put("android.permission.WRITE_CALENDAR", 0);
        counter.put("android.permission.RECEIVE_MMS", 0);
        counter.put("android.permission.READ_CALENDAR", 0);
        counter.put("android.permission.USE_SIP", 0);
        counter.put("android.permission.READ_PHONE_STATE", 0);
        counter.put("android.permission.SEND_SMS", 0);
        counter.put("com.android.voicemail.permission.ADD_VOICEMAIL", 0);
        counter.put("android.permission.BODY_SENSORS", 0);
        counter.put("android.permission.RECEIVE_WAP_PUSH", 0);
        counter.put("android.permission.GET_ACCOUNTS", 0);
        counter.put("android.permission.RECEIVE_SMS", 0);
    }

    public void increment(String perm) {
        if (counter.containsKey(perm)) {
            counter.put(perm, counter.get(perm) + 1);
        }
    }

    public void incTotApks() {
        totApks++;
    }

    public List<PermissionFreq> getOrderedListByFreq() {
        ArrayList<PermissionFreq> out = new ArrayList<>(counter.size());
        for (Map.Entry<String, Integer> entry : counter.entrySet()) {
            String key = entry.getKey().replace("android.permission.", "").replace("com.android.voicemail.permission.", "");
            out.add(new PermissionFreq(key, entry.getValue().doubleValue() * 100.0 / totApks));
        }
        Collections.sort(out);
        return out;
    }
}
