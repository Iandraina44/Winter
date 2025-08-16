package mg.itu.prom16.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import com.opencsv.CSVWriter;

public class CsvConverter {

    public void writeToCsv(Object[] objects, OutputStream outputStream) throws IOException {
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {
            if (objects.length > 0) {
                // Write header
                String[] headers = getHeaders(objects[0]);
                writer.writeNext(headers);

                // Write data rows
                for (Object obj : objects) {
                    String[] data = getData(obj);
                    writer.writeNext(data);
                }
            }
        }
    }

    private static String[] getHeaders(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        String[] headers = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            headers[i] = fields[i].getName();
        }
        return headers;
    }

    private static String[] getData(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        String[] data = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true); // Allow access to private fields
            try {
                Object value = fields[i].get(obj);
                data[i] = (value != null) ? value.toString() : "";
            } catch (IllegalAccessException e) {
                data[i] = ""; // Handle exception, maybe log it
            }
        }
        return data;
    }
}