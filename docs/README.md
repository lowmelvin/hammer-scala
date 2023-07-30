# Hammer-Scala

Hammer-Scala is a Scala 3 utility library that allows quick and painless
data conversion between different product types.

To include Hammer in your project, add the following to your dependencies:

```scala
libraryDependencies += "com.melvinlow" %% "hammer" % <version>
```

## Quick Start

```scala mdoc:invisible
import java.time.Instant
```

Assume you have an Algebraic Data Type (ADT) like this:

```scala mdoc:silent
case class AccountEntity(id: String, email: String, name: String, secret: String, createdAt: Instant)

val entity = AccountEntity("123", "test@example.com", "nobo", "should-be-hashed", Instant.now)
```

And you want to convert it to something like this:

```scala mdoc
case class Account(id: String, name: String, email: String)
```

With Hammer, you can make this conversion in one function call:

```scala mdoc
import com.melvinlow.hammer.instances.auto.given
import com.melvinlow.hammer.syntax.all.*
import com.melvinlow.hammer.*

entity.hammerTo[Account]
```

## Introduction

In many scenarios, we create case classes that are simpler versions of others. For example, you might have a comprehensive model representation for your database and a leaner version for your API consumers.

Manually constructing these leaner versions can be tedious and error-prone:

```scala mdoc
case class Octagon(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int)
case class Hexagon(b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int)

val octagon = Octagon(1, 2, 3, 4, 5, 6, 7, 8)

val hexagon = Hexagon(octagon.b, octagon.c, octagon.d, octagon.e, octagon.f, octagon.g, octagon.h)
```

Hammer uses generic programming techniques to automate the process:

```scala mdoc
octagon.hammerTo[Hexagon]
```

It can handle missing and swapped fields:

```scala mdoc
case class A(x: String, y: String, z: String)
case class B(z: String, x: String)

A("x", "y", "z").hammerTo[B]
```

And also nested fields:

```scala mdoc
case class CompanyEntity(name: String, createdAt: Instant)
case class PersonEntity(name: String, company: CompanyEntity, createdAt: Instant)

case class Company(name: String)
case class Person(name: String, company: Company)

PersonEntity("John", CompanyEntity("Scala", Instant.now), Instant.now)
  .hammerTo[Person]
```

It can also convert types, such as for auto-unboxing wrapper types:

```scala mdoc
opaque type EmailAddress = String
object EmailAddress {
  def apply(email: String) = email
  extension (email: EmailAddress) def underlying: String = email

  // Define how to hammer an email address to a string
  given Hammer[EmailAddress, String] = (e: EmailAddress) => e.underlying
}

case class Boxed(email: EmailAddress)
case class Unboxed(email: String)

Boxed(EmailAddress("test@example.com")).hammerTo[Unboxed]
```

Importantly, it will error at compile time if conversion is not possible:

```scala mdoc:fail
case class Cat(name: String, voice: "Meow")
case class Dog(name: String, voice: "Bark")

Cat("Peanuts", "Meow").hammerTo[Dog]
```

## Usage

Simply include the correct imports and call the `hammerTo` method:

```scala mdoc:reset
import com.melvinlow.hammer.instances.auto.given
import com.melvinlow.hammer.syntax.all.*
```

## Typeclasses and Extensions

To automatically convert from type `I` to `O`, provide an instance of `Hammer[I, O]`:

```scala
import com.melvinlow.hammer.*

trait Hammer[I, O] {
  def hammer(input: I): O
}
```

For example, you could automatically lift values to `Option`:

```scala mdoc:reset:invisible
import com.melvinlow.hammer.Hammer
import com.melvinlow.hammer.instances.auto.given
import com.melvinlow.hammer.syntax.all.*
```

```scala mdoc
given [I]: Hammer[I, Option[I]] = (i: I) => Some(i)

case class F(x: Int)
case class G(x: Option[Int])

F(1).hammerTo[G]
```

Underneath the hood, Hammer simply looks for a matching field name
where a `Hammer[I, O]` is provided. The default imports include an
identity hammer (i.e., `Hammer[I, I]`) so that if two fields
have the same name and type, they are always compatible.
