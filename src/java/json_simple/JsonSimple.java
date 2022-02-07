package json_simple;

import java.util.Date;

public class JsonSimple {
    private int ind, indMax;
    private String json;
    private String separators = " ,\n";
    private final String quote = "\"";
    private final String quoteComa = quote + ",";
    private final String a = "\\";
    private final String quoteEcran = a + quote;
    private String currentSymbol;
    private String digits = "1234567890.+-";
    int ii = 0;
//    SimpleRecordToJson recordToJson = new SimpleRecordToJson();

    public String nameRecToList;

    public FieldSimpl jsonToModel(String st) throws JsonSyntaxException {
        if (st == null) return null;
        FieldSimpl res = null;
        json = st;
        indMax = st.length();
        ind = -1;
        if (firstSymbol()) {
            res = new FieldSimpl();
            res.name = "";
            switch (currentSymbol) {
                case "[" :
                    res.value = getList();
                    if (res.value instanceof ListRecords) {
                        res.type = FieldSimpl.TYPE_LIST_RECORD;
                    } else {
                        res.type = FieldSimpl.TYPE_LIST_FIELD;
                    }
                    return res;
                case "{" :
                    res.type = FieldSimpl.TYPE_RECORD;
                    res.value = getClazz();
                    return res;
                case quote :
                    res.type = FieldSimpl.TYPE_STRING;
                    res.value = getStringValue();
                    return res;
                default:
                    throw new JsonSyntaxException("Does not start with [ or { : " + textForException());
            }
        }
        return res;
    }
/*
    public String ModelToJson(Record model) {
        if (model != null) {
            return recordToJson.recordToJson(model);
        } else {
            return null;
        }
    }
*/
    private Object getList() throws JsonSyntaxException {
        if (firstSymbol()) {
            if (currentSymbol.equals("{") || currentSymbol.equals("]")) {
                ListRecords list = new ListRecords();
                while (!currentSymbol.equals("]")) {
                    if (currentSymbol.equals("{")) {
                        list.add(getClazz());
                        if (!firstSymbol()) {
                            throw new JsonSyntaxException("No ] " + textForException());
                        }
                    } else {
                        throw new JsonSyntaxException("No { " + textForException());
                    }
                }
                return list;
            } else {
                ListFields listF = new ListFields();
                while (!currentSymbol.equals("]")) {
                    listF.add(getField());
                    if (!firstSymbol()) {
                        throw new JsonSyntaxException("No ] " + textForException());
                    }
                }
                return listF;
            }
        }
        return new ListRecords();
    }

    private String textForException() {
        int in = ind - 200;
        if (in < 0) {
            in = 0;
        }
        int ik = ind + 150;
        if (ik > indMax) {
            ik = indMax;
        }
        return "near position: " + ind + " text >>" + json.substring(in, ik) + "<<";
    }

    private FieldSimpl getField() throws JsonSyntaxException {
        FieldSimpl item = new FieldSimpl();
        item.name = "";
        switch (currentSymbol) {
            case quote : // String
                FieldSimpl fs = getStringValue();
                item.type = fs.type;
                item.value = fs.value;
                break;
            case "n" :   // null
                item.type = FieldSimpl.TYPE_NULL;
                item.value = getNullValue();
                break;
            case "f" :   // boolean
            case "t" :
                item.type = FieldSimpl.TYPE_BOOLEAN;
                item.value = getBooleanValue();
                break;
            default:
                if (digits.contains(currentSymbol)) {    // digit
//                                item.type = Field.TYPE_INTEGER;
                    FieldSimpl f = getDigitalValue(item.name);
                    item.value = f.value;
                    item.type = f.type;
                } else {
                }
        }
        return item;
    }

