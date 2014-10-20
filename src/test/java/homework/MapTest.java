package homework;

import org.mapdb.MapInterfaceTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import static homework.TestUtils.createRoot;
import static homework.utils.ExceptionWrappingUtils.rethrowIOExAsIoErr;

/**
 * Created by dnmaras on 10/18/14.
 */
public class MapTest extends MapInterfaceTest<String,String> {

    /**
     * Constructor with an explicit {@code supportsIteratorRemove} parameter.
     */
    public MapTest() {
        super(
                true //allowsNullKeys ***
                , true// , allowsNullValues ***
                , true// , supportsPut,****
                , true// supportsRemove,*
                , true// supportsClear,
                , true// supportsIteratorRemove,
                , true// supportsEntrySetValue  ***
        );
    }

    @Override
    protected Map<String,String> makeEmptyMap() throws UnsupportedOperationException {
        return new MapBasedOnCache<>(new FileSystemHashCache<>(createRoot()));
    }

    @Override
    protected Map<String,String> makePopulatedMap() throws UnsupportedOperationException {
        Map<String,String> map = makeEmptyMap();
        map.put("a","b");
        map.put("x","x");
        map.put("a","b");
        map.put("a","b");
        return map;
    }

    @Override
    protected String getKeyNotInPopulatedMap() throws UnsupportedOperationException {
        return "c";
    }

    @Override
    protected String getValueNotInPopulatedMap() throws UnsupportedOperationException {
        return "d";
    }

}
