# Design Overview

전체 Jsoup UML 추가

## 각 패키지 역할

### `parser`

Input Stream을 통해 받은 data를 `Tokeniser`를 통해 토큰으로 구분한다. 이후, 이 토큰으로부터 `TreeBuilder`를 통해 HTML이나 XML의 Document Tree를 생성하는 역할.

### `nodes`

HTML과 XML의 Element를 표현하는 클래스들의 집합체로서 역할.

각 클래스에는 Document를 생성할 때 필요한 insert, remove 등의 메소드를 제공.

### `select`

`select` 패키지 안에는 CSS select query를 parsing한 후, 요청한 노드를 가져오는 역할.

### `safety`

`safety`에는 whitelist 규칙을 정의하는 `Whitelist`와 HTML 내용중 그 whitelist 규칙에 어긋나는 것을 없애는 `Cleaner` 클래스가 있음.

`Cleaner`를 통해 User가 원하는 Element와 attribute를 포함하고 있는 HTML을 제공받을 수 있음.
또한, cross-site scripting attack을 막음.

### `helper`

각종 Helper 메소드를 제공.

예를 들어 Http 연결시 사용하는 GET, POST 등 request 메소드

Validate 클래스를 통해 제공하는 유효성 검증을 위한 메소드

### `internal`

Jsoup을 실행하기 위해 필요한 내부 APIs을 담고있음.

## 동작과정

여기서는 한 웹 페이지를 파싱하기까지의 Jsoup의 전반적인 동작과정을 설명한다.

`HttpConnection`을 통해서 주어진 url에 연결한다.
`HttpConnection.Request`을 통해 url을 encoding 후, 주어진 request 방식(GET, POST)에 따라 요청을 보낸다.

`HttpConnection.Response`는 Response 내용을 받는다. 받은 내용의 type(HTML, XML)에 따라 Parser를 선택한 후, parsing작업을 시작한다.

구체적인 Parsing 방식은 `TreeBuilder`를 통해 정한다. 현재 Jsoup에는 `HtmlTreeBuilder`, `XmlTreeBuilder` 두가지 TreeBuilder가 존재한다. 

`TreeBuilder`에는 `parse()`가 있다.
`parse()`내에서 `runParser()`를 통해 parsing을 시작한다. 그리고 `runParser()`는 Tokeniser로부터 토큰을 받는다. 지정한 Document type(HTML, XML)에 따라 받은 Token으로부터 적절한 Document Tree를 생성한다.

Tokenising 할 때, TokeniserState가 가지고 있는 `read()` method를 통해서 토큰을 인식한다.

TokeniserState 내부에는 Enum 클래스 형식으로 state를 정의하고 있다. 또한, 이 각 state는 각기 자신에게 맞는 read() 메소드를 구현하고 있으며, 이 state들을 transition 함으로써 정해진 규칙에 맞게 token을 구분한다.

`HtmlTreeBuilderState`는 Tokeniser와 비슷한 방식으로 동작하고 있다. 즉, 정해진 규칙에 따라 토큰의 type을 보고, 이를 node로 어떻게 Document내부에 insert하는지 정한다.

이 Document tree를 생성하는 과정에서 Jsoup이 불규칙한 HTML 내용도 자동으로 수정할 수 있다. 예를 들어, `<html>` Element `<body>` Element가 없는 상황도 자동으로 수정할 수 있다.

만들어진 Document Tree로부터 유저는 select을 통해 원하는 node 얻을 수 있다.

한편, Jsoup는 Css selector 문법을 사용하고 있다. 유저가 입력해주는 Select query는 `QueryParser`를 통해서 파싱 후, document tree를 traverse 후, 유저의 query에 맞는 결과 node를 반환한다.