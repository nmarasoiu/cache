package homework.utils;

import homework.option.Option;

import java.util.function.Supplier;

import static homework.option.OptionFactory.none;
import static homework.option.OptionFactory.some;

/**
 * Created by nmarasoiu on 10/28/2014.
 */
public class LazyValue<V> {
    private final Supplier<V> supplier;
    private Option<V> val = none();//I want to cover value==null case

    public LazyValue(Supplier<V> supplier) {
        this.supplier = supplier;
    }

    public V getValue() {
        if (val.isEmpty()) {
            val = some(supplier.get());
        }
        return val.get();
    }
}
