# Hammer-Scala

Hammer-Scala is a Scala 3 utility library that allows quick and painless
data conversion between different product types.

To include Hammer in your project, add the following to your dependencies:

```scala
libraryDependencies += "com.melvinlow" %% "hammer" % <version>
```

## Quick Start


Assume you have an Algebraic Data Type (ADT) like this:

```scala
case class AccountEntity(id: String, email: String, name: String, secret: String, createdAt: Instant)

val entity = AccountEntity("123", "test@example.com", "nobo", "should-be-hashed", Instant.now)
```

And you want to convert it to something like this:

```scala
case class Account(id: String, name: String, email: String)
```

With Hammer, you can make this conversion in one function call:

```scala
import com.melvinlow.hammer.instances.auto.given
import com.melvinlow.hammer.syntax.all.*
import com.melvinlow.hammer.*

entity.hammerTo[Account]
// res0: Account = Account(
//   id = "123",
//   name = "nobo",
//   email = "test@example.com"
// )
```

## Introduction

In many scenarios, we create case classes that are simpler versions of others. For example, you might have a comprehensive model representation for your database and a leaner version for your API consumers.

Manually constructing these leaner versions can be tedious and error-prone:

```scala
case class Octagon(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int)
case class Hexagon(b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int)

val octagon = Octagon(1, 2, 3, 4, 5, 6, 7, 8)
// octagon: Octagon = Octagon(
//   a = 1,
//   b = 2,
//   c = 3,
//   d = 4,
//   e = 5,
//   f = 6,
//   g = 7,
//   h = 8
// )

val hexagon = Hexagon(octagon.b, octagon.c, octagon.d, octagon.e, octagon.f, octagon.g, octagon.h)
// hexagon: Hexagon = Hexagon(b = 2, c = 3, d = 4, e = 5, f = 6, g = 7, h = 8)
```

Hammer uses generic programming techniques to automate the process:

```scala
octagon.hammerTo[Hexagon]
// res1: Hexagon = Hexagon(b = 2, c = 3, d = 4, e = 5, f = 6, g = 7, h = 8)
```

It can handle missing and swapped fields:

```scala
case class A(x: String, y: String, z: String)
case class B(z: String, x: String)

A("x", "y", "z").hammerTo[B]
// res2: B = B(z = "z", x = "x")
```

And also nested fields:

```scala
case class CompanyEntity(name: String, createdAt: Instant)
case class PersonEntity(name: String, company: CompanyEntity, createdAt: Instant)

case class Company(name: String)
case class Person(name: String, company: Company)

PersonEntity("John", CompanyEntity("Scala", Instant.now), Instant.now)
  .hammerTo[Person]
// res3: Person = Person(name = "John", company = Company(name = "Scala"))
```

It can also convert types, such as for auto-unboxing wrapper types:

```scala
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
// res4: Unboxed = Unboxed(email = "test@example.com")
```

Importantly, it will error at compile time if conversion is not possible:

```scala
case class Cat(name: String, voice: "Meow")
case class Dog(name: String, voice: "Bark")

Cat("Peanuts", "Meow").hammerTo[Dog]
// error: 
// No given instance of type com.melvinlow.hammer.Hammer[("Meow" : String), ("Bark" : String)] was found.
// I found:
// 
//     com.melvinlow.hammer.instances.auto.given_Hammer_I_O[("Meow" : String),
//       ("Bark" : String)](
//       /* missing */summon[deriving.Mirror.ProductOf[("Meow" : String)]], ???)
// 
// But Failed to synthesize an instance of type deriving.Mirror.ProductOf[("Meow" : String)]: class String is not a generic product because it is not a case class.
```

## Usage

Simply include the correct imports and call the `hammerTo` method:

```scala
import com.melvinlow.hammer.instances.auto.given
import com.melvinlow.hammer.syntax.all.*
```

## Advanced Usage

Hammer includes patching functionality that allows you to override
specific fields. To do this, call the `hammerWith` extension method:

```scala
case class HumanEntity(name: String, age: Int)
case class Human(name: String, age: Int)

// Override the name field
HumanEntity("Hami", 18).hammerWith[Human](Patch["name"]("Arno"))
// res6: Human = Human(name = "Arno", age = 18)
```

The same method can also be used to convert a case class to a richer case class
by providing the values of the missing fields:

```scala
case class Engineer(name: String)
case class EngineerEntity(name: String, createdAt: Instant, updatedAt: Instant)

Engineer("Lynn").hammerWith[EngineerEntity](
  Patch["createdAt"](Instant.now),
  Patch["updatedAt"](Instant.now)
)
// res7: EngineerEntity = EngineerEntity(
//   name = "Lynn",
//   createdAt = 2023-07-31T06:14:57.426081Z,
//   updatedAt = 2023-07-31T06:14:57.426083Z
// )
```

As shown, you are required to provide the desired output type (can be inferred) along with a
variable number of `Patch` objects that each contain the field name and value to inject.
The `Patch` objects can be specified in any order.

For comparison, these two usages are equivalent:

```scala
octagon.hammerTo[Hexagon]
// res8: Hexagon = Hexagon(b = 2, c = 3, d = 4, e = 5, f = 6, g = 7, h = 8)

octagon.hammerWith[Hexagon]()
// res9: Hexagon = Hexagon(b = 2, c = 3, d = 4, e = 5, f = 6, g = 7, h = 8)
```

Finally, note that only top level patching is supported--it is not possible
to inject a value somewhere deep into a nested case class.

## Typeclasses and Extensions

To automatically convert from type `I` to `O`, provide an instance of `Hammer[I, O]`:

```scala
import com.melvinlow.hammer.*

trait Hammer[I, O] {
  def hammer(input: I): O
}
```

For example, you could automatically lift values to `Option`:


```scala
given [I]: Hammer[I, Option[I]] = (i: I) => Some(i)

case class F(x: Int)
case class G(x: Option[Int])

F(1).hammerTo[G]
// res11: G = G(x = Some(value = 1))
```

Underneath the hood, Hammer simply looks for a matching field name
where a `Hammer[I, O]` is provided. The default imports include an
identity hammer (i.e., `Hammer[I, I]`) so that if two fields
have the same name and type, they are always compatible.
