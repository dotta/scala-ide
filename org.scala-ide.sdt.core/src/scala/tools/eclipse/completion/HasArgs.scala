package scala.tools.eclipse.completion

object HasArgs extends Enumeration {
  val NoArgs, EmptyArgs, NonEmptyArgs = Value

  /** Given a list of method's parameters it tells if the method
   * arguments should be adorned with parenthesis. */
  def from(params: List[List[_]]) = params match {
    case Nil => NoArgs
    case Nil :: Nil => EmptyArgs
    case _ => NonEmptyArgs
  }
}