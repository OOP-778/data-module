import com.jparams.store.memory.MemoryStore;

public class TestingMap {
  public static void main(String[] args) {

    // Property Holder two primary keys
    // >>

    final MemoryStore<TestObject> store =
        MemoryStore.newStore(TestObject.class).withIndex("uuid", object -> "wfafwa").build();
  }
}
