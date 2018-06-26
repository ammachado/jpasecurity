/*
 * Copyright 2011 - 2016 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.jpasecurity.xml;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Tests the correct exception behavior of {@link AbstractXmlParser}.
 * @author Arne Limburg
 */
public class AbstractXmlParserTest {

    private static final String SAX_PARSER_FACTORY_PROPERTY_NAME = "javax.xml.parsers.SAXParserFactory";

    private AbstractXmlParser<DelegatingXmlHandler> parser;

    @Before
    public void initialize() {
        ContentHandler contentHandler = mock(ContentHandler.class);
        parser = new TestXmlParser(new DelegatingXmlHandler(contentHandler));
    }

    @After
    public void clearSAXParserFactoryProperty() {
        System.clearProperty(SAX_PARSER_FACTORY_PROPERTY_NAME);
    }

    @Test
    public void parse() throws IOException, SAXException {
        ContentHandler contentHandler = parser.getHandler().getContentHandler();
        parser.parse(getClass().getResource("test.xml"));
        verify(contentHandler).startElement(eq(""), eq(""), eq("test"), any(Attributes.class));
        verify(contentHandler).endElement(eq(""), eq(""), eq("test"));
    }

    @Test(expected = FileNotFoundException.class)
    public void parseNonExisitingFile() throws IOException {
        parser.parse(new URL("file:./non-existing-file.xml"));
    }

    public static class TestXmlParser extends AbstractXmlParser<DelegatingXmlHandler> {

        TestXmlParser(DelegatingXmlHandler xmlHandler) {
            super(xmlHandler);
        }
    }

    public static class DelegatingXmlHandler extends DefaultHandler {

        private final ContentHandler delegate;

        DelegatingXmlHandler(ContentHandler contentHandler) {
            this.delegate = contentHandler;
        }

        ContentHandler getContentHandler() {
            return delegate;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            delegate.setDocumentLocator(locator);
        }

        @Override
        public void startDocument() throws SAXException {
            delegate.startDocument();
        }

        @Override
        public void endDocument() throws SAXException {
            delegate.endDocument();
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            delegate.startPrefixMapping(prefix, uri);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
            delegate.endPrefixMapping(prefix);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            delegate.startElement(uri, localName, qName, atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            delegate.endElement(uri, localName, qName);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            delegate.characters(ch, start, length);
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            delegate.ignorableWhitespace(ch, start, length);
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            delegate.processingInstruction(target, data);
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
            delegate.skippedEntity(name);
        }
    }
}
