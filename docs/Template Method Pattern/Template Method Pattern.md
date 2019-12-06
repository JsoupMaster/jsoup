# Template Method Pattern

`Jsoup`에서 `Template Method Pattern`이 적용된 부분은 다음과 같다.
- `parser` Package
  - `TreeBuilder`

## TreeBuilder

`TreeBuilder`는 토큰들로부터 DOM을 생성한다.
이때, Html DOM Rule에 따라 Html을 파싱하려면, `HtmlTreeBuilder`를 이용하고,
Html DOM Rule을 전혀 적용하지 않고 Xml을 파싱하려면, `XmlTreeBuilder`를 이용한다.

![TreeBuilder Template Method Pattern Class Diagram](https://user-images.githubusercontent.com/19348185/68832840-53bef880-06f5-11ea-9895-e37ebd6645dd.png)


### 상응하는 코드 및 작동 방식 설명 (코드 대응해서)

먼저, `TreeBuilder`의 `runParser()`에서 파싱을 시작한다.

```java
abstract class TreeBuilder {
// other codes
    Document parse(Reader input, String baseUri, Parser parser) {
        initialiseParse(input, baseUri, parser);
        runParser();
        return doc;
    }

    protected void runParser() {
        while (true) {
        Token token = tokeniser.read();
        process(token);
        token.reset();

        if (token.type == Token.TokenType.EOF)
            break;
        }
    }

    protected abstract boolean process(Token token);
}
```

그리고, `HtmlTreeBuilder`는 `TreeBuilder`를 상속받아 `process()`를 구현한다.

```java
public class HtmlTreeBuilder extends TreeBuilder {
  // other codes

  @Override
  protected boolean process(Token token) {
    currentToken = token;
    return this.state.process(token, this);
  }
}
```

`XmlTreeBuilder` 또한, `TreeBuilder`의 `process()`를 구현하고, `initialiseParse()`를 override 한다.

```java
public class XmlTreeBuilder extends TreeBuilder {

    @Override
    protected boolean process(Token token) {
        ...
    }
}
```



### 판단 근거

![Template Method Pattern Class Diagram](https://user-images.githubusercontent.com/47529632/70235991-5f0ebc80-17a7-11ea-82de-8e1b9505ccef.PNG)

한편, `TreeBuilder`의 클래스 다이어그램은 전형적인 `Template Method Pattern`의 클래스 다이어그램과 동일했다.

Template Method 패턴은 Abstract에서 알고리즘의 framework를 정해놓고, 그 알고리즘의 실제 behavior들을 그 하위 클래스에서 정의하도록 한 패턴이다.

TreeBuilder 클래스 또한 parsing 알고리즘의 framework를 `parse()`와 `runParser()`를 통해 정해놓고 `process()`를 하위 클래스들 HtmlTreeBuilder와 XmlTreeBuilder에서 각자 정의하도록 한다.

한편, `HtmlTreeBuilder`와 `XmlTreeBuilder` 모두 파싱 알고리즘의 동일한 정해진 잘 변하지 않는 틀에 따라 진행되지만, 구체적으로는 `HtmlTreeBuilder`은 Html DOM Rule에 따라 Html을 파싱하기 위한 것이고, `XmlTreeBuilder`은 Html DOM Rule을 전혀 적용하지 않고 Xml을 파싱하기 위한 것으로 알고리즘의 세부적인 스텝은 다르다. 

이 상황은 Template Method Pattern을 적용할 때의 문제상황과 일치하며, 따라서 Template Method Pattern을 적용한 것은 적절하다고 볼 수 있다.