    private Record getClazz() throws JsonSyntaxException {
        Record list = new Record();
        if (firstSymbol()) {
            while ( ! currentSymbol.equals("}")) {
                 if (currentSymbol.equals(quote)) {
                     FieldSimpl item = getValue();
                     if (item == null) {
                         return list;
                     }
                     list.add(item);
                     if ( ! firstSymbol()) {
                         throw new JsonSyntaxException("No } " + textForException());
                     }
                } else {
                    if (ind < indMax) {
                        throw new JsonSyntaxException("Expected \" " + textForException());
//                        firstSymbol();
                    } else {
                        throw new JsonSyntaxException("No } " + textForException());
                    }
                }
            }
        }
        return list;
    }

    private FieldSimpl getValue() throws JsonSyntaxException {
        FieldSimpl item = new FieldSimpl();
        item.name = getName(quote);
        if (item.name != null && firstSymbol()) {
            if (currentSymbol.equals(":")) {
                if (firstSymbol()) {
                    switch (currentSymbol) {
                        case quote : // String
                            FieldSimpl fs = getStringValue();
                            item.type = fs.type;
                            item.value = fs.value;
                            break;
                        case "n" :   // null
                            item.type = FieldSimpl.TYPE_NULL;
                            item.value = getNullValue();
                            break;
                        case "f" :   // boolean
                        case "t" :
                            item.type = FieldSimpl.TYPE_BOOLEAN;
                            item.value = getBooleanValue();
                            break;
                        case "[" :   // List
                            item.value = getList();
                            if (item.value instanceof ListRecords) {
                                item.type = FieldSimpl.TYPE_LIST_RECORD;
                            } else {
                                item.type = FieldSimpl.TYPE_LIST_FIELD;
                            }
                            break;
                        case "{" :   // Class
                            item.type = FieldSimpl.TYPE_RECORD;
                            item.value = getClazz();
                            if (nameRecToList != null && nameRecToList.length() > 0
                                    && nameRecToList.equals(item.name)) {
                                recToList(item);
                            }
                            break;
                        default:
                            if (digits.contains(currentSymbol)) {    // digit
                                FieldSimpl f = getDigitalValue(item.name);
                                item.value = f.value;
                                item.type = f.type;
//                                item.value = getIntegerValue();
                            } else {
                            }
                    }
                } else {
                    throw new JsonSyntaxException("No value " + textForException());
                }
            } else {
                throw new JsonSyntaxException("No : " + textForException());
            }
        } else {
            throw new JsonSyntaxException("No : " + textForException());
        }
        return item;
    }

    private void recToList(FieldSimpl item) {
        Record rec = (Record) item.value;
        int ik = rec.size();
        if (ik > 0) {
            FieldSimpl f = rec.get(0);
            if (f.type == FieldSimpl.TYPE_RECORD) {
                ListRecords lr = new ListRecords();
                for (int i = 0; i < ik; i++) {
                    lr.add((Record) rec.get(i).value);
                }
                item.type = FieldSimpl.TYPE_LIST_RECORD;
                item.value = lr;
            } else {
                ListFields lf = new ListFields();
                for (int i = 0; i < ik; i++) {
                    f = rec.get(i);
                    lf.add(new FieldSimpl("", f.type, f. value));
                }
                item.type = FieldSimpl.TYPE_LIST_FIELD;
                item.value = lf;
            }
        } else {
            item.type = FieldSimpl.TYPE_LIST_RECORD;
            item.value = new ListRecords();
        }
    }

    private Object getNullValue() throws JsonSyntaxException {
        String st = json.substring(ind, ind + 4);
        if (st.toUpperCase().equals("NULL")) {
            ind+=3;
        } else {
            throw new JsonSyntaxException("No NULL " + textForException());
        }
        return null;
    }

    private Boolean getBooleanValue() {
        String st;
        switch (currentSymbol) {
            case "f" :
                st = json.substring(ind, ind + 5);
                if (st.toUpperCase().equals("FALSE")) {
                    ind+=4;
                    return new Boolean(false);
                } else {
                    return null;
                }
            case "t" :
                st = json.substring(ind, ind + 4);
                if (st.toUpperCase().equals("TRUE")) {
                    ind+=3;
                    return new Boolean(true);
                } else {
                    return null;
                }
            default:
            return null;
        }
    }

