# Facade Pattern

Jsoup에서 Facade Pattern이 적용된 부분은 다음과 같다.
- `parser` Package
  - `Parser`

## Parser

Parser 클래스는 Html과 Xml을 파싱하여 Document를 만든다.

한편, 파싱하여 Document를 만드는 과정에는 Tonkenising 및 Tree building 등 많은 작업이 수반된다.

![Parse Facade Pattern Diagram](https://user-images.githubusercontent.com/47529632/70218827-13e4b180-1787-11ea-8343-d581a8171055.jpg)

### 상응하는 코드 및 작동 방식 설명 (코드 대응해서)

``` java 

```


### 판단 근거

Facade Pattern Class Diagram
![Facade Pattern Class Diagram](https://user-images.githubusercontent.com/47529632/70218815-0fb89400-1787-11ea-9a5e-eb7bc5d2da10.PNG)

![Facade Pattern Class Diagram2](https://user-images.githubusercontent.com/47529632/70219091-8e153600-1787-11ea-8e5c-912054e03827.PNG)

한편, Parser 의 클래스 다이어그램은 전형적인 Facade Pattern의 클래스 다이어그램과 동일했다.

Facade 패턴은 system내의 interface들의 집합에 대해 하나의 단일 interface를 제공함으로써 client와 system의 implementation간의 많은 dependency를 줄여주고, 간단한 interface를 통해 system에 접근할 수 있도록 해준다.

Parser 클래스 또한 html, xml을 파싱하여 Document를 만드는 과정에 수반되는 Tonkenising 및 Tree building 등 많은 작업을 single interface로 묶어 Client에게 제공함으로써 Facade 패턴의 이득을 취한다.

따라서 Parser 클래스의 경우, Facade 패턴을 사용한 것은 적절하다.
