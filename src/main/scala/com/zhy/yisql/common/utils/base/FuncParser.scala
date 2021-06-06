package com.zhy.yisql.common.utils.base

import scala.collection.mutable.ArrayBuffer

class FuncParser {
  def printFunc(s: Func, level: Int = 0):Unit = {
    println("*" * level + s.name)
    s.params.foreach {
      case func: Func => printFunc(func, level + 1)
      case symbo: Symbol => println("*" * level + symbo)
    }
  }

  def evaluate(s: Func, executeFunc: (ExecuteFunc) => Symbol): Symbol = {
    val tempParams = s.params.map {
      case func@Func(name, params, raw) =>
        if (params.filter(_.isInstanceOf[Func]).length == 0) {
          executeFunc(ExecuteFunc(name, params.map(_.asInstanceOf[Symbol])))
        } else {
          evaluate(func, executeFunc)
        }
      case symbol: Symbol => symbol

    }
    executeFunc(ExecuteFunc(s.name, tempParams))

  }

  def parseFunc(s: String): Option[Func] = {
    if (s.isEmpty) return None
    val nameBuf = ArrayBuffer[Char]()
    val paramBuf = ArrayBuffer[Char]()
    val params = ArrayBuffer[Expr]()

    var namePhase = true
    var paramPhase = false
    val charArray = s.toCharArray

    var bracketStart = false
    var bracketEnd = false

    var bracketStartNum = 0
    var bracketEndNum = 0
    try {
      (0 until charArray.length).foreach { index =>
        if (charArray(index) == ',' || index == charArray.length - 1) {
          val temp = String.valueOf(paramBuf.toArray)
          val funcOpt = parseFunc(temp)
          if (funcOpt.isDefined) {
            params += funcOpt.get
          } else {
            params += Symbol(temp)
          }
          paramBuf.clear()
        } else if (paramPhase) {
          paramBuf += charArray(index)
        }

        if (charArray(index) == '(') {
          namePhase = false
          paramPhase = true
          bracketStart = true
        }


        if (charArray(index) == ')') {
          bracketEnd = true
        }

        if (namePhase) {
          nameBuf += charArray(index)
        }
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        None
    }

    if (!bracketStart || !bracketEnd) return None

    Option(Func(String.valueOf(nameBuf.toArray).trim(), params.toArray, s))
  }
}

case class ExecuteFunc(name: String, params: Array[Symbol])

sealed abstract class Expr(raw: String)

case class Func(name: String, params: Array[Expr], raw: String) extends Expr(raw)

case class Symbol(name: String) extends Expr(name)
