package com.melvinlow.hammer

object syntax {
  object internal {
    extension [I](input: I) {
      inline def hammerTo[O](using hammer: Hammer[I, O]): O = hammer.hammer(input)
    }
  }

  object all {
    export internal.*
  }
}
