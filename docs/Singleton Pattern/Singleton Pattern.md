# Singleton Pattern

Jsoup에서 Singleton Pattern이 적용된 부분은 다음과 같다.
- `parser` Package
  - `Tag`

## Tag

Tag 클래스는 각 토큰에 맞는 Tag들을 정의한다.

![Tag Singleton Class Diagram](https://user-images.githubusercontent.com/47529632/70221178-3aa4e700-178b-11ea-81be-250240571ff6.PNG)


### 상응하는 코드 및 작동 방식 설명 (코드 대응해서)

``` java 
public class Tag {
    private static final Map<String, Tag> tags = new HashMap<>(); // map of known tags

    private String tagName;

    private Tag(String tagName) {
        this.tagName = tagName;
        normalName = Normalizer.lowerCase(tagName);
    }

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

    public static Tag valueOf(String tagName) {
        return valueOf(tagName, ParseSettings.preserveCase);
    }

    // internal static initialisers:
    // prepped from http://www.w3.org/TR/REC-html40/sgml/dtd.html and other sources
    private static final String[] blockTags = {
            "html", "head", "body", "frameset", "script", "noscript", "style", "meta", "link", "title", "frame",
            "noframes", "section", "nav", "aside", "hgroup", "header", "footer", "p", "h1", "h2", "h3", "h4", "h5", "h6",
            "ul", "ol", "pre", "div", "blockquote", "hr", "address", "figure", "figcaption", "form", "fieldset", "ins",
            "del", "dl", "dt", "dd", "li", "table", "caption", "thead", "tfoot", "tbody", "colgroup", "col", "tr", "th",
            "td", "video", "audio", "canvas", "details", "menu", "plaintext", "template", "article", "main",
            "svg", "math", "center"
    };
    ...

    static {
        // creates
        for (String tagName : blockTags) {
            Tag tag = new Tag(tagName);
            register(tag);
        }
        for (String tagName : inlineTags) {
            Tag tag = new Tag(tagName);
            tag.isBlock = false;
            tag.formatAsBlock = false;
            register(tag);
        }

        // mods:
        for (String tagName : emptyTags) {
            Tag tag = tags.get(tagName);
            Validate.notNull(tag);
            tag.canContainInline = false;
            tag.empty = true;
        }
        ...

    } 

    private static void register(Tag tag) {
        tags.put(tag.tagName, tag);
    }
}
```

먼저, Tag 클래스는 외부에서 자신의 객체를 생성할 수 없도록 생성자를 private으로 선언하였다. 

또, Tag 객체들을 담고 있는 Map인 tags도 private으로 선언하고, final static형으로 선언하면서 동시에 초기화까지 하여 하나의 tags map밖에 없도록 하였다.

이렇게 선언한 tags map에는

코드 아래에 static하게 정의된 blockTags, inlineTags등 internal static initialiser들이 있는데,  register 함수를 통해 이것들이 tags map에 compile time에 넣어진다.

### 판단 근거

Singleton Pattern Class Diagram
![Singleton Class Diagram](https://user-images.githubusercontent.com/47529632/70222161-ef8bd380-178c-11ea-99a1-22ff34373677.PNG)


한편, Tag 의 클래스 다이어그램은 전형적인 Singleton Pattern의 클래스 다이어그램과 동일했다.

Singleton 패턴은 특정 클래스가 한 시스템에서 하나의 instance만을 가지도록 함으로써, singleton 객체에 대해 controlled access를 부가하고자 할 때 적용하는 패턴이다.

Tags 클래스 또한 각 토큰에 맞는 Tag들을 compile time에 모두 생성한 후, 이에 접근만 할 수 있고 새로운 tags map을 만들거나 그 tags map에 추가 할 수 없도록 만들었다.

Tag는 한번 그 객체를 하나만 만든 후, 그것을 계속 재사용하면 되므로 적절한 패턴이라고 볼 수 있다.

한편, 코드를 보면
``` java
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

위와 같이 valueOf를 했을 때, 미리 컴파일 타임에 생성해두지 않은 tag이면 해당 태그 객체를 생성하여 반환해준다. 

하지만 이렇게 새로운 tag에 대해서는 한번만 생성하지 않고, 그때 그때 생성하여 반환해준다면, 같은 여러 tag 객체들이 생길 것이며 이는 singleton 패턴을 사용한 장점이 없어진다.

따라서 이는 패턴의 구현이 잘못된 것으로 보인다.

이를 해결하고자 Tag 클래스를 완전한 싱글톤 패턴으로 바꾸고, 이를 사용하는 모든 부분들을 수정하려 했지만, Tag를 사용하는 부분이 너무 많아 모두를 하나하나 수정하는 것은 시간적으로 불가능했다.