## Unit Test 

** 해당 링크로 들어가서 잠시 기다리시면 해당 코드 부분으로 이동된다.
### src/test/java/org/jsoup/nodes/ElementTest.java

[ElementTest 수정사항 보기](https://github.com/JsoupMaster/jsoup/commit/fa0c345e728133dbd3d3716cc953b32f6594bf2a#diff-3cfacf8ca025e4dd7790f4d041f8a1db)

### src/test/java/org/jsoup/nodes/NodeTest.java

[NodeTest 수정사항 보기](https://github.com/JsoupMaster/jsoup/commit/fa0c345e728133dbd3d3716cc953b32f6594bf2a#diff-571ddc7ccfae46f8d5ad269dcc8b2503)

### src/test/java/org/jsoup/select/ElementsTest.java

[ElementsTest 수정사항 보기](https://github.com/JsoupMaster/jsoup/commit/fa0c345e728133dbd3d3716cc953b32f6594bf2a#diff-cac1f1d55b55a6198d3fc9a76bd48932)

### src/test/java/org/jsoup/select/SelectorTest.java

[SelectorTest 수정사항 보기](https://github.com/JsoupMaster/jsoup/commit/fa0c345e728133dbd3d3716cc953b32f6594bf2a#diff-019fca2647aaa8947a1b7d6a55d4e174)

### src/test/java/org/jsoup/select/TraversorTest.java

[TraversorTest 수정사항 보기](https://github.com/JsoupMaster/jsoup/commit/fa0c345e728133dbd3d3716cc953b32f6594bf2a#diff-7fb207a9cbf81cc66e80967685c5aa36)

한편, TraversorTest의 경우 현재 Test Case의 이름을 바꾸었다.

[TraversorTest Test Case 이름 수정사항 보기](https://github.com/JsoupMaster/jsoup/commit/29564ba674d09a22a5ffe297f72cb96e6516165c#diff-7fb207a9cbf81cc66e80967685c5aa36)



## Unit test ofr DownloaderFactory
``` java
package org.jsoup.downloader;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class DownloaderFactoryTest {

    @Test
    public void testCreateUrlDownloader() {
        DownloaderFactory df = new DownloaderFactory();
        assertEquals(true, df.create("UrlsDownloader") instanceof UrlsDownloader);
    }

    @Test
    public void testCreateImageDownloader() {
        DownloaderFactory df = new DownloaderFactory();
        assertEquals(true, df.create("ImageDownloader") instanceof ImageDownloader);
    }

    @Test
    public void testCreateHtmlDownloader() {
        DownloaderFactory df = new DownloaderFactory();
        assertEquals(true, df.create("HtmlDownloader") instanceof HtmlDownloader);
    }

}
```

## Unit Test for Downloader

```java
package org.jsoup.downloader;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DownloaderTest {

	String testUrl = "https://raw.githubusercontent.com/JsoupMaster/jsoup/master/src/main/javadoc/overview.html";
	String storePath = System.getProperty("user.dir") + "/download/";

	@Test
	public void testUrlsDownloader() {
        File file = new File(storePath);
        file.mkdir();

		Downloader downloader = new UrlsDownloader();
		downloader.download(testUrl, storePath  + "urls.txt");

		try {
			InputStream in = new FileInputStream(storePath  + "urls.txt");
			try {
				String urls = new String(in.readAllBytes());
				assertTrue(urls.equalsIgnoreCase(
						"http://whatwg.org/html\n" +
						"http://jonathanhedley.com/\n" +
						"https://jsoup.org/\n"));

			} catch (IOException e) {
				fail("file is not readable.");
			}
		} catch (FileNotFoundException e) {
			fail("file is not found.");
		}

		file = new File(storePath  + "urls.txt");
		file.delete();

        file = new File(storePath);
		file.delete();
	}
}
```

현재 DownloaderTest의 경우 이름을 DepthOneCrawler로 바꾸었다.

[DownloaderTest 수정사항 보기](https://github.com/JsoupMaster/jsoup/commit/832a9628357eafea862812976670c620c6d6fdb4#diff-adeb6dc18e8726c9e2c93f65adfd6732)



## Total Test Result

![Total Test Result](https://user-images.githubusercontent.com/47529632/70300867-09382400-183c-11ea-8d5a-1cd020627c40.png)

## DownloaderFactory unit test code

<img width="787" alt="Downloader Factory unit test code" src="https://user-images.githubusercontent.com/47529632/70314699-60012600-185b-11ea-8627-941760f1854b.png">

## All unit tests passed

<img width="630" alt="All unit tests result(gw)" src="https://user-images.githubusercontent.com/47529632/70314702-60012600-185b-11ea-834a-b2ad35b82083.png">

## Example code for our downloader

<img width="574" alt="Downloader Example code" src="https://user-images.githubusercontent.com/47529632/70314697-5f688f80-185b-11ea-8c64-4d9c0370dc3e.png">

## Downloaded images

<img width="699" alt="downloaded images" src="https://user-images.githubusercontent.com/47529632/70314695-5f688f80-185b-11ea-9fa3-64d5b194342e.png">

## Downloaded urls

<img width="620" alt="downloaded urls" src="https://user-images.githubusercontent.com/47529632/70314696-5f688f80-185b-11ea-8d5f-1438c9300b91.png">

## Downloaded HTML files

<img width="681" alt="downloaded html files" src="https://user-images.githubusercontent.com/47529632/70314694-5f688f80-185b-11ea-8260-aa1cf04e8ab1.png">

