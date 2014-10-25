package homework.option;

/**
 * Created by dnmaras on 10/25/14.
 */
public class OptionWithValue<V> implements Option<V> {
    private final V value;

    public OptionWithValue(V value) {
        this.value = value;
    }


    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public V value() {
        return value;
    }
}
