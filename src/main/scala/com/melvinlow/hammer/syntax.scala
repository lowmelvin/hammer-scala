package com.melvinlow.hammer

import scala.deriving.*

object syntax {
  object internal {
    extension [I](input: I) {
      inline def hammerTo[O](using hammer: Hammer[I, O]): O = hammer.hammer(input)

      inline def hammerWith[O, L <: Tuple](using
        Mirror.ProductOf[I],
        Mirror.ProductOf[O]
      ): Hammer.AugmentedHammer[I, O, L] =
        new Hammer.AugmentedHammer[I, O, L](input)
    }
  }

  object all {
    export internal.*
  }
}
