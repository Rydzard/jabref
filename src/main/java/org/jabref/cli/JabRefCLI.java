package org.jabref.cli;

import java.util.List;
import java.util.Objects;

import javafx.util.Pair;

import org.jabref.logic.exporter.ExporterFactory;
import org.jabref.logic.importer.ImportFormatReader;
import org.jabref.logic.l10n.Localization;
import org.jabref.logic.os.OS;
import org.jabref.logic.preferences.CliPreferences;
import org.jabref.logic.util.BuildInfo;
import org.jabref.model.entry.BibEntryTypesManager;
import org.jabref.model.strings.StringUtil;
import org.jabref.model.util.DummyFileUpdateMonitor;

import com.airhacks.afterburner.injection.Injector;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class JabRefCLI {
    private static final int WIDTH = 100; // Number of characters per line before a line break must be added.
    private static final String WRAPPED_LINE_PREFIX = ""; // If a line break is added, this prefix will be inserted at the beginning of the next line
    private static final String STRING_TABLE_DELIMITER = " : ";

    private final CommandLine cl;
    private final List<String> leftOver;

    public JabRefCLI(String[] args) throws ParseException {
        Options options = getOptions();
        this.cl = new DefaultParser().parse(options, args, true);
        this.leftOver = cl.getArgList();
    }

    public static String getExportMatchesSyntax() {
        return "[%s]searchTerm,outputFile:%s[,%s]".formatted(
                Localization.lang("field"),
                Localization.lang("file"),
                Localization.lang("exportFormat"));
    }

    public boolean isHelp() {
        return cl.hasOption("help");
    }

    public boolean isShowVersion() {
        return cl.hasOption("version");
    }

    public boolean isBlank() {
        return cl.hasOption("blank");
    }

    public boolean isDisableGui() {
        return cl.hasOption("nogui");
    }

    public boolean isPreferencesExport() {
        return cl.hasOption("prexp");
    }

    public String getPreferencesExport() {
        return cl.getOptionValue("prexp", "jabref_prefs.xml");
    }

    public boolean isPreferencesImport() {
        return cl.hasOption("primp");
    }

    public String getPreferencesImport() {
        return cl.getOptionValue("primp", "jabref_prefs.xml");
    }

    public boolean isPreferencesReset() {
        return cl.hasOption("prdef");
    }

    public String getPreferencesReset() {
        return cl.getOptionValue("prdef");
    }

    public boolean isFileExport() {
        return cl.hasOption("output");
    }

    public String getFileExport() {
        return cl.getOptionValue("output");
    }

    public boolean isBibtexImport() {
        return cl.hasOption("importBibtex");
    }

    public String getBibtexImport() {
        return cl.getOptionValue("importBibtex");
    }

    public boolean isFileImport() {
        return cl.hasOption("import");
    }

    public String getFileImport() {
        return cl.getOptionValue("import");
    }

    public boolean isAuxImport() {
        return cl.hasOption("aux");
    }

    public String getAuxImport() {
        return cl.getOptionValue("aux");
    }

    public boolean isImportToOpenBase() {
        return cl.hasOption("importToOpen");
    }

    public String getImportToOpenBase() {
        return cl.getOptionValue("importToOpen");
    }

    public boolean isDebugLogging() {
        return cl.hasOption("debug");
    }

    public boolean isFetcherEngine() {
        return cl.hasOption("fetch");
    }

    public String getFetcherEngine() {
        return cl.getOptionValue("fetch");
    }

    public boolean isExportMatches() {
        return cl.hasOption("exportMatches");
    }

    public String getExportMatches() {
        return cl.getOptionValue("exportMatches");
    }

    public boolean isGenerateCitationKeys() {
        return cl.hasOption("generateCitationKeys");
    }

    public boolean isAutomaticallySetFileLinks() {
        return cl.hasOption("automaticallySetFileLinks");
    }

    public boolean isWriteXmpToPdf() {
        return cl.hasOption("writeXmpToPdf");
    }

    public boolean isEmbedBibFileInPdf() {
        return cl.hasOption("embedBibFileInPdf");
    }

    public boolean isWriteMetadataToPdf() {
        return cl.hasOption("writeMetadataToPdf");
    }

    public String getWriteMetadataToPdf() {
        return cl.hasOption("writeMetadatatoPdf") ? cl.getOptionValue("writeMetadataToPdf") :
                cl.hasOption("writeXMPtoPdf") ? cl.getOptionValue("writeXmpToPdf") :
                        cl.hasOption("embeddBibfileInPdf") ? cl.getOptionValue("embeddBibfileInPdf") : null;
    }

    public String getJumpToKey() {
        return cl.getOptionValue("jumpToKey");
    }

    public boolean isJumpToKey() {
        return cl.hasOption("jumpToKey");
    }

    private static Options getOptions() {
        Options options = new Options();

        // boolean options
        options.addOption("h", "help", false, Localization.lang("Display help on command line options"));
        options.addOption("n", "nogui", false, Localization.lang("No GUI. Only process command line options"));
        options.addOption("asfl", "automaticallySetFileLinks", false, Localization.lang("Automatically set file links"));
        options.addOption("g", "generateCitationKeys", false, Localization.lang("Regenerate all keys for the entries in a BibTeX file"));
        options.addOption("b", "blank", false, Localization.lang("Do not open any files at startup"));
        options.addOption("v", "version", false, Localization.lang("Display version"));
        options.addOption(null, "debug", false, Localization.lang("Show debug level messages"));

        options.addOption(Option
                .builder("i")
                .longOpt("import")
                .desc("%s: '%s'".formatted(Localization.lang("Import file"), "-i library.bib"))
                .hasArg()
                .argName("FILE[,FORMAT]")
                .build());

        options.addOption(Option
                .builder()
                .longOpt("importToOpen")
                .desc(Localization.lang("Same as --import, but will be imported to the opened tab"))
                .hasArg()
                .argName("FILE[,FORMAT]")
                .build());

        options.addOption(Option
                .builder("ib")
                .longOpt("importBibtex")
                .desc("%s: '%s'".formatted(Localization.lang("Import BibTeX"), "-ib @article{entry}"))
                .hasArg()
                .argName("BIBTEX_STRING")
                .build());

        options.addOption(Option
                .builder("o")
                .longOpt("output")
                .desc("%s: '%s'".formatted(Localization.lang("Export an input to a file"), "-i db.bib -o db.htm,html"))
                .hasArg()
                .argName("FILE[,FORMAT]")
                .build());

        options.addOption(Option
                .builder("m")
                .longOpt("exportMatches")
                .desc("%s: '%s'".formatted(Localization.lang("Matching"), "-i db.bib -m author=Newton,search.htm,html"))
                .hasArg()
                .argName("QUERY,FILE[,FORMAT]")
                .build());

        options.addOption(Option
                .builder("f")
                .longOpt("fetch")
                .desc("%s: '%s'".formatted(Localization.lang("Run fetcher"), "-f Medline/PubMed:cancer"))
                .hasArg()
                .argName("FETCHER:QUERY")
                .build());

        options.addOption(Option
                .builder("a")
                .longOpt("aux")
                .desc("%s: '%s'".formatted(Localization.lang("Sublibrary from AUX to BibTeX"), "-a thesis.aux,new.bib"))
                .hasArg()
                .argName("FILE[.aux],FILE[.bib] FILE")
                .build());

        options.addOption(Option
                .builder("x")
                .longOpt("prexp")
                .desc("%s: '%s'".formatted(Localization.lang("Export preferences to a file"), "-x prefs.xml"))
                .hasArg()
                .argName("[FILE]")
                .build());

        options.addOption(Option
                .builder("p")
                .longOpt("primp")
                .desc("%s: '%s'".formatted(Localization.lang("Import preferences from a file"), "-p prefs.xml"))
                .hasArg()
                .argName("[FILE]")
                .build());

        options.addOption(Option
                .builder("d")
                .longOpt("prdef")
                .desc("%s: '%s'".formatted(Localization.lang("Reset preferences"), "-d mainFontSize,newline' or '-d all"))
                .hasArg()
                .argName("KEY1[,KEY2][,KEYn] | all")
                .build());

        options.addOption(Option
                .builder()
                .longOpt("writeXmpToPdf")
                .desc("%s: '%s'".formatted(Localization.lang("Write BibTeX as XMP metadata to PDF."), "-w pathToMyOwnPaper.pdf"))
                .hasArg()
                .argName("CITEKEY1[,CITEKEY2][,CITEKEYn] | PDF1[,PDF2][,PDFn] | all")
                .build());

        options.addOption(Option
                .builder()
                .longOpt("embedBibFileInPdf")
                .desc("%s: '%s'".formatted(Localization.lang("Embed BibTeX as attached file in PDF."), "-w pathToMyOwnPaper.pdf"))
                .hasArg()
                .argName("CITEKEY1[,CITEKEY2][,CITEKEYn] | PDF1[,PDF2][,PDFn] | all")
                .build());

        options.addOption(Option
                .builder("w")
                .longOpt("writeMetadataToPdf")
                .desc("%s: '%s'".formatted(Localization.lang("Write BibTeX to PDF (XMP and embedded)"), "-w pathToMyOwnPaper.pdf"))
                .hasArg()
                .argName("CITEKEY1[,CITEKEY2][,CITEKEYn] | PDF1[,PDF2][,PDFn] | all")
                .build());

        options.addOption(Option
                .builder("j")
                .longOpt("jumpToKey")
                .desc("%s: '%s'".formatted(Localization.lang("Jump to the entry of the given citation key."), "-j key"))
                .hasArg()
                .argName("CITATIONKEY")
                .build());

        return options;
    }

    public void displayVersion() {
        System.out.println(getVersionInfo());
    }

    public static void printUsage(CliPreferences preferences) {
        String header = "";

        ImportFormatReader importFormatReader = new ImportFormatReader(
                preferences.getImporterPreferences(),
                preferences.getImportFormatPreferences(),
                preferences.getCitationKeyPatternPreferences(),
                new DummyFileUpdateMonitor()
        );
        List<Pair<String, String>> importFormats = importFormatReader
                .getImportFormats().stream()
                .map(format -> new Pair<>(format.getName(), format.getId()))
                .toList();
        String importFormatsIntro = Localization.lang("Available import formats");
        String importFormatsList = "%s:%n%s%n".formatted(importFormatsIntro, alignStringTable(importFormats));

        ExporterFactory exporterFactory = ExporterFactory.create(
                preferences,
                Injector.instantiateModelOrService(BibEntryTypesManager.class));
        List<Pair<String, String>> exportFormats = exporterFactory
                .getExporters().stream()
                .map(format -> new Pair<>(format.getName(), format.getId()))
                .toList();
        String outFormatsIntro = Localization.lang("Available export formats");
        String outFormatsList = "%s:%n%s%n".formatted(outFormatsIntro, alignStringTable(exportFormats));

        String footer = '\n' + importFormatsList + outFormatsList + "\nPlease report issues at https://github.com/JabRef/jabref/issues.";

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(WIDTH, "jabref [OPTIONS] [BIBTEX_FILE]\n\nOptions:", header, getOptions(), footer, true);
    }

    private String getVersionInfo() {
        BuildInfo buildInfo = Injector.instantiateModelOrService(BuildInfo.class);
        return "JabRef %s".formatted(buildInfo.version);
    }

    public List<String> getLeftOver() {
        return leftOver;
    }

    protected static String alignStringTable(List<Pair<String, String>> table) {
        StringBuilder sb = new StringBuilder();

        int maxLength = table.stream()
                             .mapToInt(pair -> Objects.requireNonNullElse(pair.getKey(), "").length())
                             .max().orElse(0);

        for (Pair<String, String> pair : table) {
            int padding = Math.max(0, maxLength - pair.getKey().length());
            sb.append(WRAPPED_LINE_PREFIX);
            sb.append(pair.getKey());

            sb.append(StringUtil.repeatSpaces(padding));

            sb.append(STRING_TABLE_DELIMITER);
            sb.append(pair.getValue());
            sb.append(OS.NEWLINE);
        }

        return sb.toString();
    }
}
