一、内部机制

for 推导式的语法实际上是编译器提供的语法糖，它会调用容器方法 foreach、 map、 flatMap 以及 withFilter 方法。

// 局部变量使用 map 传递
```
// 定义一组方法
val filesHere = (new java.io.File(".")).listFiles
def fileLines(file: java.io.File) = scala.io.Source.fromFile(file).getLines().toList

for {
  file <- filesHere if file.isFile
  line <- fileLines(file)
  trimmed = line.trim
  if trimmed.matches(".*scalaTest.*")
} println(file + ":" + trimmed)

// 取消语法糖
filesHere
  .withFilter(file => file.isFile)
  .foreach(file =>
    fileLines(file)
      .map { line => val trimmed = line.trim; (line, trimmed) }
      .withFilter { case (line, trimmed) => trimmed.matches(".*scalaTest.*") }
      .foreach { case (line, trimmed) => println(file + ":" + trimmed) }
  )
```

// 对比前面的表达式
```
for {
  s <- states
  c <- s
  if c.isLower
  c2 = s"$c-${c.toUpper} "
} yield c2

// 取消语法糖
states flatMap  (_.toSeq withFilter (_.isLower) map { c =>
  val c2 = s"$c-${c.toUpper} "
  c2
})
```


二、其它容器

除了这些明显的容器类型(List、Array 以及 Map)之外，for 推导式中还可以使用任何一种实现 foreach、map、flat 以及 withFilter 方法的类型。换言之，任何提供了这些方法的类型均可视为容器，而我们也可以在 for 推导式中使用这些 类型的实例。


1.Option 容器

示例：
```
def positive(i: Int): Option[Int] =   if (i > 0) Some(i) else None

for {
  i1 <- positive(5)
  i2 <- positive(-1 * i1)              // <1>   EPIC FAIL!
  i3 <- positive(25 * i2)              // <2>
  i4 <- positive(-2 * i3)              // EPIC FAIL!
} yield (i1 + i2 + i3 + i4)

// 在第2步之后，直接返回 None，后续都是返回这个 None，不会运行 positive 方法。
```

注意如果只有一层判断也可以写成这样
```
source match {
  case Some(s) =>
    println(s"Closing $fileName...")
    s.close()
}

//等同
for (s <- source) {
  println(s"Closing $fileName...")
  s.close
}
```

2.Either 容器

Either 容器默认是 right-biased，匹配时类似这种
```
def map[B1](f: B => B1): Either[A, B1] = this match {
  case Right(b) => Right(f(b))
  case _        => this.asInstanceOf[Either[A, B1]]
}
```
可以使用 left 方法，将其转换为 LeftProjection, 时类似这种
``` 
def map[A1](f: A => A1): Either[A1, B] = e match {
  case Left(a) => Left(f(a))
  case _       => e.asInstanceOf[Either[A1, B]]
}
```

示例：
```
def positive(i: Int): Either[String,Int] =
  if (i > 0) Right(i) else Left(s"nonpositive number $i")

val r =for {
  i1 <- positive(5)
  i2 <- positive(-1 * i1)   // EPIC FAIL!
  i3 <- positive(25 * i2)
  i4 <- positive(-2 * i3)   // EPIC FAIL!
} yield (i1 + i2 + i3 + i4)

println(r) //  Left(nonpositive number -5)
//在第二步之后，后续都是返回 Left("nonpositive number -5")
```

示例：
```
def addInts2(s1: String, s2: String): Either[NumberFormatException,Int] = 
  try { 
    Right(s1.toInt + s2.toInt)
  } catch { 
    case nfe: NumberFormatException => Left(nfe)
  }

println(addInts2("1", "2"))
println(addInts2("1", "x"))
println(addInts2("x", "2"))
```

3.Try 容器

类似于 Either，只不过其 Left 是固定的 Throwble 类型

```
def positive(i: Int): Try[Int] = Try {
  assert(i > 0, s"nonpositive number $i")
  i
}

var r = for {
  i1 <- positive(5)
  i2 <- positive(-1 * i1) // EPIC FAIL!
  i3 <- positive(25 * i2)
  i4 <- positive(-2 * i3) // EPIC FAIL!
} yield (i1 + i2 + i3 + i4)
println(r) // Failure(java.lang.AssertionError: assertion failed: nonpositive number -5)
```

Try 的 apply 方法的实现如下：
```
object Try {
  /** Constructs a `Try` using the by-name parameter.  This
   * method will ensure any non-fatal exception is caught and a
   * `Failure` object is returned.
   */
  def apply[T](r: => T): Try[T] =
    try Success(r) catch {
      case NonFatal(e) => Failure(e)
    }
}
```

