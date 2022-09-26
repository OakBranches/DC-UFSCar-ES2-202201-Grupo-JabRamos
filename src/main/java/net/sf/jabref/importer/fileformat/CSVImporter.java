/**
 *
 */
package net.sf.jabref.importer.fileformat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.sf.jabref.importer.OutputPrinter;
import net.sf.jabref.model.entry.BibEntry;


public class CSVImporter extends ImportFormat {

    @Override
    public boolean isRecognizedFormat(InputStream in) throws IOException {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public List<BibEntry> importEntries(InputStream in, OutputPrinter status) throws IOException {

        List<BibEntry> entrys = new ArrayList<>();
        List<String> header = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                List<String> row = Arrays.asList(values);
                if (header != null) {
                    HashMap<String, String> fields = new HashMap<>();
                    int count = 0;
                    String buffer = "";
                    for (int i = 0; (i < row.size()); i++) {
                        String current = row.get(i);
                        if (count >= header.size()) {
                            break;
                        }

                        if ((buffer.length() == 0) && !current.startsWith("\"")) {
                            fields.put(header.get(count), current);
                            count++;
                        } else if ((buffer.length() != 0) || current.startsWith("\"")) {
                            buffer += current;
                            if (current.endsWith("\"")) {
                                fields.put(header.get(count), buffer.substring(1, buffer.length() - 1));
                                buffer = "";
                                count++;
                            }
                        } else {
                            fields.put(header.get(count), current);
                            count++;
                        }

                    }

                    if (!fields.isEmpty()) {
                        entrys.add(BibEntryFromCSV(fields));
                    }

                } else {
                    header = row;
                }
            }
        }

        return entrys;
    }

    private BibEntry BibEntryFromCSV(HashMap<String, String> fields) {

        try {
            System.out.println(fields);
            BibEntry bib = new BibEntry();
            bib.setField(fields);
            bib.setType(getBibType(fields));
            bib.setCiteKey(fields.get("Identifier"));
            return bib;
        } catch (Exception e) {
            System.out.println(e);
        }

        return null;

    }

    private String getBibType(HashMap<String, String> fields) {
        switch (fields.get("BibliographyType")) {
        case "7":
            return "Article";
        case "1":
            return "Book";
        case "2":
            return "Booklet";
        case "5":
            if (fields.get("Booktitle").isEmpty()) {
                return "Inbook";
            }
            return "Incollection";
        case "6":
            return "Inproceedings";
        case "8":
            return "Manual";
        case "9":
            return "Mastersthesis";
        //            return "Phdthesis";
        case "3":
            return "Proceedings";
        case "13":
            return "Techreport";
        case "14":
            return "Unpublished";
        default:
            return "Misc";
        }
    }

    @Override
    public String getFormatName() {
        return "csv";
    }

    @Override
    public String getExtensions() {
        return "csv";
    }

}
