# Iterator Pattern

Jsoup에서 Iterator Pattern이 적용된 부분은 다음과 같다.
- `nodes` Package
  - `DatasetIterator` in `Attributes` class

## DatasetIterator

![DatasetIterator Iterator Pattern](https://user-images.githubusercontent.com/47529632/70298124-d68a2d80-1833-11ea-9296-636e5505cb05.PNG)

### 상응하는 코드 및 작동 방식 설명 (코드 대응해서)

Also many Iterator Pattern. Like in `Attributes` Class :

``` java 
private class DatasetIterator implements Iterator<Map.Entry<String, String>> {
            private Iterator<Attribute> attrIter = attributes.iterator();
            private Attribute attr;
            public boolean hasNext() {
                while (attrIter.hasNext()) {
                    attr = attrIter.next();
                    if (attr.isDataAttribute()) return true;
                }
                return false;
            }

            public Entry<String, String> next() {
                return new Attribute(attr.getKey().substring(dataPrefix.length()), attr.getValue());
            }

            public void remove() {
                attributes.remove(attr.getKey());
            }
        }
```

### 판단 근거

Dataset을 Iterate 하는 과정에서 유저가 그것의 구체적인 data structure를 알 필요 없이, `Iterator` interface만을 알면 되도록 구현하였다.

이는 aggregate object의 구체적인 representation을 알 필요 없이, 그 aggregate object의 element들에 접근하도록 하는 Iterator 패턴의 목적과 동일하다.

따라서 Iterator Pattern을 이용하여 DatasetIterator를 추가한 것은 적절하다고 볼 수 있다.