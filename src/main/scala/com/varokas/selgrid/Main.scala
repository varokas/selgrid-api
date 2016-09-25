package com.varokas.selgrid

object Main {
  def main(args: Array[String]) {
    val r = new Requests("", "", "")
    println(r.createGrid("01", 1, 2, "mesos.bigbears.io"))
    //println(r.deleteGrid("01"))
  }
}
