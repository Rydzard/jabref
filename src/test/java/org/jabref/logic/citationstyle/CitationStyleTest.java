package org.jabref.logic.citationstyle;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jabref.logic.util.TestEntry;
import org.jabref.model.database.BibDatabase;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.entry.BibEntryTypesManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitationStyleTest {

    @Test
    void getDefault() {
        assertNotNull(CitationStyle.getDefault());
    }

    @Test
    void defaultCitation() {
        BibDatabaseContext context = new BibDatabaseContext(new BibDatabase(List.of(TestEntry.getTestEntry())));
        context.setMode(BibDatabaseMode.BIBLATEX);
        String citation = CitationStyleGenerator.generateBibliography(List.of(TestEntry.getTestEntry()), CitationStyle.getDefault().getSource(), CitationStyleOutputFormat.HTML, context, new BibEntryTypesManager()).getFirst();

        // if the default citation style changes this has to be modified
        String expected = """
                  <div class="csl-entry">
                    <div class="csl-left-margin">[1]</div><div class="csl-right-inline">B. Smith, B. Jones, and J. Williams, &ldquo;Title of the test entry,&rdquo; <span style="font-style: italic">BibTeX Journal</span>, vol. 34, no. 3, pp. 45&ndash;67, Jul. 2016, doi: 10.1001/bla.blubb.</div>
                  </div>
                """;

        assertEquals(expected, citation);
    }

    @Test
    void discoverCitationStylesNotNull() {
        List<CitationStyle> styleList = CitationStyle.discoverCitationStyles();
        assertNotNull(styleList);
    }

    @ParameterizedTest
    @MethodSource
    void citationStylePresent(String cslFileName) {
        Optional<CitationStyle> citationStyle = CitationStyle.createCitationStyleFromFile(cslFileName);
        assertTrue(citationStyle.isPresent());
    }

    static Stream<Arguments> citationStylePresent() {
        return Stream.of(
                Arguments.of("ieee.csl"),
                Arguments.of("apa.csl"),
                Arguments.of("vancouver.csl"),
                Arguments.of("chicago-author-date.csl"),
                Arguments.of("nature.csl")
        );
    }

    @ParameterizedTest
    @MethodSource
    void titleMatches(String expectedTitle, String cslFileName) {
        Optional<CitationStyle> citationStyle = CitationStyle.createCitationStyleFromFile(cslFileName);
        CitationStyle.StyleInfo styleInfo = new CitationStyle.StyleInfo(citationStyle.get().getTitle(), citationStyle.get().isNumericStyle());
        assertEquals(expectedTitle, styleInfo.title());
    }

    static Stream<Arguments> titleMatches() {
        return Stream.of(
                Arguments.of("IEEE", "ieee.csl"),
                Arguments.of("American Psychological Association 7th edition", "apa.csl"),
                Arguments.of("Vancouver", "vancouver.csl"),
                Arguments.of("Chicago Manual of Style 17th edition (author-date)", "chicago-author-date.csl"),
                Arguments.of("Nature", "nature.csl")
        );
    }

    @ParameterizedTest
    @MethodSource
    void numericPropertyMatches(boolean expectedNumericNature, String cslFileName) {
        Optional<CitationStyle> citationStyle = CitationStyle.createCitationStyleFromFile(cslFileName);
        CitationStyle.StyleInfo styleInfo = new CitationStyle.StyleInfo(citationStyle.get().getTitle(), citationStyle.get().isNumericStyle());
        assertEquals(expectedNumericNature, styleInfo.isNumericStyle());
    }

    private static Stream<Arguments> numericPropertyMatches() {
        return Stream.of(
                Arguments.of(true, "ieee.csl"),
                Arguments.of(false, "apa.csl"),
                Arguments.of(true, "vancouver.csl"),
                Arguments.of(false, "chicago-author-date.csl"),
                Arguments.of(true, "nature.csl")
        );
    }
    
    @Test
    void testStripInvalidProlog() {
        String input = "<xml>Valid content</xml>";
        String result = stripInvalidProlog(input);
        assertEquals("<xml>Valid content</xml>", result, "Should return the input unchanged when it starts with '<'");

        input = "Invalid text before prolog<xml>Valid content</xml>";
        result = stripInvalidProlog(input);
        assertEquals("<xml>Valid content</xml>", result, "Should strip invalid text before '<'");

        input = "";
        result = stripInvalidProlog(input);
        assertEquals("", result, "Should return an empty string for empty input");

        input = "<";
        result = stripInvalidProlog(input);
        assertEquals("<", result, "Should return the input unchanged when it only contains '<'");
    }

    @Test
    void testParseStyleInfoIsParsed() {
        String content = """
                    <style>
                        <info>
                            <title>Valid Title</title>
                        </info>
                        <bibliography/>
                    </style>
                """;
        String filename = "valid-style.csl";

        Optional<CitationStyle.StyleInfo> result = parseStyleInfo(filename, content);

        assertTrue(result.isPresent(), "Expected valid style info to be parsed");
    }

    @Test
    void testTitleStyleInfo() {
        String content = """
                    <style>
                        <info>
                            <title>Valid Title</title>
                        </info>
                        <bibliography/>
                    </style>
                """;
        String filename = "valid-style.csl";

        Optional<CitationStyle.StyleInfo> result = parseStyleInfo(filename, content);

        assertEquals("Valid Title", result.get().title(), "Expected title to match");
    }


    @Test
    void testNumericStyle() {
        String content = """
                    <style>
                        <info>
                            <title>Numeric Style</title>
                        </info>
                        <bibliography/>
                        <category citation-format=\"numeric\"/>
                    </style>
                """;
        String filename = "numeric-style.csl";

        Optional<CitationStyle.StyleInfo> result = parseStyleInfo(filename, content);

        assertTrue(result.get().isNumericStyle(), "Expected numeric style");
    }


    @Test
    void testInvalidStyleInfo() {
        String content = "<style><info></info></style>"; // Missing title and bibliography
        String filename = "invalid-style.csl";

        Optional<CitationStyle.StyleInfo> result = parseStyleInfo(filename, content);

        assertFalse(result.isPresent(), "Expected no valid style info to be parsed");
    }

    @Test
    void testCreateCitationStyleFromSource() throws Exception {
        // Mockovanie InputStream
        InputStream mockInputStream = mock(InputStream.class);
        String mockContent = "<style><info><title>APA Style</title></info><bibliography/></style>";
        when(mockInputStream.readAllBytes()).thenReturn(mockContent.getBytes());

        // Simulujeme, že createCitationStyleFromSource vráti platný CitationStyle
        Optional<CitationStyle> result = CitationStyle.createCitationStyleFromSource(mockInputStream, "apa.csl");
        
        assertTrue(result.isPresent(), "Expected CitationStyle to be created from source");
        assertEquals("APA Style", result.get().getTitle(), "Expected title to match");
        assertFalse(result.get().isNumericStyle(), "Expected non-numeric style");
        assertEquals("apa.csl", result.get().getFilePath(), "Expected correct file path");
    }
}
