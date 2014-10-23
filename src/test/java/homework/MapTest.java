package homework;

import homework.adaptors.MapBasedOnCache;
import homework.filesystem.ExtendedCacheOnFilesystem;
import org.mapdb.MapInterfaceTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static homework.utils.TestUtils.createRoot;

/**
 * Created by dnmaras on 10/18/14.
 */
public class MapTest extends MapInterfaceTest<String,String> {

    public static final List<String> STRINGS = Arrays.asList(UUID.randomUUID().toString().split("-"));

    @Override
    protected Map<String,String> makeEmptyMap() throws UnsupportedOperationException {
        return new MapBasedOnCache<>(new ExtendedCacheOnFilesystem<>(createRoot()));
    }

    @Override
    protected Map<String,String> makePopulatedMap() throws UnsupportedOperationException {
        Map<String,String> map = makeEmptyMap();
        STRINGS.stream().forEach(s -> map.put(s, s));
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
