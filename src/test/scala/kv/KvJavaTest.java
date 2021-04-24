package kv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhy.yisql.common.utils.kv.KVIndex;
import com.zhy.yisql.common.utils.kv.KVStoreSerializer;
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
  public void simple() throws Exception {
    LevelDB lDb = new LevelDB(new File("java_example"));
    lDb.write(new JCustomClassItem("testval", "456"));
    JCustomClassItem wapper = lDb.read(JCustomClassItem.class, "testval");
    System.out.println(wapper);
  }
}

class JCustomClassItem implements Serializable {
  @KVIndex
  String name;
  String className;

  JCustomClassItem() {
  }

  JCustomClassItem(String name, String className) {
    this.name = name;
    this.className = className;
  }

  public String getName() {
    return name;
  }

  public String getClassName() {
    return className;
  }
}

//class JCustomClassItemWrapper implements Serializable {
//  @JsonIgnore
//  @KVIndex
//  String id() {
//    return customClassItem.name;
//  }
//
//  JCustomClassItem customClassItem;
//
//  JCustomClassItemWrapper() {
//  }
//
//  JCustomClassItemWrapper(JCustomClassItem customClassItem) {
//    this.customClassItem = customClassItem;
//  }
//
//  public JCustomClassItem getCustomClassItem() {
//    return customClassItem;
//  }
//}