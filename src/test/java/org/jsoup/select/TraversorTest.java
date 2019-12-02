package org.jsoup.select;

import static org.junit.Assert.assertEquals;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.junit.Test;

public class TraversorTest {
    // Note: NodeTraversor.traverse(new NodeVisitor) is tested in
    // ElementsTest#traverse()

    @Test
    public void preorderFilterVisit() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor nodeTraversor = new PreorderNodeTraversor();
        nodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                accum.append("<").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                accum.append("</").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p><#text></#text></p></div><div><#text></#text></div>", accum.toString());
    }

    @Test
    public void preorderFilterSkipChildren() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor nodeTraversor = new PreorderNodeTraversor();
        nodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                accum.append("<").append(node.nodeName()).append(">");
                // OMIT contents of p:
                return ("p".equals(node.nodeName())) ? FilterResult.SKIP_CHILDREN : FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                accum.append("</").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p></p></div><div><#text></#text></div>", accum.toString());
    }

    @Test
    public void preorderFilterSkipEntirely() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor nodeTraversor = new PreorderNodeTraversor();
        nodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                // OMIT p:
                if ("p".equals(node.nodeName()))
                    return FilterResult.SKIP_ENTIRELY;
                accum.append("<").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                accum.append("</").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div></div><div><#text></#text></div>", accum.toString());
    }

    @Test
    public void preorderFilterRemove() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There be <b>bold</b></div>");
        NodeTraversor nodeTraversor = new PreorderNodeTraversor();
        nodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                // Delete "p" in head:
                return ("p".equals(node.nodeName())) ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                // Delete "b" in tail:
                return ("b".equals(node.nodeName())) ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div></div>\n<div>\n There be \n</div>", doc.select("body").html());
    }

    @Test
    public void preorderFilterStop() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor nodeTraversor = new PreorderNodeTraversor();
        nodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                accum.append("<").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                accum.append("</").append(node.nodeName()).append(">");
                // Stop after p.
                return ("p".equals(node.nodeName())) ? FilterResult.STOP : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div><p><#text></#text></p>", accum.toString());
    }

    @Test
    public void tailFirstPreorderFilterVisit() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor nodeTraversor = new TailFirstPreorderNodeTraversor();
        nodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                accum.append("<").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                accum.append("</").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("</div></#text><#text><div></div></p></#text><#text><p><div>", accum.toString());
    }

    @Test
    public void tailFirstPreorderFilterSkipChildren() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor nodeTraversor = new TailFirstPreorderNodeTraversor();
        nodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                accum.append("<").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                accum.append("</").append(node.nodeName()).append(">");
                // OMIT contents of p:
                return ("p".equals(node.nodeName())) ? FilterResult.SKIP_CHILDREN : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("</div></#text><#text><div></div></p><p><div>", accum.toString());
    }

    @Test
    public void tailFirstPreorderFilterSkipEntirely() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor nodeTraversor = new TailFirstPreorderNodeTraversor();
        nodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                accum.append("<").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                // OMIT p:
                if ("p".equals(node.nodeName()))
                    return FilterResult.SKIP_ENTIRELY;
                accum.append("</").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("</div></#text><#text><div></div><div>", accum.toString());
    }

    @Test
    public void tailFirstPreorderFilterRemove() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There be <b>bold</b></div>");
        NodeTraversor nodeTraversor = new TailFirstPreorderNodeTraversor();
        nodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                // Delete "p" in head:
                return ("p".equals(node.nodeName())) ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                // Delete "b" in tail:
                return ("b".equals(node.nodeName())) ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("<div></div>\n<div>\n There be \n</div>", doc.select("body").html());
    }

    @Test
    public void tailFirstPreorderFilterStop() {
        Document doc = Jsoup.parse("<div><p>Hello</p></div><div>There</div>");
        final StringBuilder accum = new StringBuilder();
        NodeTraversor nodeTraversor = new TailFirstPreorderNodeTraversor();
        nodeTraversor.filter(new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                accum.append("<").append(node.nodeName()).append(">");
                return FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                accum.append("</").append(node.nodeName()).append(">");
                // Stop after p.
                return ("p".equals(node.nodeName())) ? FilterResult.STOP : FilterResult.CONTINUE;
            }
        }, doc.select("div"));
        assertEquals("</div></#text><#text><div></div></p>", accum.toString());
    }
}
