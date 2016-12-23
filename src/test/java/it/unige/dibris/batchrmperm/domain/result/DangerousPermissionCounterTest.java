package it.unige.dibris.batchrmperm.domain.result;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by simo on 17/12/16.
 */
public class DangerousPermissionCounterTest {


    @Test
    public void increment() throws Exception {
        String p = "android.permission.WRITE_CONTACTS";
        String wrong = "wrong";
        DangerousPermissionCounter pc = new DangerousPermissionCounter();
        pc.increment(p);
        Assert.assertEquals((int) pc.counter.get(p), 1);
        pc.increment(p);
        Assert.assertEquals((int) pc.counter.get(p), 2);
        pc.increment(wrong);

    }

}