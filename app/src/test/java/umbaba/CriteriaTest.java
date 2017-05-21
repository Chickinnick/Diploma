package umbaba;

import com.umbaba.bluetoothvswifidirect.data.comparation.Criteria;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Nick on 21.05.2017.
 */
public class CriteriaTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void getSpeedValueString() throws Exception {

        String speedValueString = Criteria.getSpeedValueString(1234.2342);
        Assert.assertEquals(speedValueString, "1234.23MBps");

    }

}