class Foo {
  def foo: AliasedCustomException = throw new exception.CustomException
  private type AliasedCustomException = exception.CustomException

  throw new AliasedCustomException/*ref*/()
}