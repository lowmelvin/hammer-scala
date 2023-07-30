package com.melvinlow.hammer

import scala.compiletime.*
import scala.compiletime.ops.any.*
import scala.compiletime.ops.int.{S => Succ}
import scala.deriving.*

trait Hammer[I, O] {
  def hammer(input: I): O
}

object Hammer {
  inline def apply[I, O](using ev: Hammer[I, O]): Hammer[I, O] = ev

  inline def hammerTo[I, O](input: I)(using hammer: Hammer[I, O]): O = hammer.hammer(input)

  inline def hammerWith[I: Mirror.ProductOf, O: Mirror.ProductOf, L <: Tuple](source: I)
    : AugmentedHammer[I, O, L] = new AugmentedHammer[I, O, L](source)

  private[hammer] final class AugmentedHammer[I, O, L <: Tuple](val source: I) extends AnyVal {
    inline private def helper[Labels, Values <: Tuple](values: Values)(using
      Mirror.ProductOf[I],
      Mirror.ProductOf[O]
    ): O = inline erasedValue[(Labels, Values)] match {
      case _: (EmptyTuple, EmptyTuple) => summonOrMakeProductHammer[I, O].hammer(source)
      case _: (l *: lTail, v *: _) =>
        given e: Extractor[I, l, v] = (_: I) => values.productElement(0).asInstanceOf[v]
        helper[lTail, Tuple.Drop[Values, 1]](values.drop(1))
      case _ => error("Mismatched labels and values.")
    }

    inline def apply[Values <: Tuple](values: Values)(using
      Mirror.ProductOf[I],
      Mirror.ProductOf[O]
    ): O = helper[L, Values](values)
  }

  inline private def summonOrMakeProductHammer[I: Mirror.ProductOf, O: Mirror.ProductOf]
    : Hammer[I, O] =
    summonFrom {
      case h: Hammer[I, O] => h
      case _               => makeProductHammer[I, O]
    }

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

      override def extract(source: S): O = {
        val value = source.asInstanceOf[Product].productElement(idx)
        hammer.hammer(value.asInstanceOf)
      }
    }

  inline private def summonOrMakeExtractor[S: Mirror.ProductOf, L, O]: Extractor[S, L, O] =
    summonFrom {
      case e: Extractor[S, L, O] => e
      case _                     => makeExtractor[S, L, O]
    }

  inline private def makeExtractors[S: Mirror.ProductOf, Labels <: Tuple, Outputs <: Tuple]
    : List[Extractor[S, ?, ?]] =
    inline erasedValue[(Labels, Outputs)] match {
      case _: (l *: tailL, o *: tailO) =>
        summonOrMakeExtractor[S, l, o] :: makeExtractors[S, tailL, tailO]
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
