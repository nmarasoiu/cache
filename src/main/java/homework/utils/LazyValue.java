package homework.utils;

import homework.option.Option;

import java.util.function.Supplier;

import static homework.option.Option.none;

/**
 * Created by nmarasoiu on 10/28/2014.
 */
public class LazyValue<V> {
    //I want to cover the scenario when the client can emitter of the lazy value can delegate to its consumer the supplier of the value, if any
    private final Supplier<Option<V>> supplier;

    //I want to cover value==null case
    private Option<V> valueOption = none();

    public LazyValue(Supplier<Option<V>> supplier) {
        this.supplier = supplier;
    }

    public LazyValue() {
        this(() -> none());
    }

    public Option<V> getValue() {
        if (valueOption.isEmpty()) {
            valueOption = supplier.get();
        }
        return valueOption;
    }
}
