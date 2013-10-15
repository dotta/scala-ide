package scala.tools.eclipse.completion

/** Context related to the invocation of the Completion.
  * Can be extended with more context as needed in future
  *
  * @param contextType The type of completion - e.g. Import, method apply
  */
case class CompletionContext(
  invocationOffset: Int,
  contextType: CompletionContext.ContextType)

object CompletionContext {
  trait ContextType
  case object DefaultContext extends ContextType
  case object ApplyContext extends ContextType
  case object ImportContext extends ContextType
}