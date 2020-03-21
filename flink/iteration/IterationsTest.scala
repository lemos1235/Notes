package quickstart

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala._

object IterationsTest {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val someIntegers: DataStream[Long] = env.generateSequence(0, 1000)

    val iteratedStream = someIntegers.iterate(
      iteration => {
        val minusOne = iteration.map( v => v - 1)
        val stillGreaterThanZero = minusOne.filter (_ > 0)
        val lessThanZero = minusOne.filter(_ <= 0)
        (stillGreaterThanZero, lessThanZero)
      }
    )

  }
}
