package com.melvinlow.hammer

import com.melvinlow.hammer.instances.auto.given
import com.melvinlow.hammer.syntax.all.*

object HammerSpec extends weaver.FunSuite {
  test("should hammer a basic ADT") {
    final case class A(x: Int, y: Int)
    final case class B(x: Int, y: Int)

    expect(A(1, 2).hammerTo[B] == B(1, 2))
  }

  test("should hammer an ADT with reversed and missing fields") {
    final case class A(x: Int, y: Int, z: List[Int])
    final case class B(z: List[Int], x: Int)

    expect(A(1, 2, List(3)).hammerTo[B] == B(List(3), 1))
  }

  test("should hammer nested fields") {
    final case class A(x: Int, y: Int)
    final case class B(y: Int)
    final case class C(a: A, b: B)
    final case class D(b: B, a: B)

    expect(C(A(1, 2), B(3)).hammerTo[D] == D(B(3), B(2)))
  }

  test("should hammer a custom field") {
    final case class A(x: Int)
    final case class B(x: String)

    given Hammer[Int, String] with {
      override def hammer(input: Int): String = input.toString
    }

    expect {
      A(1).hammerTo[B] == B("1")
    }
  }

  test("should hammer an int to option") {
    given Hammer[Int, Option[Int]] = (i: Int) => Some(i)

    case class F(x: Int)
    case class G(x: Option[Int])

    expect(F(1).hammerTo[G] == G(Some(1)))
  }
}
