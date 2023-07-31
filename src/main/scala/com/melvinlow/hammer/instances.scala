package com.melvinlow.hammer

import com.melvinlow.hammer.Hammer.*

import scala.deriving.Mirror

object instances {
  object internal {
    given identityHammer[I]: Hammer[I, I] with {
      inline def hammer(input: I): I = input
    }

    inline given [I: Mirror.ProductOf, O: Mirror.ProductOf]: Hammer[I, O] = makeProductHammerMacro()
  }

  object semiauto {
    export internal.identityHammer
  }

  object auto {
    export internal.given
  }
}
