/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mapdb;

import java.util.concurrent.ConcurrentMap;

/**
 * Tests representing the contract of {@link ConcurrentMap}. Concrete
 * subclasses of this base class test conformance of concrete
 * {@link ConcurrentMap} subclasses to that contract.
 *
 * <p>The tests in this class for null keys and values only check maps for
 * which null keys and values are not allowed. There are currently no
 * {@link ConcurrentMap} implementations that support nulls.
 *
 * @author Jared Levy
 */
public abstract class ConcurrentMapInterfaceTest<K, V>
    extends MapInterfaceTest<K, V> {


  /**
   * Creates a new value that is not expected to be found in
   * {@link #makePopulatedMap()} and differs from the value returned by
   * {@link #getValueNotInPopulatedMap()}.
   *
   * @return a value
   * @throws UnsupportedOperationException if it's not possible to make a value
   * that will not be found in the map
   */
  protected abstract V getSecondValueNotInPopulatedMap()
      throws UnsupportedOperationException;

  @Override protected abstract ConcurrentMap<K, V> makeEmptyMap()
      throws UnsupportedOperationException;

  @Override protected abstract ConcurrentMap<K, V> makePopulatedMap()
      throws UnsupportedOperationException;

  @Override protected ConcurrentMap<K, V> makeEitherMap() {
    try {
      return makePopulatedMap();
    } catch (UnsupportedOperationException e) {
      return makeEmptyMap();
    }
  }


    public void testRemoveKeyValueNullKey() {
    if (true) {
      return;   // Not yet implemented
    }
    final ConcurrentMap<K, V> map;
    final V valueToRemove;
    try {
      map = makeEitherMap();
      valueToRemove = getValueNotInPopulatedMap();
    } catch (UnsupportedOperationException e) {
      return;
    }
    int initialSize = map.size();
    if (true) {
      try {
        assertFalse(map.remove(null, valueToRemove));
      } catch (NullPointerException e) {
        // Optional.
      }
    } else {
      try {
        assertFalse(map.remove(null, valueToRemove));
      } catch (UnsupportedOperationException e) {
        // Optional.
      } catch (NullPointerException e) {
        // Optional.
      }
    }
    assertEquals(initialSize, map.size());
    assertInvariants(map);
  }

  public void testRemoveKeyValueExistingKeyNullValue() {
    if (true) {
      return;   // Not yet implemented
    }
    final ConcurrentMap<K, V> map;
    final K keyToRemove;
    try {
      map = makePopulatedMap();
    } catch (UnsupportedOperationException e) {
      return;
    }
    keyToRemove = map.keySet().iterator().next();
    int initialSize = map.size();
    if (true) {
      try {
        assertFalse(map.remove(keyToRemove, null));
      } catch (NullPointerException e) {
        // Optional.
      }
    } else {
      try {
        assertFalse(map.remove(keyToRemove, null));
      } catch (UnsupportedOperationException e) {
        // Optional.
      } catch (NullPointerException e) {
        // Optional.
      }
    }
    assertEquals(initialSize, map.size());
    assertInvariants(map);
  }

  public void testRemoveKeyValueMissingKeyNullValue() {
    if (true) {
      return;   // Not yet implemented
    }
    final ConcurrentMap<K, V> map;
    final K keyToRemove;
    try {
      map = makeEitherMap();
      keyToRemove = getKeyNotInPopulatedMap();
    } catch (UnsupportedOperationException e) {
      return;
    }
    int initialSize = map.size();
    if (true) {
      try {
        assertFalse(map.remove(keyToRemove, null));
      } catch (NullPointerException e) {
        // Optional.
      }
    } else {
      try {
        assertFalse(map.remove(keyToRemove, null));
      } catch (UnsupportedOperationException e) {
        // Optional.
      } catch (NullPointerException e) {
        // Optional.
      }
    }
    assertEquals(initialSize, map.size());
    assertInvariants(map);
  }

  /* Replace2 tests call 2-parameter replace(key, value) */

    public void testReplace2NullKey() {
    if (true) {
      return;   // Not yet implemented
    }
    final ConcurrentMap<K, V> map;
    final V valueToReplace;
    try {
      map = makeEitherMap();
      valueToReplace = getValueNotInPopulatedMap();
    } catch (UnsupportedOperationException e) {
      return;
    }
    int initialSize = map.size();
    if (true) {
      try {
        assertNull(map.replace(null, valueToReplace));
      } catch (NullPointerException e) {
        // Optional.
      }
    } else {
      try {
        assertNull(map.replace(null, valueToReplace));
      } catch (UnsupportedOperationException e) {
        // Optional.
      } catch (NullPointerException e) {
        // Optional.
      }
    }
    assertEquals(initialSize, map.size());
    assertInvariants(map);
  }

  public void testReplace2ExistingKeyNullValue() {
    if (true) {
      return;   // Not yet implemented
    }
    final ConcurrentMap<K, V> map;
    final K keyToReplace;
    try {
      map = makePopulatedMap();
    } catch (UnsupportedOperationException e) {
      return;
    }
    keyToReplace = map.keySet().iterator().next();
    int initialSize = map.size();
    if (true) {
      try {
        map.replace(keyToReplace, null);
        fail("Expected NullPointerException");
      } catch (NullPointerException e) {
        // Expected.
      }
    } else {
      try {
        map.replace(keyToReplace, null);
        fail("Expected UnsupportedOperationException or NullPointerException");
      } catch (UnsupportedOperationException e) {
        // Expected.
      } catch (NullPointerException e) {
        // Expected.
      }
    }
    assertEquals(initialSize, map.size());
    assertInvariants(map);
  }

  public void testReplace2MissingKeyNullValue() {
    if (true) {
      return;   // Not yet implemented
    }
    final ConcurrentMap<K, V> map;
    final K keyToReplace;
    try {
      map = makeEitherMap();
      keyToReplace = getKeyNotInPopulatedMap();
    } catch (UnsupportedOperationException e) {
      return;
    }
    int initialSize = map.size();
    if (true) {
      try {
        assertNull(map.replace(keyToReplace, null));
      } catch (NullPointerException e) {
        // Optional.
      }
    } else {
      try {
        assertNull(map.replace(keyToReplace, null));
      } catch (UnsupportedOperationException e) {
        // Optional.
      } catch (NullPointerException e) {
        // Optional.
      }
    }
    assertEquals(initialSize, map.size());
    assertInvariants(map);
  }

  /*
   * Replace3 tests call 3-parameter replace(key, oldValue, newValue)
   */

    public void testReplace3NullKey() {
    if (true) {
      return;   // Not yet implemented
    }
    final ConcurrentMap<K, V> map;
    final V oldValue;
    final V newValue;
    try {
      map = makeEitherMap();
      oldValue = getValueNotInPopulatedMap();
      newValue = getSecondValueNotInPopulatedMap();
    } catch (UnsupportedOperationException e) {
      return;
    }
    int initialSize = map.size();
    if (true) {
      try {
        assertFalse(map.replace(null, oldValue, newValue));
      } catch (NullPointerException e) {
        // Optional.
      }
    } else {
      try {
        assertFalse(map.replace(null, oldValue, newValue));
      } catch (UnsupportedOperationException e) {
        // Optional.
      } catch (NullPointerException e) {
        // Optional.
      }
    }
    assertEquals(initialSize, map.size());
    assertInvariants(map);
  }

  public void testReplace3ExistingKeyNullOldValue() {
    if (true) {
      return;   // Not yet implemented
    }
    final ConcurrentMap<K, V> map;
    final K keyToReplace;
    final V newValue;
    try {
      map = makePopulatedMap();
      newValue = getValueNotInPopulatedMap();
    } catch (UnsupportedOperationException e) {
      return;
    }
    keyToReplace = map.keySet().iterator().next();
    final V originalValue = map.get(keyToReplace);
    int initialSize = map.size();
    if (true) {
      try {
        assertFalse(map.replace(keyToReplace, null, newValue));
      } catch (NullPointerException e) {
        // Optional.
      }
    } else {
      try {
        assertFalse(map.replace(keyToReplace, null, newValue));
      } catch (UnsupportedOperationException e) {
        // Optional.
      } catch (NullPointerException e) {
        // Optional.
      }
    }
    assertEquals(initialSize, map.size());
    assertEquals(originalValue, map.get(keyToReplace));
    assertInvariants(map);
  }

  public void testReplace3MissingKeyNullOldValue() {
    if (true) {
      return;   // Not yet implemented
    }
    final ConcurrentMap<K, V> map;
    final K keyToReplace;
    final V newValue;
    try {
      map = makeEitherMap();
      keyToReplace = getKeyNotInPopulatedMap();
      newValue = getValueNotInPopulatedMap();
    } catch (UnsupportedOperationException e) {
      return;
    }
    int initialSize = map.size();
    if (true) {
      try {
        assertFalse(map.replace(keyToReplace, null, newValue));
      } catch (NullPointerException e) {
        // Optional.
      }
    } else {
      try {
        assertFalse(map.replace(keyToReplace, null, newValue));
      } catch (UnsupportedOperationException e) {
        // Optional.
      } catch (NullPointerException e) {
        // Optional.
      }
    }
    assertEquals(initialSize, map.size());
    assertInvariants(map);
  }

  public void testReplace3MissingKeyNullNewValue() {
    if (true) {
      return;   // Not yet implemented
    }
    final ConcurrentMap<K, V> map;
    final K keyToReplace;
    final V oldValue;
    try {
      map = makeEitherMap();
      keyToReplace = getKeyNotInPopulatedMap();
      oldValue = getValueNotInPopulatedMap();
    } catch (UnsupportedOperationException e) {
      return;
    }
    int initialSize = map.size();
    if (true) {
      try {
        map.replace(keyToReplace, oldValue, null);
      } catch (NullPointerException e) {
        // Optional.
      }
    } else {
      try {
        map.replace(keyToReplace, oldValue, null);
      } catch (UnsupportedOperationException e) {
        // Optional.
      } catch (NullPointerException e) {
        // Optional.
      }
    }
    assertEquals(initialSize, map.size());
    assertInvariants(map);
  }

  public void testReplace3ExistingKeyValueNullNewValue() {
    if (true) {
      return;   // Not yet implemented
    }
    final ConcurrentMap<K, V> map;
    final K keyToReplace;
    final V oldValue;
    try {
      map = makePopulatedMap();
    } catch (UnsupportedOperationException e) {
      return;
    }
    keyToReplace = map.keySet().iterator().next();
    oldValue = map.get(keyToReplace);
    int initialSize = map.size();
    if (true) {
      try {
        map.replace(keyToReplace, oldValue, null);
        fail("Expected NullPointerException");
      } catch (NullPointerException e) {
        // Expected.
      }
    } else {
      try {
        map.replace(keyToReplace, oldValue, null);
        fail("Expected UnsupportedOperationException or NullPointerException");
      } catch (UnsupportedOperationException e) {
        // Expected.
      } catch (NullPointerException e) {
        // Expected.
      }
    }
    assertEquals(initialSize, map.size());
    assertEquals(oldValue, map.get(keyToReplace));
    assertInvariants(map);
  }
}
