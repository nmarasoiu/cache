package homework;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by dnmaras on 10/27/14.
 */
public interface ExtendedCache<K,V> extends FunctionalCache<K,V> {

     Stream<Map.Entry<K, V>> entryStream() ;

}
