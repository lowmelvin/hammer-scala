package com.melvinlow.hammer

import scala.deriving.*

object syntax {
  object internal {
    extension [I](input: I) {
      inline def hammerTo[O](using hammer: Hammer[I, O]): O = hammer.hammer(input)

      inline def hammerWith[O](inline args: Patch[?, ?]*)(using
        Mirror.ProductOf[I],
        Mirror.ProductOf[O]
      ): O = Hammer.hammerWith(input, args*)
    }
  }

  object all {
    export internal.*
  }
}
