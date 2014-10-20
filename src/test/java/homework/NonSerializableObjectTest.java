package homework;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOError;
import java.io.NotSerializableException;

/**
 * Created by dnmaras on 10/13/14.
 */
public class NonSerializableObjectTest extends CacheTest<Object, Object> {
    @Override
    protected Object testKey() {
        return new Object();
    }

    @Override
    protected Object testValue() {
        return new Object();
    }

    @Test
    public void checkGetAfterPutReturnsValue() {
        try {
            super.checkGetAfterPutReturnsValue();
        } catch (IOError e) {
            Assert.assertEquals(e.getCause().getClass(), NotSerializableException.class);
            return;
        }
        Assert.fail();
    }

    @Test
    public void checkGetAfterPutReturnsValueForNull() {
        try {
            super.checkGetAfterPutReturnsValueForNull();
        } catch (IOError e) {
            Assert.assertEquals(e.getCause().getClass(), NotSerializableException.class);
            return;
        }
        Assert.fail();
    }

    @Test
    public void checkGetAfterPutReturnsNull() {
        try {
            super.checkGetAfterPutReturnsNull();
        } catch (IOError e) {
            Assert.assertEquals(e.getCause().getClass(), NotSerializableException.class);
            return;
        }
        Assert.fail();
    }
}
