class Foo {
  def foo: AliasedCustomException = throw new CustomException 
  private type AliasedCustomException = CustomException

  throw new AliasedCustomException()
}

class CustomException/*ref*/ extends RuntimeException