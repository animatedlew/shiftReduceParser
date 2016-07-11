package com.animatedlew.parser

import collection.mutable

object Parser {
  import scala.util.control.Breaks._
  object parser {

    val tokens = Vector("(", "(", ")", ")")
    val grammar: Grammar = Vector(
      "S" -> Vector("P"),
      "P" -> Vector("(", "P", ")"),
      "P" -> Vector()
    )

    def process() = {
      val accepted = parse(tokens, grammar)
      print(Console.WHITE)
      println(s"${Console.BLUE}accepted: $accepted ${Console.WHITE}")
    }

    type Grammar = Vector[(String, Vector[String])]

    private def prettyify(v: Vector[String]) = v mkString " "

    case class State(
      terminal: String = "X",
      seen: Vector[String] = Vector("a", "b"),
      expected: Vector[String] = Vector("c", "d"),
      from: Int = 0
    ) {
      override def toString =
        s"$terminal -> ${prettyify(seen)} . ${prettyify(expected)} from $from"
    }

    def closure(grammar: Grammar, i: Int, ab: Vector[String], cd: Vector[String]) = {
      for ((lhs, rhs) <- grammar if cd.nonEmpty && cd.head == lhs) yield State(lhs, Vector(), rhs, i)
    }

    def shift(tokens: Vector[String], i: Int, x: String, ab: Vector[String], cd: Vector[String], j: Int) = {
      cd.headOption flatMap { nonTerminal =>
        if (i < tokens.size && nonTerminal == tokens(i))
          Some(State(x, ab :+ nonTerminal, cd.tail, j))
        else None
      }
    }

    def reduce(chart: mutable.Map[Int, Vector[State]], x: String, ab: Vector[String], cd: Vector[String], j: Int) = {
      for (state <- chart(j) if cd.isEmpty && state.expected.headOption.contains(x)) yield {
        state.copy(seen = state.seen :+ state.expected.head, expected = state.expected.tail)
      }
    }

    def updateChart(chart: mutable.Map[Int, Vector[State]], index: Int, state: State) = {
      if (!chart(index).contains(state)) {
        chart(index) = chart(index) :+ state
        Some(chart)
      } else None
    }

    def parse(tokens: Vector[String], grammar: Grammar) = {
      val (startTerminal, startRule) = grammar.head
      val startState = State(startTerminal, Vector(), startRule, 0)
      var chart = mutable.Map[Int, Vector[State]]().withDefaultValue(Vector()).updated(0, Vector(startState))
      (tokens :+ "$").zipWithIndex.foreach { case (t, i) =>
        println(s"${Console.GREEN}current: $t ${Console.WHITE}")
        breakable { while(true) {
          var changes = false
          for (state <- chart(i)) {
            val (x, ab, cd, j) = (state.terminal, state.seen, state.expected, state.from)
            for (nextState <- closure(grammar, i, ab, cd)) yield {
              updateChart(chart, i, nextState) foreach { updatedChart =>
                chart = updatedChart
                changes = true
              }
            }
            shift(tokens, i, x, ab, cd, j) foreach { nextState =>
              updateChart(chart, i + 1, nextState) foreach { updatedChart =>
                println(s"${Console.BLUE}shift")
                chart = updatedChart
                changes = true
              }
            }
            for (nextState <- reduce(chart, x, ab, cd, j)) yield {
              updateChart(chart, i, nextState) foreach { updatedChart =>
                println(s"${Console.BLUE}reduce")
                chart = updatedChart
                changes = true
              }
            }
          }
          if (!changes) break()
        }}
      }
      debug(chart)
      val acceptingState = State(startTerminal, startRule, Vector(), 0)
      chart(tokens.size).contains(acceptingState)
    }

    def debug(chart: mutable.Map[Int, Vector[State]]) = {
      println(s"${Console.MAGENTA}-" * 25)
      chart.toVector.sortBy { _._1 } foreach { case (k, v) =>
        println(s"${Console.YELLOW}-- chart $k --")
        v.foreach { state => println(s"${Console.CYAN}\t$state") }
      }
      println(s"${Console.MAGENTA}-" * 25)
      print(Console.WHITE)
    }
  }
}
