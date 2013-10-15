package scala.tools.eclipse.completion

/** A completion proposal coming from the Scala compiler. This
 *  class holds together data about completion proposals.
 *
 *  This class is independent of both the Scala compiler (does not
 *  know about Symbols and Types), and the UI elements used to
 *  display it to the user.
 *
 *  @note Parameter names are retrieved lazily, since the operation is potentially long-running.
 *  @see  ticket #1001560
 */
case class CompletionInfo(
  kind: MemberKind.Value,
  context: CompletionContext,
  startPos: Int,             // position where the 'completion' string should be inserted
  completion: String,        // the string to be inserted in the document
  display: String,           // the display string in the completion list
  displayDetail: String,     // additional details to be display in the completion list (like package for a class)
  relevance: Int,
  isJava: Boolean,
  getParamNames: () => List[List[String]], // parameter names (excluding any implicit parameter sections)
  paramTypes: List[List[String]],          // parameter types matching parameter names (excluding implicit parameter sections)
  fullyQualifiedName: String, // for Class, Trait, Type, Objects: the fully qualified name
  needImport: Boolean        // for Class, Trait, Type, Objects: import statement has to be added
) {

  /** Return the tooltip displayed once a completion has been activated. */
  def tooltip: String = {
    val contextInfo = for {
      (names, tpes) <- getParamNames().zip(paramTypes)
    } yield for { (name, tpe) <- names.zip(tpes) } yield "%s: %s".format(name, tpe)

    contextInfo.map(_.mkString("(", ", ", ")")).mkString("")
  }
}