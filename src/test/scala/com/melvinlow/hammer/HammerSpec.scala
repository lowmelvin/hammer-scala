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

  test("should hammer a field with a custom extractor") {
    final case class A(x: Int, y: Int)
    final case class B(x: Int, y: Int)

    given Extractor[A, "x", Int] = (_: A) => 10

    expect(A(1, 2).hammerTo[B] == B(10, 2))
  }

  test("should hammer a missing field with a custom extractor") {
    final case class A(x: Int, y: Int)
    final case class B(x: Int, y: Int, z: Int)

    given Extractor[A, "z", Int] = (_: A) => 10

    expect(A(1, 2).hammerTo[B] == B(1, 2, 10))
  }

  test("should hammer with all directly specified fields") {
    final case class A(x: Int)
    final case class B(x: Int)

    val b1 = A(0).hammerWith[B](Patch["x"](1))
    val b2 = A(0).hammerWith[B](Patch["x"](2))
    val b3 = A(0).hammerTo[B]
    val b4 = A(0).hammerWith[B]()

    expect.all(
      b1 == B(1),
      b2 == B(2),
      b3 == B(0),
      b4 == B(0)
    )
  }

  test("should hammer with partially specified fields") {
    final case class A(x: Int, y: String)
    final case class B(w: String, y: String, z: Int)

    val b1 = A(0, "y").hammerWith[B](Patch["w"]("w"), Patch["z"](1), Patch["y"]("x"))
    val b2 = A(0, "y").hammerWith[B](Patch["w"]("w"), Patch["z"](2))

    expect.all(
      b1 == B("w", "x", 1),
      b2 == B("w", "y", 2)
    )
  }

  test("should override a custom extractor when using hammerWith") {
    final case class A(x: Int, y: Int)
    final case class B(x: Int, y: Int)

    given Extractor[A, "x", Int] = (_: A) => 10

    expect(A(1, 2).hammerWith[B](Patch["x"](11)) == B(11, 2))
  }
}
