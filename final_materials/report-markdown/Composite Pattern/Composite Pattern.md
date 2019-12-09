# Composite Pattern

`Jsoup`에서 `Composite Pattern`이 적용된 부분은 다음과 같다.
- `nodes` Package
  - `Node`

## Node

`Node`는  base, abstract Node model로서, Elements, Documents, Comments등이 모두 `Node`의 Instance이다.

Node Composite Pattern Class Diagram

![Node Composite Pattern Class Diagram](https://user-images.githubusercontent.com/47529632/70321792-a65e8100-186b-11ea-9db8-d5c99ef35cb5.png)

### 상응하는 코드 및 판단 근거

Composite Pattern Class Diagram

![Composite Pattern Class Diagram](https://user-images.githubusercontent.com/47529632/70239159-232b2580-17ae-11ea-9977-2d8ff187a417.PNG)

```java
public class Element extends Node {
    private static final List<Node> EMPTY_NODES = Collections.emptyList();
}

abstract class LeafNode extends Node {
    private static final List<Node> EmptyNodes = Collections.emptyList();
}

public class PseudoTextElement extends Element {    
    ...
}

public class TextNode extends LeafNode {
    ...
}



```

`Element`와 `LeafNode`는 `Node` Class의 Children class이다. `Element`와 `LeafNode` 모두 `Node`를 포함하는 list를 가지고 있다.
또한, `pseudoTextElement`, `TextNode`와 같은 몇몇 다른 클래스들이 `Element`와 `LeafNode`를 상속받는다.

따라서 Composite 패턴의 관점에서 보았을 때, `Node`는 Component에 해당하며, `Element`와 `LeafNode`는 Composite class에 해당한다.

