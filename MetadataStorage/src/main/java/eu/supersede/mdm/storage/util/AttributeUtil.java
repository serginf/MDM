package eu.supersede.mdm.storage.util;

import eu.supersede.mdm.storage.bdi.extraction.metamodel.NewSourceLevel2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

//added for date

/**
 * @Author Kashif Rabbani
 */
public class AttributeUtil {
    // List of all date formats that we want to parse.
    // Add your own format here.
    private static List<SimpleDateFormat>
            dateFormats = new ArrayList<SimpleDateFormat>() {
        {
            add(new SimpleDateFormat("MM/dd/yyyy"));
            add(new SimpleDateFormat("dd.MM.yyyy"));
            add(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a"));
            add(new SimpleDateFormat("dd.MM.yyyy hh:mm:ss a"));
            add(new SimpleDateFormat("yyyy.MM.dd"));
            add(new SimpleDateFormat("dd-MM-yyyy"));
            add(new SimpleDateFormat("MM-dd-yyyy"));
            add(new SimpleDateFormat("yyyy-MM-dd"));
        }
    };
    /**
     * MySQL Data Types
     */
    private static List<String> mySqlBooleanDataTypes = Arrays.asList(
            "TINYINT"
    );
    private static List<String> mySqlNumberDataTypes = Arrays.asList(
            "SMALLINT",
            "MEDIUMINT",
            "INT",
            "BIGINT"
    );
    private static List<String> mySqlDecimalDataTypes = Arrays.asList(
            "FLOAT",
            "DOUBLE",
            "DECIMAL"
    );

    private static List<String> mySqlDateDataTypes = Arrays.asList(
            "DATE",
            "DATETIME",
            "TIMESTAMP",
            "TIME",
            "YEAR"
    );
    private static List<String> mySqlStringDataTypes = Arrays.asList(
            "CHAR",
            "VARCHAR",
            "TINYTEXT",
            "TEXT",
            "BLOB",
            "MEDIUMTEXT",
            "MEDIUMBLOB",
            "LONGTEXT",
            "LONGBLOB",
            "ENUM",
            "SET"
    );


    public static boolean isDate(Object value) {

        Date date = null;
        String input = (String) value;
        if (value == null)
            return false;
        for (SimpleDateFormat format : dateFormats) {
            try {
                format.setLenient(false);
                date = format.parse(input);
                return true;
            } catch (ParseException e) {

            }
        }
        return false;
    }


    public static boolean isBoolean(Object value) {
        if (value == null)
            return false;
        try {
            Boolean b = (Boolean) value;
            return true;
        } catch (ClassCastException e) {
        }
        return false;
    }

    public static boolean isInteger(Object value) {
        if (value == null)
            return false;
        try {
            Integer i = (Integer) value;
            return true;
        } catch (ClassCastException e) {
        }
        return false;
    }

    public static boolean isDecimal(Object value) {
        if (value == null)
            return false;
        try {
            Double d = (Double) value;
            return true;
        } catch (ClassCastException e) {
        }
        return false;
    }

    public static boolean isStringDecimal(String strValue) {
        try {
            double d = Double.parseDouble(strValue);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isStringInteger(String strValue) {
        try {
            Integer i = Integer.parseInt(strValue);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    public static boolean isStringBoolean(String strValue) {
        strValue = strValue.replaceAll(".", "");
        try {
            if (strValue.equals("True") || strValue.equals("False") || strValue.equals("true") || strValue.equals("false")) {
                Boolean b = Boolean.parseBoolean(strValue);
                return true;
            }

        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static boolean isStringDate(String strValue) {
        Date dateValue = null;
        for (SimpleDateFormat format : dateFormats) {
            try {
                format.setLenient(false);
                dateValue = format.parse(strValue);
                return true;
            } catch (ParseException | DateTimeParseException dfe) {
                return false;
            }
        }
        return true;
    }

    public static String getDataType(Object value) { //String to NewSourceLevel2
        if (isInteger(value)) {
            return NewSourceLevel2.NUMBER.val();
        } else if (isDecimal(value)) {
            return NewSourceLevel2.Decimal.val();
        } else if (isBoolean(value)) {
            return NewSourceLevel2.Boolean.val();
        } else if (isDate(value)) {
            return NewSourceLevel2.Date.val();
        } else {
            return NewSourceLevel2.STRING.val();
        }
    }

    /**
     * Used in XmlSchemaExtraction Class
     * Required for XML Text Nodes (As a text node always returns a String)
     * String value types are detected Manually
     *
     * @param stringValue
     * @return String of RDF Data Type e.g. Integer, Date, String etc.
     */
    public static String getStringDataType(String stringValue) {
        if (isStringInteger(stringValue)) {
            return NewSourceLevel2.NUMBER.val();
        } else if (isStringDecimal(stringValue)) {
            return NewSourceLevel2.Decimal.val();
        } else if (isStringBoolean(stringValue)) {
            return NewSourceLevel2.Boolean.val();
        } else if (isStringDate(stringValue)) {
            return NewSourceLevel2.Date.val();
        } else {
            return NewSourceLevel2.STRING.val();
        }
    }

    /**
     * Used in RelationalToRDFS Class
     * Required to detect proper type for RDF
     *
     * @param columnDataType
     * @return String of RDF Data Type e.g. Integer, Date, String etc.
     */
    public static String getRdbColumnDataTypeURI(String columnDataType) {
        if (mySqlNumberDataTypes.contains(columnDataType)) {
            return NewSourceLevel2.NUMBER.val();
        } else if (mySqlDecimalDataTypes.contains((columnDataType))) {
            return NewSourceLevel2.Decimal.val();
        } else if (mySqlBooleanDataTypes.contains((columnDataType))) {
            return NewSourceLevel2.Boolean.val();
        } else if (mySqlDateDataTypes.contains((columnDataType))) {
            return NewSourceLevel2.Date.val();
        } else if (mySqlStringDataTypes.contains((columnDataType))) {
            return NewSourceLevel2.STRING.val();
        } else {
            return NewSourceLevel2.STRING.val();
        }
    }
}
