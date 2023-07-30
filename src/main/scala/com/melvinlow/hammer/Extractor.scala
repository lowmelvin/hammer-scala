package com.melvinlow.hammer

trait Extractor[S, L, O] {
  def extract(source: S): O
}

object Extractor {
  def apply[S, L, O](using ev: Extractor[S, L, O]): Extractor[S, L, O] = ev
}
