# Strategy Pattern

Jsoup에서 Stategy Pattern이 적용된 부분은 다음과 같다.
- `parser` Package
  - `Parser`

- `select` Package
  - `NodeTraversor`

## Parser


`Parser` 클래스는 Html과 Xml을 파싱하여 Document를 만든다. 이때, Html과 Xml 중 어느 것을 파싱하냐에 따라 Document의 트리 구조가 달라져야한다. 따라서 토큰들을 통해 DOM 트리를 만드는 `TreeBuilder` 알고리즘을 갈아끼움으로써  Html과 Xml 각각에 대한 다른 트리 구조의 Document를 만든다. 

![Parser Strategy Pattern Class Diagram](https://user-images.githubusercontent.com/47529632/70214817-6d48e280-177f-11ea-839c-791284d36842.PNG)



### 상응하는 코드 및 작동 방식 설명 (코드 대응해서)

``` java 
public class Parser {
    private TreeBuilder treeBuilder;

    public Parser(TreeBuilder treeBuilder) {
        this.treeBuilder = treeBuilder;
        settings = treeBuilder.defaultSettings();
        errors = ParseErrorList.noTracking();
    }

    public TreeBuilder getTreeBuilder() {
        return treeBuilder;
    }
    
    public Parser setTreeBuilder(TreeBuilder treeBuilder) {
        this.treeBuilder = treeBuilder;
        treeBuilder.parser = this;
        return this;
    }
```

먼저, `Parser` 클래스는Html과 Xml을 파싱하여 Document를 만드는데, 이때, 토큰들을 통해 DOM 트리를 만드는 `TreeBuilder` 알고리즘을 갈아끼움으로써  Html과 Xml 각각에 대한 다른 구조의 트리를 만든다. 이를 위해 `TreeBuilder` 형의 변수 `treeBuilder`가 선언되어 있다. 

`Parser` 클래스를 생성할 때 원하는 `treeBuilder`를 받음으로써, 또는 `setTreeBuilder()` 함수를 통해 원하는 `treeBuilder`를 정할 수 있으며, `getTreeBuilder()` 함수를 통해 현재 setting되어 있는 `TreeBuilder`를 얻을 수도 있다.

``` java
abstract class TreeBuilder {
    protected void initialiseParse(Reader input, String baseUri, Parser parser) {
        Validate.notNull(input, "String input must not be null");
        Validate.notNull(baseUri, "BaseURI must not be null");

        doc = new Document(baseUri);
        doc.parser(parser);
        this.parser = parser;
        settings = parser.settings();
        reader = new CharacterReader(input);
        currentToken = null;
        tokeniser = new Tokeniser(reader, parser.getErrors());
        stack = new ArrayList<>(32);
        this.baseUri = baseUri;
    }

    abstract List<Node> parseFragment(String inputFragment, Element context, String baseUri, Parser parser);

    protected abstract boolean process(Token token);
 }
```


`TreeBuilder`에는 여러 TreeBuilder에서 parseFragment, process함수를 반드시 구현하도록 abstract method로서 선언했으며, 
공통적으로 쓰이는 함수 initialiseParse를 proteceted로 선언하였다.

``` java 
public class HtmlTreeBuilder extends TreeBuilder {
    @Override
    protected void initialiseParse(Reader input, String baseUri, 
                                    Parser parser) {
        super.initialiseParse(input, baseUri, parser);

        // this is a bit mucky. todo - probably just create new parser objects to ensure all reset.
        state = HtmlTreeBuilderState.Initial;
        originalState = null;
        baseUriSetFromDoc = false;
        headElement = null;
        formElement = null;
        contextElement = null;
        formattingElements = new ArrayList<>();
        pendingTableCharacters = new ArrayList<>();
        emptyEnd = new Token.EndTag();
        framesetOk = true;
        fosterInserts = false;
        fragmentParsing = false;
    }

    List<Node> parseFragment(String inputFragment, Element context, String baseUri, Parser parser) {
        // context may be null
        state = HtmlTreeBuilderState.Initial;
        initialiseParse(new StringReader(inputFragment), baseUri, parser);
        contextElement = context;
        fragmentParsing = true;
        Element root = null;

        if (context != null) {
            if (context.ownerDocument() != null) // quirks setup:
                doc.quirksMode(context.ownerDocument().quirksMode());

            // initialise the tokeniser state:
            String contextTag = context.tagName();
            if (StringUtil.in(contextTag, "title", "textarea"))
                tokeniser.transition(TokeniserState.Rcdata);
            else if (StringUtil.in(contextTag, "iframe", "noembed", "noframes", "style", "xmp"))
                tokeniser.transition(TokeniserState.Rawtext);
            else if (contextTag.equals("script"))
                tokeniser.transition(TokeniserState.ScriptData);
            else if (contextTag.equals(("noscript")))
                tokeniser.transition(TokeniserState.Data); // if scripting enabled, rawtext
            else if (contextTag.equals("plaintext"))
                tokeniser.transition(TokeniserState.Data);
            else
                tokeniser.transition(TokeniserState.Data); // default

            root = new Element(Tag.valueOf("html", settings), baseUri);
            doc.appendChild(root);
            stack.add(root);
            resetInsertionMode();

            // setup form element to nearest form on context (up ancestor chain). ensures form controls are associated
            // with form correctly
            Elements contextChain = context.parents();
            contextChain.add(0, context);
            for (Element parent: contextChain) {
                if (parent instanceof FormElement) {
                    formElement = (FormElement) parent;
                    break;
                }
            }
        }

        runParser();
        if (context != null)
            return root.childNodes();
        else
            return doc.childNodes();
    }

    @Override
    protected boolean process(Token token) {
        currentToken = token;
        return this.state.process(token, this);
    }

}
```

`TreeBuilder`를 상속받고 있는 `HtmlTreeBuilder`는 TreeBuilder에 대한 Concrete Class이다.

또한, `TreeBuilder`에서 정의한 `initialiseParse()` 메소드를 Override하여 `HtmlTreeBuilder`에 맞게 정의하였으며, `TreeBuilder`에서 정의한 추상메소드 `parseFragment()`와 `process()`를 `HtmlTreeBuilder`에 맞게 구현했다.

``` java 
public class XmlTreeBuilder extends TreeBuilder {
   @Override
    protected void initialiseParse(Reader input, String baseUri, Parser parser) {
        super.initialiseParse(input, baseUri, parser);
        stack.add(doc); // place the document onto the stack. differs from HtmlTreeBuilder (not on stack)
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
    }

    List<Node> parseFragment(String inputFragment, Element context, String baseUri, Parser parser) {
        return parseFragment(inputFragment, baseUri, parser);
    }

    @Override
    protected boolean process(Token token) {
        // start tag, end tag, doctype, comment, character, eof
        switch (token.type) {
            case StartTag:
                insert(token.asStartTag());
                break;
            case EndTag:
                popStackToClose(token.asEndTag());
                break;
            case Comment:
                insert(token.asComment());
                break;
            case Character:
                insert(token.asCharacter());
                break;
            case Doctype:
                insert(token.asDoctype());
                break;
            case EOF: // could put some normalisation here if desired
                break;
            default:
                Validate.fail("Unexpected token type: " + token.type);
        }
        return true;
    }
}
```

`TreeBuilder`를 상속받고 있는 `XmlTreeBuilder` 또한 TreeBuilder에 대한 Concrete Class이다.

그리고 `TreeBuilder`에서 정의한 `initialiseParse()` 메소드를 Override하여 `XmlTreeBuilder`에 맞게 정의하였으며, `TreeBuilder`에서 정의한 추상메소드 `parseFragment()`와 `process()`를 `XmlTreeBuilder`에 맞게 구현했다.

``` java
public class Parser {
    public Document parseInput(String html, String baseUri) {
        return treeBuilder.parse(new StringReader(html), baseUri, this);
    }

    public Document parseInput(Reader inputHtml, String baseUri) {
        return treeBuilder.parse(inputHtml, baseUri, this);
    }

    public List<Node> parseFragmentInput(String fragment, Element context, String baseUri) {
        return treeBuilder.parseFragment(fragment, context, baseUri, this);
    }
}

```

따라서 `parser`에서 교체된 `TreeBuilder`에 맞게 `parse()`, `parseFragment()`를 호출함으로써 parsing을 수행하게 된다.

### 판단 근거

Strategy Pattern Class Diagram
![Strategy Pattern Class Diagram](https://user-images.githubusercontent.com/47529632/70216012-0da00680-1782-11ea-9e19-72dcc3bb18a7.PNG)

한편, `parser` 및 `TreeBuilder` 의 클래스 다이어그램은 전형적인 State Pattern의 클래스 다이어그램과 동일했다.

`parser`를 context 즉, client 클래스로 보고, `TreeBuilder`를 abstract Strategy로 그리고 `HTMLTreeBuilder`, `XmlTreeBuilder` 각각을 ConcreteStrategy로 볼 수 있다.

Strategy 패턴은 교환될 수 있는 특정 알고리즘들을 encapsulate시켜 Client로부터 분리하기 위한 목적으로 사용한다. 따라서 새로운 algorithm을 추가하거나 기존의 알고리즘들을 삭제 및 수정하기 쉬워진다.

한편, `parser` 클래스에서도 TreeBuilder를 바꿈으로써 알고리즘을 교체하여, Html에 맞게 트리를 짜거나, Xml에 맞게 트리를 짠다. 만약 Html과 Xml외에 새로운 형식을 parsing하는 기능이 Jsoup에 추가 된다면, 또는 기존의 Html, Xml에 맞는 트리만드는 방식이 바뀔 때 Client에 영향을 미치지 않느다. 

따라서 `parser`, `TreeBuilder`, `HtmlTreeBuilder`, `XmlTreeBuilder` 클래스의 경우, Strategy 패턴을 사용한 것은 적절하다.




## NodeTraversal 

`NodeTraversal`은 Depth-first node traversor이다. Dom Tree의 지정한 root node 아래의 모든 노드를 iterate하기 위해 사용한다.

![NodeTraversor Strategy Pattern Class Diagram](https://user-images.githubusercontent.com/47529632/70286787-d4ae7300-180f-11ea-9dbe-91259538fbe5.PNG)

### 상응하는 코드 및 작동 방식 설명 (코드 대응해서)

``` java 
public class NodeTraversor {
    private NodeVisitor visitor;

    public NodeTraversor(NodeVisitor visitor) {
        this.visitor = visitor;
    }

    public static void traverse(NodeVisitor visitor, Node root) {
        Node node = root;
        int depth = 0;
        
        while (node != null) {
            visitor.head(node, depth);
            if (node.childNodeSize() > 0) {
                node = node.childNode(0);
                depth++;
            } else {
                while (node.nextSibling() == null && depth > 0) {
                    visitor.tail(node, depth);
                    node = node.parentNode();
                    depth--;
                }
                visitor.tail(node, depth);
                if (node == root)
                    break;
                node = node.nextSibling();
            }
        }
    }

     public static FilterResult filter(NodeFilter filter, Node root) {
        Node node = root;
        int depth = 0;

        while (node != null) {
            FilterResult result = filter.head(node, depth);
            if (result == FilterResult.STOP)
                return result;
            // Descend into child nodes:
            if (result == FilterResult.CONTINUE && node.childNodeSize() > 0) {
                node = node.childNode(0);
                ++depth;
                continue;
            }
            // No siblings, move upwards:
            while (node.nextSibling() == null && depth > 0) {
                // 'tail' current node:
                if (result == FilterResult.CONTINUE || result == FilterResult.SKIP_CHILDREN) {
                    result = filter.tail(node, depth);
                    if (result == FilterResult.STOP)
                        return result;
                }
                Node prev = node; // In case we need to remove it below.
                node = node.parentNode();
                depth--;
                if (result == FilterResult.REMOVE)
                    prev.remove(); // Remove AFTER finding parent.
                result = FilterResult.CONTINUE; // Parent was not pruned.
            }
            // 'tail' current node, then proceed with siblings:
            if (result == FilterResult.CONTINUE || result == FilterResult.SKIP_CHILDREN) {
                result = filter.tail(node, depth);
                if (result == FilterResult.STOP)
                    return result;
            }
            if (node == root)
                return result;
            Node prev = node; // In case we need to remove it below.
            node = node.nextSibling();
            if (result == FilterResult.REMOVE)
                prev.remove(); // Remove AFTER finding sibling.
        }
        // root == null?
        return FilterResult.CONTINUE;
    }
}
```
`NodeTraversor`에서는 정해진 NodeVisitor와 NodeFilter의 객체에 따라 `traverse()`와 `filter()`를 통해 노드를 iterate하거나 filtering한다.


``` java
public interface NodeVisitor {
    void head(Node node, int depth);
    void tail(Node node, int depth);
}

public interface NodeFilter {
    FilterResult head(Node node, int depth);
    FilterResult tail(Node node, int depth);
}
```

한편, `NodeVisitor`와 `NodeFilter`는 interface로, 각각 `head()`와 `tail()`을 선언했다.

``` java 
public abstract class Node implements Cloneable {
        public Node traverse(NodeVisitor nodeVisitor) {
        Validate.notNull(nodeVisitor);
        NodeTraversor.traverse(nodeVisitor, this);
        return this;
    }

    public void setBaseUri(final String baseUri) {
        Validate.notNull(baseUri);

        traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                node.doSetBaseUri(baseUri);
            }

            public void tail(Node node, int depth) {
            }
        });
    }
}
```

그리고, `Node`에서 `NodeVisitor`의 생성과 동시에 `head()`와 `tail()`을 동적으로 정의해준 후, `NodeTraversor`를 통해 `traverse()`를 수행한다.

### 판단 근거

Strategy Pattern Class Diagram
![Strategy Pattern Class Diagram](https://user-images.githubusercontent.com/47529632/70216012-0da00680-1782-11ea-9e19-72dcc3bb18a7.PNG)

한편, `NodeTraversor`의 클래스 다이어그램은 전형적인 Strategy 패턴의 클래스 다이어그램과 유사하다.

무엇보다 클래스 다이어그램을 보고 판단하기 전에, 동작 과정(매커니즘)을 보고 Strategy 패턴으로 추측했다. 즉, `NodeTraversor`에 `traverse()`와 `filter()`에서 `head()`와 `tail()` 알고리즘을 사용하는데, 이 알고리즘은 `traverse()`와 `filter()` 함수에서 전달받은 `NodeVisitor`와 `NodeFilter`에 따라 다르게 작동한다. 이 `NodeVisitor`와 `NodeFilter`는 `head()`와 `tail()`이 선언되어 있는 interface로 `Node`에서 `NodeTraversor`의 `traverse()`와 `filter()`를 사용하기 전에 `NodeVisitor`와 `NodeFilter`를 만들면서 동시에 `head()`와 `tail()`을 정의, 삽입하여, 다르게 알고리즘이 동작하게 한다.

따라서 이는 Strategy 패턴이 적용된 것으로 볼 수 있다.