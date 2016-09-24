package com.varokas.selgrid

object Main {
  def main(args: Array[String]) {
    val r = new Requests("", "", "")
    //println(r.createGrid("01", 2, 2))
    println(r.deleteGrid("01"))
  }
}
