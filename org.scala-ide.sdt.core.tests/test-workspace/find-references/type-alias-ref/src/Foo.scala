class Foo {
  private type AliasedCustomException = exception.CustomException

  throw new AliasedCustomException/*ref*/()
}