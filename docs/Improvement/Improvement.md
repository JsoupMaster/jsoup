# 팀이 수행한 기능 확장과 설계 개선

## 확장된 기능 요약
유저가 파싱 뿐만 아니라 파싱을 하며, image나 url, html file을 Download받을 수 있는 기능을 만들었다.

### 확장된 기능 위치

![새로 추가한 기능 위치](https://user-images.githubusercontent.com/47529632/70325350-1faea180-1875-11ea-8807-0a1ae76d1c91.PNG)

## 확장된 기능 `Download`에 적용된 설계 패턴

3가지 다운로드 기능과 관련된 패턴들은 다음과 같다.

- Facade 패턴: `Downloader` class는 유저들이 그들이 원하는 파일들을 다운로드 받을 수 있도록 간단한 API를 제공한다.

- Template 패턴: `ImageDownloader`, `UrlsDownloader` and `HtmlDownloader`는  그것들 자신의 `downloadTarget()`를 구현한다. 다른 메소드들 역시 마찬가지이다.

- Simple Factory 패턴: 유저와 `Downloader` 클래스 사이의 abstraction을 추가했다.

![image](https://user-images.githubusercontent.com/19348185/70141239-f574ac80-16d9-11ea-83b1-a0651e310132.png)


## 설계 개선 내용 요약
Package간 dependency를 줄이고 응집성을 높이기 위해, 클래스를 적절한 패키지로 옮겨주었다.

또한, Tag 클래스를 완전한 Singleton 패턴으로 바꿔 문제를 해결하고자 했다. 


## 상세한 변경 내용 설명 및 기존 설계/코드와의 비교

## `Tag Class Problem`

해당 문제는 `org.jsoup.parser`의 `Tag` Class에 있다.

미리 정의된 모든 태그를 포함하는 `Tag`에는 `HashMap`이 있다. 각각의 태그는 `Map`에 `<String, Tag>` 쌍으로 저장된다. 클라이언트는 태그의 이름을 `valueOf`에 전달함으로써 `Tag` 객체를 얻는다.

```java
public static Tag valueOf(String tagName, ParseSettings settings) {
        Validate.notNull(tagName);
        Tag tag = tags.get(tagName);

        if (tag == null) {
            tagName = settings.normalizeTag(tagName);
            Validate.notEmpty(tagName);
            tag = tags.get(tagName);

            if (tag == null) {
                // not defined: create default; go anywhere, do anything! (incl be inside a <p>)
                tag = new Tag(tagName);
                tag.isBlock = false;
            }
        }
        return tag;
    }
```

`Tag` 생성자는 `private`이고, 모든 미리 정의된 태그들은 `static` 초기화 부분에서 `HashMap`에 삽입된다. 만약 Tag 클래스가 모르는 태그 이름이 `valueOf` 메소드를 통해 전달되면 그에 대한 한 `Tag` 객체를 생성하여 그것을 반환한다. 이는 동일한 unknown `Tag`의 많은 객체들을 생성하는 문제를 일으킨다. 

### TokenQueue Class Problem


`org.jsoup.nodes` package에 있는 `TokenQueue` class는 그 패키지의 다른 것들과 관련성이 없다.

`TokenQueue` class는 단지 `org.jsoup.select` package에 있는 ` QueryParser` class와 관계를 맺고 함께 작동하고 있다.

``` java
public class QueryParser {
    private final static String[] combinators = {",", ">", "+", "~", " "};
    private static final String[] AttributeEvals = new String[]{"=", "!=", "^=", "$=", "*=", "~="};

    private TokenQueue tq;
```
https://github.com/JsoupMaster/jsoup/blob/8d1d503913a68e549b5c4a94717c62cf3f64507a/src/main/java/org/jsoup/select/QueryParser.java#L17-L21


## 도입된 설계 패턴 및 설계 원칙과 적용 이유.

## `Tag Class Improvement`

메모리의 낭비를 방지하기 위해, `Singleton` 패턴으로 unknown `Tag` 객체 생성 부분을 바꿨다.

```java
public static Tag valueOf(String tagName, ParseSettings settings) {
        Validate.notNull(tagName);
        // zgy: Start
        Tag tag = tags.get(tagName);

        if (tag == null) {
            tagName = settings.normalizeTag(tagName);
            Validate.notEmpty(tagName);
            // zgy: Second 
            tag = tags.get(tagName);

            if (tag == null) {
            // zgy: Handle multithreading
                synchronized(Tag.class) {
                    tag = tags.get(tagName);
                    if (tag == null) {
                        // not defined: create default; go anywhere, do anything! (incl be inside a <p>)
                        tag = new Tag(tagName);
                        tag.isBlock = false;
                        register(tag);
                    }
                }
            }
        }
        return tag;
    }
```

불필요한 locking을 방지하기 위해 double-check를 사용했다. HashMap에 새로운 `Tag`를 추가할 필요가 있을 때만 그 access를 serialize 했다.

Unit 테스트 코드는 `TagTest.java`와 `TagTestMultithreading.java`에서 확인할 수 있다.

한편, 이것은 모든 유닛 테스트를 통과 할 수 없었다. 

즉, `Tag` 클래스를 unknown 태그들에 대해 `Singleton`으로 만들기 위해서는 unknown tag들과 관련된 모든 parsing 부분들을 수정해야 했다. 따라서 `Tag` 클래스 수정은 하지 않기로 결정했다. 

## `TokenQueue Class Improvement`

package dependency를 줄이기 위해, `TokenQueue` class를 `org.jsoup.nodes`에서 to `org.jsoup.select`로 이동시켰다.

## `Selector Class Improvement`

### Function Improvement :

`selectLast` method를 추가했다.

## `Collector Class Improvement`

### Function Improvement : 

`findLast` method를 추가했다.

## `NodeTraversor Class Improvement`

### Function Improvement
이전의 `NodeTraversor` Class 대신 `HeadToTailTraversor` Class가 추가되었다. 

`Collector` Class의 `findLast` method에서 사용되는 `TailToHeadTraversor` Class가 추가되었다. 

### Design Pattern Improvement

#### 이전
![UML](https://user-images.githubusercontent.com/19328795/70142731-433ee400-16dd-11ea-9678-f9b422acd86e.jpg)
* Strategy 패턴
  * 사용되지 않음.

#### 현재
![NodeTraversor Pattern Improve](https://user-images.githubusercontent.com/47529632/70321097-cbea8b00-1869-11ea-9637-378ad4173930.PNG)

* Template Method 패턴
  * `NodeTraversor` Class를 Interface로 바꾸었다.
  * `HeadToTailTraversor` Class와 `TailToHeadTraversor` Class는 `NodeTraversor` Interface를 상속받는다.


## `Node Class Improvement`

### Function Improvement
유저가 traverse algorithm을 선택할 수 있다(`NodeTraversor`).

### Design Pattern Improvement

#### Old
![UML](https://user-images.githubusercontent.com/19328795/70141605-c90d6000-16da-11ea-9cf7-2c3f1023d6f9.jpg)
* Visitor 패턴
  * `NodeVisitor`는 Visitor 패턴을 따르지만, `NodeTraversor`는 Visitor 패턴을 따르지 않는다.
#### New
![UML](https://user-images.githubusercontent.com/19328795/70141830-33be9b80-16db-11ea-9c57-ec74948b6270.jpg)
* Visitor 패턴
  * `traverse` method는 `NodeTraversor` 객체를 가진다.