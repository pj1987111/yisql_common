package kv

import java.io.File

import com.fasterxml.jackson.annotation.JsonIgnore
import com.zhy.yisql.common.utils.kv.{KVIndex, KVUtils, LevelDB}
import org.junit.Test


/**
  *  \* Created with IntelliJ IDEA.
  *  \* User: hongyi.zhou
  *  \* Date: 2021-04-22
  *  \* Time: 23:29
  *  \* Description: 
  *  \*/
class KvTest {
  @Test
  def testKv(): Unit = {
    val db = KVUtils.open(new File("example3"), 3)
//    val lDb = new LevelDB(new File("example1"))
    db.write(CustomClassItem("testval1", "4567", 12.456f))
    val value = db.read(classOf[CustomClassItem], "testval1")
//    lDb.write(Map("123"->"123","456"->"789"))
//    lDb.read()
    val viewV = db.view(classOf[CustomClassItem])
    val count = db.count(classOf[CustomClassItem])
    print(11)
  }
}

case class CustomClassItem(@KVIndex name: String, value1: String, value2: Float) {
  @KVIndex
  def id = name
}
