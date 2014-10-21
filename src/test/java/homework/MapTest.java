package homework;

import org.mapdb.MapInterfaceTest;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static homework.TestUtils.createRoot;

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
        return new MapBasedOnCache<>(new ExtendedCacheOnFilesystem<>(createRoot()));
    }

    @Override
    protected Map<String,String> makePopulatedMap() throws UnsupportedOperationException {
        Map<String,String> map = makeEmptyMap();
        Arrays.asList(UUID.randomUUID().toString().split("-")).stream().forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                map.put(s, s);
            }
        });
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
