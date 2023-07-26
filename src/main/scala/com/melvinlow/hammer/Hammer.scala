package com.melvinlow.hammer

import scala.compiletime.*
import scala.compiletime.ops.any.*
import scala.compiletime.ops.int.{S => Succ}
import scala.deriving.*

trait Hammer[I, O] {
  def hammer(input: I): O
}

object Hammer {
  def apply[I, O](using ev: Hammer[I, O]): Hammer[I, O] = ev

  inline private def summonHammerAndIdx[Labels <: Tuple, Types <: Tuple, L, O, Index <: Int]
    : (Hammer[?, O], Int) =
    inline erasedValue[(Labels, Types)] match {
      case _: (L *: tailL, t *: tailT) => summonInline[Hammer[t, O]] -> constValue[Index]
      case _: (_ *: tailL, _ *: tailT) => summonHammerAndIdx[tailL, tailT, L, O, Succ[Index]]
      case _                           => error("Could not find label.")
    }

  inline private def makeExtractor[S, L, O](using m: Mirror.ProductOf[S]): Extractor[S, L, O] =
    new Extractor[S, L, O] {
      lazy val (hammer, idx) =
        summonHammerAndIdx[m.MirroredElemLabels, m.MirroredElemTypes, L, O, 0]

      override def extract[L](source: S): O = {
        val value = source.asInstanceOf[Product].productElement(idx)
        hammer.hammer(value.asInstanceOf)
      }
    }

  inline private def makeExtractors[S: Mirror.ProductOf, Labels <: Tuple, Outputs <: Tuple]
    : List[Extractor[S, ?, ?]] =
    inline erasedValue[(Labels, Outputs)] match {
      case _: (l *: tailL, o *: tailO) => makeExtractor[S, l, o] :: makeExtractors[S, tailL, tailO]
      case _: (EmptyTuple, EmptyTuple) => Nil
      case _                           => error("Could not make extractors.")
    }

  inline def makeProductHammer[S, O](using
    ms: Mirror.ProductOf[S],
    mo: Mirror.ProductOf[O]
  ): Hammer[S, O] =
    new Hammer[S, O] {
      lazy val extractors = makeExtractors[S, mo.MirroredElemLabels, mo.MirroredElemTypes]

      override def hammer(input: S): O = {
        val outputs = extractors.map(_.extract(input))
        mo.fromProduct(Tuple.fromArray(outputs.toArray).asInstanceOf)
      }
    }
}
