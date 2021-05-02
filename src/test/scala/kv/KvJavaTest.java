package kv;

import com.zhy.yisql.common.utils.kv.InMemoryStore;
import com.zhy.yisql.common.utils.kv.KVIndex;
import com.zhy.yisql.common.utils.kv.LevelDB;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;

/**
 *  \* Created with IntelliJ IDEA.
 *  \* User: hongyi.zhou
 *  \* Date: 2021-04-24
 *  \* Time: 00:02
 *  \* Description: 
 *  \
 */
public class KvJavaTest {
  @Test
  public void leveldbTest() throws Exception {
    LevelDB lDb = new LevelDB(new File("java_example"));
    lDb.write(new JCustomClassItem("testval", "456"));
    JCustomClassItem wapper = lDb.read(JCustomClassItem.class, "testval");
    System.out.println(wapper);
  }

  @Test
  public void inMemoTest() throws Exception {
    InMemoryStore inMemoryStore = new InMemoryStore();
    inMemoryStore.write(new JCustomClassItem("testval1", "456"));
    inMemoryStore.write(new JCustomClassItem("testval2", "456"));
    JCustomClassItem wapper1 = inMemoryStore.read(JCustomClassItem.class, "testval1");
    JCustomClassItem wapper2 = inMemoryStore.read(JCustomClassItem.class, "testval2");
    System.out.println(1);
  }

  static class JCustomClassItem implements Serializable {
    @KVIndex
    String name;
    String value;

    JCustomClassItem() {
    }

    JCustomClassItem(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }
  }
}