package org.eclipse.lemminx.utils;

import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import static org.apache.commons.text.StringEscapeUtils.unescapeXml;

public class MarkdownConverter {

	private final Document document;
	private final StringBuilder markdown = new StringBuilder();
	private int insidePre;
	private int insideCode;
	private boolean insideCodeBlock;

	public static String convert(String helpText) {
		if (!StringUtils.isTagOutsideOfBackticks(helpText)) {
			return unescapeXml(helpText);
		}
		return new MarkdownConverter(helpText).toMarkupContent().getValue();
	}

	public MarkdownConverter(String html) {
		document = Jsoup.parse(html);
	}

	public MarkupContent toMarkupContent() {
		children(document);
		return new MarkupContent(MarkupKind.MARKDOWN, markdown.toString());
	}

	private void children(Node parent) {
		parent.childNodes().forEach(this::node);
	}

	private void node(Node node) {
		if (node instanceof Element element) {
			element(element);
			return;
		}
		if (node instanceof TextNode text) {
			text(text);
			return;
		}
	}

	private void text(TextNode text) {
		if (insideCodeBlock) {
			codeText(text);
		} else if (insidePre > 0) {
			preText(text);
		} else {
			rawText(text);
		}
	}

	private void rawText(TextNode text) {
		var txt = text.text();
		if (!txt.isBlank()) {
			markdown.append(txt);
		}
	}

	private void preText(TextNode text) {
		markdown.append(text.getWholeText());
	}

	private void codeText(TextNode text) {
		for (var line : text.getWholeText().split("\n")) {
			markdown.append("    ");
			markdown.append(line);
			markdown.append('\n');
		}
	}

	private void element(Element element) {
		switch (element.tagName()) {
		case "h1" -> h(element, 1);
		case "h2" -> h(element, 2);
		case "h3" -> h(element, 3);
		case "h4" -> h(element, 4);
		case "h5" -> h(element, 5);
		case "h6" -> h(element, 6);
		case "a" -> a(element);
		case "b" -> b(element);
		case "em" -> em(element);
		case "br" -> br();
		case "p" -> p(element);
		case "div" -> div(element);
		case "section" -> section(element);
		case "code" -> code(element);
		case "pre" -> pre(element);
		case "dl" -> dl(element);
		case "dt" -> dt(element);
		case "dd" -> dd(element);
		case "ol" -> ol(element);
		case "ul" -> ul(element);
		case "li" -> li(element);
		default -> children(element);
		}
	}

	private void h(Element h, int level) {
		markdown.append("\n\n");
		for (int pos = 0; pos < level; pos++) {
			markdown.append("#");
		}
		markdown.append(" ");
		children(h);
		markdown.append("\n\n");
	}

	private void a(Element a) {
		var href = a.attr("href");
		if (href.startsWith("eclipse-javadoc:")) {
			children(a);
			return;
		}
		markdown.append('[');
		children(a);
		markdown.append(']');
		markdown.append('(');
		markdown.append(href);
		markdown.append(')');
	}

	private void li(Element li) {
		if ("ol".equals(li.parent().tagName())) {
			markdown.append("1. ");
		} else {
			markdown.append("* ");
		}
		children(li);
		markdown.append("\n");
	}

	private void ol(Element ol) {
		markdown.append("\n\n");
		children(ol);
		markdown.append("\n");
	}

	private void ul(Element ol) {
		markdown.append("\n\n");
		children(ol);
		markdown.append("\n");
	}

	private void section(Element section) {
		markdown.append("\n\n");
		children(section);
		markdown.append("\n\n");
	}

	private void dl(Element dl) {
		children(dl);
		markdown.append("\n\n");
	}

	private void dt(Element dt) {
		markdown.append("\n\n");
		markdown.append("**");
		children(dt);
		markdown.append("**");
	}

	private void dd(Element dt) {
		markdown.append("\n\n");
		children(dt);
	}

	private void pre(Element pre) {
		insidePre++;
		children(pre);
		insidePre--;
	}

	private void code(Element code) {
		insideCode++;
		if (insideCode > 1) {
			children(code);
		} else if (code.childNodeSize() == 0) {
			// empty element
		} else if (code.wholeText().contains("\n")) {
			codeBlock(code);
		} else {
			codeInline(code);
		}
		insideCode--;
	}

	private void codeInline(Element code) {
		markdown.append('`');
		children(code);
		markdown.append('`');
	}

	private void codeBlock(Element code) {
		insideCodeBlock = true;
		markdown.append("\n\n");
		children(code);
		markdown.append("\n\n");
		insideCodeBlock = false;
	}

	private void div(Element div) {
		markdown.append("\n\n");
		children(div);
		markdown.append("\n\n");
	}

	private void p(Element p) {
		children(p);
	}

	private void br() {
		markdown.append("\n\n");
	}

	private void em(Element em) {
		markdown.append("_");
		children(em);
		markdown.append("_");
	}

	private void b(Element b) {
		markdown.append("**");
		children(b);
		markdown.append("**");
	}

}