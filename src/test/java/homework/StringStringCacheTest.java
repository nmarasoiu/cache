package homework;

/**
 * Created by dnmaras on 10/13/14.
 */
public class StringStringCacheTest extends  CacheTest<String,String> {
    @Override
    protected String testKey() {
        return "kk";
    }

    @Override
    protected String testValue() {
        return "Vv";
    }

}