    private FieldSimpl getStringValue() throws JsonSyntaxException {
        int i = ind, j;
        do {
            j = i + 1;
            i = json.indexOf(quote, j);
            if (i < 0) {
                throw new JsonSyntaxException("Not " + quote +" " + textForException());
            }
        } while (json.substring(i - 1, i).equals("\\"));
        String st = json.substring(ind + 1, i);
        st = delSlash(st);
        ind = i;
        FieldSimpl field = new FieldSimpl();
        field.name = "";
        if (st.startsWith("/D")) {
            String t = st.substring(6, 18);
            Date d = new Date(Long.valueOf(t));
            field.type = FieldSimpl.TYPE_DATE;
            field.value = d;
        } else {
            field.type = FieldSimpl.TYPE_STRING;
            field.value = st;
        }
        return field;
    }

    private String delSlash(String st) {
        char[] c = st.toCharArray();
        StringBuilder builder = new StringBuilder();
        int i1;
        int ik = c.length;
//        boolean isYetSlash = false;
        for (int i = 0; i < ik; i++) {
            if (c[i] == '\\') {
                i1 = i + 1;
                if (i1 < ik ) {
                    char c1 = c[i1];
                    if (c1 == 'u') {
                        int iu = i + 5;
                        if (iu < ik) {
                            char cu = (char) Integer.parseInt(new String(new char[]{c[i + 2], c[i + 3], c[i + 4], c[i + 5]}), 16);
                            builder.append(cu);
                            i = iu;
                        }
                    } else if (c1 == 'r' || c1 == 'n' || c1 == 't') {
                        i++;
                    }
                }
            } else {
                builder.append(c[i]);
            }
        }
        String result = builder.toString();
        return result;
    }

    private Integer getIntegerValue() {
        int j = -1;
        int l = json.length();
        for (int i = ind; i < l; i++) {
            if ( ! digits.contains(json.substring(i, i + 1))) {
                j = i;
                break;
            }
        }
        if (j == -1) {
            return null;
        } else {
            String st = json.substring(ind, j);
            ind = j - 1;
            return Integer.valueOf(st);
        }
    }

    private FieldSimpl getDigitalValue(String name) {
        FieldSimpl f = new FieldSimpl();
        f.name = name;
        int j = -1;
        int l = json.length();
        for (int i = ind; i < l; i++) {
            if ( ! digits.contains(json.substring(i, i + 1))) {
                j = i;
                break;
            }
        }
        if (j == -1) {
            return null;
        } else {
            String st = json.substring(ind, j);
            ind = j - 1;
            if (st.contains(".")) {
                Double d = Double.valueOf(st);
                f.type = FieldSimpl.TYPE_DOUBLE;
                f.value = d;
                return f;
            } else {
                f.type = FieldSimpl.TYPE_LONG;
                f.value = Long.valueOf(st);
                return f;
            }
        }
    }

    private int indexOf(String separ, int begin) {
        int l = json.length();
        for (int i = begin; i < l; i++) {
            if (separ.contains(json.substring(i, i + 1))) {
                return i;
            }
        }
        return -1;
    }

    private String getName(String separ) throws JsonSyntaxException {
        String st = "";
        int i = json.indexOf(quote, ind + 1);
        if (i > -1) {
            st = json.substring(ind + 1, i);
            ind = i;
        } else {
            throw new JsonSyntaxException("Not name " + textForException());
        }
        return st;
    }

    private boolean firstSymbol() {
        ind++;
        currentSymbol = json.substring(ind, ind + 1);
        while (separators.contains(currentSymbol)) {
            ind++;
            if (ind < indMax) {
                currentSymbol = json.substring(ind, ind + 1);
            } else {
                break;
            }
        }
        return ind < indMax;
    }
}
