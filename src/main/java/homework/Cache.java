package homework;

/**
 * Created by dnmaras on 10/25/14.
 * todo 1) There should correct exception handling implemented - i.e. not silent
 * --> poate fi o optiune a cachului: daca sa inghita exceptii (pe care sa le logheze) , sau sa le lase sa iasa imediat, sau sa impacheteze 2 exceptii si sa iasa
 2) If a standard implementation of the task is taken (copy-paste, import) - there is no point in the task. We would prefer to have it implemented using core java libraries.
 --> totusi un injector trebuie bagat! guice looks ok?
 3) The solution will be mostly evaluated based on: covered with tests, code quality. If there are some corner cases not covered - it is ok.
 --> macar cateva cazuri cheie, cu mocking pe bune

 de testat de mana multiple entries per hash si concurenta

 eviction in memory si daca e de replicat coada pe filesystem fie implicit fie explicit
 */
public interface Cache<K, V> extends CommonCache<K,V>{
    V get(K key);

    boolean containsKey(K key);

}