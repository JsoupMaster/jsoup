# State Pattern

Jsoup에서 State Pattern이 적용된 부분은 다음과 같다.
- `parser` Package
  - `TokeniserState`
  - `HtmlTreeBuilderState` 

## TokeniserState


`Tokeniser` 클래스는 input stream을 통해서 읽어들인 데이터를 token으로 분류한다. 이때 `tokeniser`의 state를 바꿔가며,  token의 type을 결정한다.

![TokeniserState Class Diagram](https://user-images.githubusercontent.com/47529632/70194068-9435f300-1744-11ea-8857-6c0df076c611.PNG)

![TokeniserState Class Diagram Real](https://user-images.githubusercontent.com/47529632/70194067-9435f300-1744-11ea-8dc8-68ff78ee0c7f.png)


### 상응하는 코드 및 작동 방식 설명 (코드 대응해서)

``` java 
final class Tokeniser {
    private final CharacterReader reader; // html input

    private TokeniserState state = TokeniserState.Data; // current tokenisation state
    
    TokeniserState getState() {
            return state;
        }

    void transition(TokeniserState state) {
        this.state = state;
    }

    void advanceTransition(TokeniserState state) {
        reader.advance();
        this.state = state;
    }
```

먼저, `Tokeniser` 클래스는 reader를 통해 Input Stream으로부터 데이터를 읽어오는데, 이때, 이 데이터를 특정 토큰으로 구분하기 위해 State를 바꾸며 검사하는데, 자신의 현재 state를 저장하기 위해 `TokeniserState`형의 state변수가 있다. 



그리고 현재 state를 얻기 위한 함수 `getState()`와 state를 바꾸기 위한 함수 `transition()`, `advanceTransition()`를 제공한다.


``` java
 enum HtmlTreeBuilderState {
    TagOpen {
        // from < in data
        void read(Tokeniser t, CharacterReader r) {
            switch (r.current()) {
                case '!':
                    t.advanceTransition(MarkupDeclarationOpen);
                    break;
                case '/':
                    t.advanceTransition(EndTagOpen);
                    break;
                case '?':
                    t.advanceTransition(BogusComment);
                    break;
                default:
                    if (r.matchesLetter()) {
                        t.createTagPending(true);
                        t.transition(TagName);
                    } else {
                        t.error(this);
                        t.emit('<'); // char that got us here
                        t.transition(Data);
                    }
                    break;
            }
        }
    },
    EndTagOpen {
        ...
    }, 
    RcdataLessthanSign {
        ...
    }, 
    ...
 }
```

`TokeniserState`에는 state별로 토큰을 구분하는 규칙이 정의되어 있다. 즉, 현재 state에서 예상되는 값이 들어왔을 때 또는 예상되지 않은 값이 들어왔을 때 어떤 state로 transition 시켜주는 방식으로 구분 규칙을 정의한다.

이때, 현재 state를 알기 위해  `getState()`, `Tokeniser`의 state를 전환하기 위해 `transition()`, `advanceTransition()` 등 `Tokeniser`에 정의되어 있는 함수를 이용한다.

``` java 
Token read() {
        while (!isEmitPending)
            state.read(this, reader);

        // if emit is pending, a non-character token was found: return any chars in buffer, and leave token for next read:
        if (charsBuilder.length() > 0) {
            String str = charsBuilder.toString();
            charsBuilder.delete(0, charsBuilder.length());
            charsString = null;
            return charPending.data(str);
        } else if (charsString != null) {
            Token token = charPending.data(charsString);
            charsString = null;
            return token;
        } else {
            isEmitPending = false;
            return emitPending;
        }
    }
```

따라서 `Tokeniser`에서는 현재 state에 따른 `read()`를 반복적으로 호출함으로써, Token을 인식 및 구분할 수 있다. 

현재 state에 따른 read함수를 반복적으로 호출하는 작업은 `Tokeniser` 클래스의 `read()`에서 이루어진다.

### 판단 근거

State Pattern Class Diagram
![State Pattern Class Diagram](https://user-images.githubusercontent.com/47529632/70195202-6652ad80-1748-11ea-9c66-9533bf7f23d7.PNG)

한편, `Tokeniser State`의 클래스 다이어그램은 전형적인 State Pattern의 클래스 다이어그램과 동일했다.

State Pattern에서 Context가 state를 바꾸며, 다른 작업을 수행하는 동작 방식 또한, `Tokeniser`가 `TokeniserState`를 바꾸며 동작하는 방식과 동일했다.

구현에서는 조금 차이가 있었지만, Enum클래스가 `abstract State`로서 역할을 하고 있고, 그 안에 정의된 State들이 `ConcreteState` 역할을 하고 있고, abstract 메소드인 `read()` 또한 모든 State에서 구현되도록 정의하고 있기 때문에, 구현 또한 State Pattern으로 보아도 문제가 없었다.

State Pattern은 특정 Object(Context)가 그것의 internal state에 따라서 다른 방식으로 행동하도록 즉, Object 상황을 그것의 행동과 연관짓도록 해야할 때 쓰는 패턴이다.

`Tokeniser` 또한, 자신의 internal state인 `TokeniserState`에 따라서 다른 read 방식을 수행하도록 함으로써 토큰을 구분하므로, 그것의 행동(`read()`)와 상황(`TokeniserState`)를 연관지어야 한다. 

따라서 `TokeniserState`와 `Tokeniser`의 경우, 적절한 패턴인 State Pattern을 썼다고 볼 수 있다.

## HtmlTreeBuilderState 

`HtmlTreeBuilder` 클래스는 `Tokeniser`를 얻은 토큰들을 바탕으로 DOM을 생성한다.
이때, `HtmlTreeBuilderState`의 state를 바꿔가며, Html DOM의 규칙에 맞게 DOM을 생성한다.

![HtmlTreeBuilderState Class Diagram](https://user-images.githubusercontent.com/47529632/70200999-aa4dae80-1758-11ea-9ff0-d42634d68026.PNG)

![HtmlTreeBuilderState Class Diagram Real](https://user-images.githubusercontent.com/47529632/70201000-aa4dae80-1758-11ea-80dc-e899195958eb.PNG)

### 상응하는 코드 및 작동 방식 설명 (코드 대응해서)

``` java 
public class HtmlTreeBuilder extends TreeBuilder {
    private HtmlTreeBuilderState state; // the current state
    private HtmlTreeBuilderState originalState; // original / marked state
    private boolean fragmentParsing; // if parsing a fragment of html

    void transition(HtmlTreeBuilderState state) {
        this.state = state;
    }

    HtmlTreeBuilderState state() {
        return state;
    }

    HtmlTreeBuilderState originalState() {
        return originalState;
    }
}
```

먼저, `HtmlTreeBuilder` 클래스는 `Tokeniser`를 얻은 토큰들을 바탕으로 DOM을 생성한다. 이때, Html DOM의 규칙에 맞게 DOM을 생성하기 위해 State를 바꾸며 검사하는데, 자신의 현재 state를 저장하기 위해 `HtmlTreeBuilderState`형의 state변수가 있다. 



그리고 현재 state를 얻기 위한 함수 `state()`와 state를 바꾸기 위한 함수 `transition()`을 제공한다.



``` java
 enum HtmlTreeBuilderState {
    Initial {
        boolean process(Token t, HtmlTreeBuilder tb) {
            if (isWhitespace(t)) {
                return true; // ignore whitespace
            } else if (t.isComment()) {
                tb.insert(t.asComment());
            } else if (t.isDoctype()) {
                // todo: parse error check on expected doctypes
                // todo: quirk state check on doctype ids
                Token.Doctype d = t.asDoctype();
                DocumentType doctype = new DocumentType(
                    tb.settings.normalizeTag(d.getName()), d.getPublicIdentifier(), d.getSystemIdentifier());
                doctype.setPubSysKey(d.getPubSysKey());
                tb.getDocument().appendChild(doctype);
                if (d.isForceQuirks())
                    tb.getDocument().quirksMode(Document.QuirksMode.quirks);
                tb.transition(BeforeHtml);
            } else {
                // todo: check not iframe srcdoc
                tb.transition(BeforeHtml);
                return tb.process(t); // re-process token
            }
            return true;
        }
    },
    BeforeHtml  {
        ...
    }, 
    BeforeHead  {
        ...
    }, 
    ...
 }
```

HtmlTreeBuilderState에는 state별로 각 토큰의 doctype을 결정하고 이를 적절하게 DOM에 넣는 규칙이 정의되어 있다. 즉, 현재 state에서 예상되는 값이 들어왔을 때 또는 예상되지 않은 값이 들어왔을 때 어떤 state로 transition 시켜주는 방식으로 구분 규칙을 정의한다.

이때, 현재 state와 marked state를 알기 위해  `state()`, `originalState()` `Tokeniser`의 state를 전환하기 위해 `transition()` 등 `HtmlTreeBuilder`에 정의되어 있는 함수를 이용한다.

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



``` java
public class HtmlTreeBuilder extends TreeBuilder {
    @Override
    protected void initialiseParse(Reader input, String baseUri, Parser parser) {
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

    @Override
    protected boolean process(Token token) {
        currentToken = token;
        return this.state.process(token, this);
    }

    boolean process(Token token, HtmlTreeBuilderState state) {
        currentToken = token;
        return state.process(token, this);
    }
```

Dynamic Polymorphism을 통해 `HtmlTreeBuilder`의 `initialiseParse()`를 호출한 후, `HtmlTreebuilder`의 상위 클래스인 `TreeBuilder`에서 `runParser()`를 호출하면, `HtmlTreeBuilder`의 `process()`가 실행되고, 이에 따라 현재 state에 해당하는 `process()`가 호출됨으로써 token의 doctype을 확인하고 이를 DOM에 적절하게 추가하는 작업을 하게 된다.

### 판단 근거

State Pattern Class Diagram
![State Pattern Class Diagram](https://user-images.githubusercontent.com/47529632/70195202-6652ad80-1748-11ea-9c66-9533bf7f23d7.PNG)

한편, `HtmlTreeBuilderState`의 클래스 다이어그램은 전형적인 State Pattern의 클래스 다이어그램과 동일했다.

State Pattern에서 Context가 state를 바꾸며, 다른 작업을 수행하는 동작 방식 또한, `HtmlTreeBuilder`가 `HtmlTreeBuilderState`를 바꾸며 동작하는 방식과 동일했다.

구현에서는 조금 차이가 있었지만, Enum클래스가 `abstract State`로서 역할을 하고 있고, 그 안에 정의된 State들이 `Concrete State` 역할을 하고 있고, abstract 메소드인 `process()` 또한 모든 State에서 구현되도록 정의하고 있기 때문에, 구현 또한 State Pattern으로 보아도 문제가 없었다.

State Pattern은 특정 Object(Context)가 그것의 internal state에 따라서 다른 방식으로 행동하도록 즉, Object 상황을 그것의 행동과 연관짓도록 해야할 때 쓰는 패턴이다.

`HtmlTreeBuilder` 또한, 자신의 internal state인 `HtmlTreeBuilderState`에 따라서 다른 process 방식을 수행하도록 함으로써 token의 doctype을 확인하고 이를 적절하게 추가하는 작업을 하며, 그것의 행동(`process()`)와 상황(`HtmlTreeBuilderState`)를 연관지어야 한다. 

따라서 `HtmlTreeBuilderState`와 `HtmlTreeBuilder`의 경우, 적절한 패턴인 State Pattern을 썼다고 볼 수 있다.

