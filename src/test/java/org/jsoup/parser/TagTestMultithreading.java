package org.jsoup.parser;

import org.jsoup.MultiLocaleRule;
import org.jsoup.MultiLocaleRule.MultiLocaleTest;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

class Multithreading implements Runnable {
    private final String tagName;
    private final Tag[] result;
    private final int i;

    Multithreading(final String tagName, final Tag[] result, final int i) {
        this.tagName = tagName;
        this.result = result;
        this.i = i;
    }

    public void run() {
        try {
            final Tag tag = Tag.valueOf(tagName);
            result[i] = tag;
        } catch (final Exception e) {
            System.out.println("Exception is caught: in thread " + Thread.currentThread().getId());
        }
    }
}

public class TagTestMultithreading {

    @Rule public MultiLocaleRule rule = new MultiLocaleRule();

    @Test public void multiKnownTags() {
        final int n = 3;
        final Tag[] tags = new Tag[n];

        for (int i = 0; i < n; i++) {
            final Thread t = new Thread(new Multithreading("p", tags, i));
            t.start();
        }

        assertEquals(tags[0], tags[1]);
        assertEquals(tags[1], tags[2]);
        assertEquals(true, tags[0] == tags[1]);
    }

    @Test public void multiUnknownTags() {

        final int n = 20;
        final Tag[] tags = new Tag[n];

        for (int i = 0; i < n; i++) {
            final Thread t = new Thread(new Multithreading("FOO", tags, i));
            t.start();
        }

        assertEquals(tags[0], tags[1]);
        assertEquals(tags[1], tags[2]);
        assertEquals(true, tags[0] == tags[1]);
    }

}