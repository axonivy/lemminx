package org.eclipse.lemminx.utils;

import com.vladsch.flexmark.html2md.converter.*;
import com.vladsch.flexmark.util.data.MutableDataSet;

import static org.apache.commons.text.StringEscapeUtils.unescapeXml;

public class MarkdownConverter {

	public static String convert(String html) {
		if (!StringUtils.isTagOutsideOfBackticks(html)) {
			return unescapeXml(html);
		}
		MutableDataSet options = new MutableDataSet();
		options.set(FlexmarkHtmlConverter.SETEXT_HEADINGS, false);
		options.set(FlexmarkHtmlConverter.MAX_BLANK_LINES, 1);

		var converter = FlexmarkHtmlConverter.builder(options).build();
		return converter.convert(html, -1);
	}
}