package com.github.cwilper.dsogen;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.xml.XmlEscapers;
import jline.TerminalFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class DsoGen
{
    private static final int LINES_PER_PAGE = 51;

    private static final byte[] FOUR_KB_OF_BYTES = new byte[4096];

    private static final Date START_OF_EPOCH = new Date(0);

    private static final Date NOW = new Date();

    private static final WordList words = WordList.fromStream(1, DsoGen.class.getResourceAsStream("words.txt"));

    private static final WordList authors = randomTitlePhrases(40, 2);

    private static final WordList subjects = randomTitlePhrases(20, 1);

    static {
        Arrays.fill(FOUR_KB_OF_BYTES, (byte) 0x00);
    }

    private static WordList randomTitlePhrases(int numItems, int wordsPerItem) {
        List<String> phrases = Lists.newArrayList();
        for (int i = 0; i < numItems; i++) {
            phrases.add(randomTitlePhrase(wordsPerItem));
        }
        return new WordList(2, phrases);
    }

    private static String randomTitlePhrase(int numWords) {
        final StringBuilder phrase = new StringBuilder();
        for (int j = 0; j < numWords; j++) {
            String word = null;
            while (word == null || word.length() < 4) {
                word = randomTitleCaseWord();
            }
            if (phrase.length() > 0) {
                phrase.append(" ");
            }
            phrase.append(word);
        }
        return phrase.toString();
    }

    private DsoGen() { }

    private static void generateItem(final File dir, final String id, final int numPages,
                                     final boolean pdf, final boolean txt, final int binBitstreams,
                                     final int numBytes) throws Exception {
        final String title = randomTitlePhrase(4);
        final String description = randomSentence(100, 106)
            + " " + randomSentence(300, 306)
            + " " + randomSentence(300, 306)
            + " " + randomSentence(100, 106);
        final String author = authors.random();
        final String subject1 = subjects.random();
        final String subject2 = subjects.random();
        final String subject3 = subjects.random();

        final List<String> lines = generateLines(title, author, numPages * LINES_PER_PAGE);

        final List<String> contents = Lists.newArrayList();
        if (pdf) {
            writePdf(new File(dir, "file.pdf"), lines);
            contents.add("file.pdf\tprimary:true");
        }
        if (txt) {
            writeText(new File(dir, "file.txt"), lines);
            contents.add("file.txt\tbundle:TEXT" + (pdf ? "" : "\tprimary:true"));
        }
        for (int i = 1; i < binBitstreams + 1; i++) {
            writeBinary(new File(dir, "file" + i + ".bin"), numBytes);
            contents.add("file" + i + ".bin");
        }
        writeText(new File(dir, "contents"), contents);
        writeText(new File(dir, "dublin_core.xml"),
                getDcXml(id, title, description, author, subject1, subject2, subject3));
    }

    private static void writeBinary(final File file, final int numBytes) throws Exception {
        final FileOutputStream out = new FileOutputStream(file);
        int bytesWritten = 0;
        while (bytesWritten < numBytes) {
            final int len = ((numBytes - bytesWritten) < 4096) ? numBytes - bytesWritten : 4096;
            out.write(FOUR_KB_OF_BYTES, 0, len);
            bytesWritten += len;
        }
        out.close();
    }

    private static void writePdf(final File file, final List<String> lines) throws Exception {
        PDDocument document = new PDDocument();
        PDFont font = PDType1Font.COURIER;
        PDPage page = null;
        PDPageContentStream contentStream = null;
        int i = 0;
        for (String line : lines) {
            if (page == null) {
                page = new PDPage(PDPage.PAGE_SIZE_LETTER);
                contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(font, 11);
                contentStream.beginText();
                contentStream.moveTextPositionByAmount(65, 735);
            }
            contentStream.moveTextPositionByAmount(0, -13);
            contentStream.drawString(line);
            i++;
            if (i == LINES_PER_PAGE) {
                contentStream.endText();
                contentStream.close();
                document.addPage(page);
                page = null;
                contentStream = null;
                i = 0;
            }
        }
        if (page != null) {
            contentStream.endText();
            contentStream.close();
            document.addPage(page);
        }

        document.save(file);
        document.close();
    }

    private static void writeText(final File file, final List<String> lines) throws Exception {
        Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
    }

    private static List<String> getDcXml(final String id, final String title, final String description, final String author, final String subject1, final String subject2, final String subject3) {
        final List<String> xml = Lists.newArrayList();
        xml.add("<dublin_core>");
        final StringBuilder builder = new StringBuilder();
        addDcValue(builder, title, "title");
        addDcValue(builder, "text", "type");
        addDcValue(builder, randomDay(), "date", "issued");
        addDcValue(builder, description, "description", "abstract");
        addDcValue(builder, author, "contributor", "author");
        addDcValue(builder, subject1, "subject");
        if (!subject2.equals(subject1)) {
            addDcValue(builder, subject2, "subject");
        }
        if (!subject3.equals(subject1) && !subject3.equals(subject2)) {
            addDcValue(builder, subject3, "subject");
        }
        xml.add(builder.toString());
        xml.add("</dublin_core>");
        return xml;
    }

    private static String randomDay() {
        long randomTime = ThreadLocalRandom.current().nextLong(START_OF_EPOCH.getTime(), NOW.getTime());
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(randomTime));
    }

    private static String randomTitleCaseWord() {
        final String input = words.random();
        final StringBuilder titleCase = new StringBuilder();
        boolean nextTitleCase = true;
        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            titleCase.append(c);
        }
        return titleCase.toString();
    }

    private static void addDcValue(final StringBuilder builder, final String value, final String... eq) {
        Preconditions.checkArgument(eq.length > 0 && eq.length < 3);
        builder.append("  <dcvalue element=\"");
        builder.append(eq[0]);
        builder.append("\" qualifier=\"");
        builder.append(eq.length == 2 ? eq[1] : "none");
        builder.append("\">");
        builder.append(XmlEscapers.xmlContentEscaper().escape(value));
        builder.append("</dcvalue>\n");
    }

    private static List<String> generateLines(final String title, final String author, final int numLines) {
        List<String> lines = Lists.newArrayList();
        lines.add(center("\"" + title + "\""));
        lines.add(center("by " + author));
        lines.add("");
        for (int i = 0; i < numLines - 3; i++) {
            if ((i + 1) % 5 == 0) {
                lines.add("");
            } else {
                lines.add(randomSentence(66, 72));
            }
        }
        return lines;
    }

    private static String randomSentence(final int minLen, final int maxLen) {
        StringBuilder builder = new StringBuilder();
        while (builder.length() < minLen) {
            final String word = words.random();
            if (builder.length() + word.length() < maxLen) {
                if (builder.length() == 0) {
                    builder.append(word.substring(0, 1).toUpperCase());
                    builder.append(word.substring(1));
                } else {
                    builder.append(" ");
                    builder.append(word);
                }
            }
        }
        builder.append(".");
        return builder.toString();
    }

    private static String center(final String input) {
        final int spaces = (72 - input.length()) / 2;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            builder.append(' ');
        }
        builder.append(input);
        return builder.toString();
    }

    public static void main(final String[] args) throws Exception {
        final Options options = getOptions();
        try {
            if (args.length > 0 && args[0].equals("-h")) {
                printHelpAndExit("dsogen -out out-dir -num num-items -min min-pages -max max-pages -pdf -txt",
                        "Generates DSpace objects that can be imported for testing",
                        options, "At least one of -pdf, -txt, or -bin must be specified, in any combination.");
            }
            CommandLine cmd = new DefaultParser().parse(options, args);
            final File outDir = new File(cmd.getOptionValue("out"));
            final int numItems = getPositiveIntOptionValue(cmd, "num");
            final int minBytes = getPositiveIntOptionValue(cmd, "minbytes");
            final int maxBytes = getPositiveIntOptionValue(cmd, "maxbytes");
            Preconditions.checkArgument(minBytes <= maxBytes, "minbytes cannot be greater than maxbytes");
            final int minPages = getPositiveIntOptionValue(cmd, "minpages");
            final int maxPages = getPositiveIntOptionValue(cmd, "maxpages");
            Preconditions.checkArgument(minPages <= maxPages, "minpages cannot be greater than maxpages");
            final boolean pdf = cmd.hasOption("pdf");
            final boolean txt = cmd.hasOption("txt");
            final int binBitstreams = cmd.hasOption("bin") ? getPositiveIntOptionValue(cmd, "bin") : 0;
            Preconditions.checkArgument(pdf || txt || binBitstreams > 0, "Must specify -pdf, -txt, -bin, or any combination");

            final Iterator<Integer> numPagesInts = new Random().ints(minPages, maxPages + 1).iterator();
            final Iterator<Integer> numBytesInts = new Random().ints(minBytes, maxBytes + 1).iterator();
            for (int i = 0; i < numItems; i++) {
                final int itemNum = i + 1;
                System.out.println("Generating item " + itemNum + "/" + numItems);
                final String id = UUID.randomUUID().toString();
                final File dir = new File(outDir, id);
                Preconditions.checkState(dir.mkdirs(), "Unable to create directory: " + dir);
                generateItem(dir, id, numPagesInts.next(), pdf, txt, binBitstreams, numBytesInts.next());
            }
            System.out.println("Done.");
        } catch (Exception e) {
            if (e.getMessage() != null) {
                die(e.getMessage());
            }
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static int getPositiveIntOptionValue(final CommandLine cmd, final String opt) {
        if (!cmd.hasOption(opt)) {
            return 1;
        }
        try {
            final int value = Integer.parseInt(cmd.getOptionValue(opt));
            Preconditions.checkArgument(value > 0, "Value is not positive: " + opt);
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Value is not an integer: " + opt);
        }
    }

    private static Options getOptions() {
        final Options options = new Options();
        options.addOption(Option.builder("out").hasArg().required().desc("Output directory. It will be created if it doesn't exist yet.").build());
        options.addOption(Option.builder("num").hasArg().desc("Number of items to generate. Default is 1.").build());
        options.addOption(Option.builder("minbytes").hasArg().desc("Minimum bytes for .bin bitstreams. Default is 1.").build());
        options.addOption(Option.builder("maxbytes").hasArg().desc("Maximum bytes for .bin bitstreams. Default is 1.").build());
        options.addOption(Option.builder("minpages").hasArg().desc("Minimum pages of text for .txt and .pdf bitstreams. Default is 1.").build());
        options.addOption(Option.builder("maxpages").hasArg().desc("Maximum pages of text for .txt and .pdf bitstreams. Default is 1.").build());
        options.addOption(Option.builder("pdf").desc("Include a PDF bitstream for each item, in the ORIGINAL bundle. If specified, this will be the primary bitstream.").build());
        options.addOption(Option.builder("txt").desc("Include a plaintext bitstream for each item, in the TEXT bundle. If specified, this will be the primary bitstream if -pdf is not specified.").build());
        options.addOption(Option.builder("bin").hasArg().desc("Include the given number of binary bitstreams for each item, in the ORIGINAL bundle. If unspecified, none will be added.").build());
        options.addOption(Option.builder("h").desc("Show help").build());
        return options;
    }

    private static void die(final String message) {
        System.out.println("Error: " + message + " (-h for help)");
        System.exit(1);
    }

    private static void printHelpAndExit(final String usage, final String header, final Options options, final String footer) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(getTerminalWidth());
        helpFormatter.printHelp(usage, "\n" + header + "\n\nOptions:", options, "\n" + footer);
        System.exit(0);
    }

    private static int getTerminalWidth() {
        int reportedWidth = TerminalFactory.get().getWidth();
        if (reportedWidth < 50) {
            return 50;
        }
        return reportedWidth;
    }
}
