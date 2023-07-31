package com.melvinlow.hammer

final class Patch[K <: String, V](val underlying: V) extends AnyVal

object Patch {
  inline def apply[K <: String] = new AugmentedPatch[K]

  private[hammer] final class AugmentedPatch[K <: String](val dummy: Boolean = true)
      extends AnyVal {
    inline def apply[V](inline value: V): Patch[K, V] = new Patch[K, V](value)
  }
}
