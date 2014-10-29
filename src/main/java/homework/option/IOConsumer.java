package homework.option;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Created by nmarasoiu on 10/29/2014.
 */
public interface IOConsumer<V> {
    public void accept(V v) throws IOException ;

    public Consumer<V> andThen(IOConsumer<? super V> after) throws IOException ;
}
