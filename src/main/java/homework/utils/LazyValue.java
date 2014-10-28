package homework.utils;

import homework.option.Option;

import java.util.function.Supplier;

import static homework.option.Option.none;
import static homework.option.Option.some;

/**
 * Created by nmarasoiu on 10/28/2014.
 */
public class LazyValue<V> {
    //I want to cover the scenario when the client can emitter of the lazy value can delegate to its consumer the supplier of the value, if any
    private final Option<Supplier<Option<V>>> supplierOption;

    //I want to cover value==null case
    private Option<V> valueOption = none();

    public LazyValue(Supplier<Option<V>> supplierOption) {
        this.supplierOption = some(supplierOption);
    }

    public LazyValue() {
        this(() -> none());
    }

    public Option<V> getValue() {
        if (valueOption.isEmpty()) {
            valueOption = supplierOption.flatMap(supplier -> supplier.get());
        }
        return valueOption;
    }
}
