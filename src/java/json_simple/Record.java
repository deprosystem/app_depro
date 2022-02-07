package json_simple;

import java.util.ArrayList;

public class Record extends ArrayList<FieldSimpl>{

    public Object getValue(String name) {
        FieldSimpl f = getField(name);
        if (f == null) {
            return null;
        } else {
            return f.value;
        }
    }

    public Record addIntField(String name, int value) {
        add(new FieldSimpl(name, FieldSimpl.TYPE_INTEGER, value));
        return this;
    }

    public FieldSimpl getField(String name) {
        if (name == null || name.length() == 0) return null;
        if (name.indexOf(".") < 0) {
            for (FieldSimpl f : this) {
                if (f.name.equals(name)) {
                    return f;
                }
            }
        } else {
            String st;
            String[] nameList = name.split("\\.");
            Record record = this;
            int ik = nameList.length - 1;
            boolean yes;
            for (int i = 0; i < ik; i++) {
                st = nameList[i];
                yes = true;
                for (FieldSimpl f : record ) {
                    if (f.name.equals(st)) {
                        if (f.type == FieldSimpl.TYPE_RECORD){
                            record = (Record) f.value;
                            yes = false;
                            break;
                        } else {
                            return null;
                        }
                    }
                }
                if (yes) {
                    return null;
                }
            }
            st = nameList[ik];
            for (FieldSimpl f : record ) {
                if (f.name.equals(st)) {
                    return f;
                }
            }
        }
        return null;
    }

    public Double getDouble(String name) {
        FieldSimpl f = getField(name);
        if (f != null) {
            switch (f.type) {
                case FieldSimpl.TYPE_FLOAT:
                case FieldSimpl.TYPE_DOUBLE:
                    return (double) f.value;
                case FieldSimpl.TYPE_STRING:
                    return Double.valueOf((String) f.value);
                default:
                    return 0d;
            }
        } else {
            return 0d;
        }
    }

    public Float getFloat(String name) {
        FieldSimpl f = getField(name);
        return getFloatField(f);
    }

    public Float getFloatField(FieldSimpl f) {
        if (f != null) {
            switch (f.type) {
                case FieldSimpl.TYPE_DOUBLE:
                    double d = (double) f.value;
                    return (float) d;
                case FieldSimpl.TYPE_LONG:
                    long ll = (Long) f.value;
                    return (float) ll;
                case FieldSimpl.TYPE_FLOAT:
                case FieldSimpl.TYPE_INTEGER:
                    return (float) f.value;
                case FieldSimpl.TYPE_NULL:
                case FieldSimpl.TYPE_STRING:
                    String st = (String) f.value;
                    if (st == null || st.length() == 0 || st.equals("null")) {
                        return 0f;
                    } else {
                        Float ff = null;
                        st = st.replace(",", ".");
                        try {
                            ff = Float.parseFloat(st);
                        } catch (Exception e) {
                            System.out.println("Поле " + f.name +
                                    " тип STRING не преобразовывается в Float " + e);
                        }
                        return ff;
                    }
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public Long getLong(String name) {
        FieldSimpl f = getField(name);
        return getLongField(f);
    }

    public Long getLongField(FieldSimpl f) {
        if (f != null) {
            switch (f.type) {
                case FieldSimpl.TYPE_LONG:
                    return (Long) f.value;
                case FieldSimpl.TYPE_INTEGER:
                    int i = (Integer) f.value;
                    long l = (long) i;
                    return l;
                case FieldSimpl.TYPE_NULL:
                case FieldSimpl.TYPE_STRING:
                    String st = (String) f.value;
                    if (st == null || st.equals("null")) {
                        return 0l;
                    } else {
                        Long ff = null;
                        try {
                            ff = Long.valueOf(st);
                        } catch (Exception e) {

                        }
                        return ff;
                    }
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public String getString(String name) {
        FieldSimpl f = getField(name);
        if (f != null) {
            return String.valueOf(f.value);
        } else {
            return null;
        }
    }

    public Integer getInteger(String name) {
        FieldSimpl f = getField(name);
        if (f != null) {
            return (Integer)f.value;
        } else {
            return null;
        }
    }

    public int getInt(String name) {
        FieldSimpl f = getField(name);
        return fieldToInt(f);
    }

    public boolean getBoolean(String name) {
        FieldSimpl f = getField(name);
        if (f != null) {
            return (boolean) f.value;
        } else {
            return false;
        }
    }

    public int fieldToInt(FieldSimpl f) {
        if (f != null) {
            if (f.value instanceof Long) {
                long vv = (Long) f.value;
                int ii = (int)vv;
                return ii;
            } else if (f.value instanceof Integer) {
                return (Integer) f.value;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public boolean getBooleanVisibility(String name) {
        FieldSimpl f = getField(name);
        if (f != null) {
            if (f.value == null) {
                return false;
            } else {
                switch (f.type) {
                    case FieldSimpl.TYPE_BOOLEAN : return (boolean) f.value;
                    case FieldSimpl.TYPE_DOUBLE : return ((Double) f.value) != 0d;
                    case FieldSimpl.TYPE_INTEGER : return ((Integer) f.value) != 0;
                    case FieldSimpl.TYPE_LONG : return ((Long) f.value) != 0;
                    case FieldSimpl.TYPE_STRING : return ((String) f.value).length() > 0;
                    case FieldSimpl.TYPE_LIST_RECORD : return ((ListRecords) f.value).size() > 0;
                    case FieldSimpl.TYPE_LIST_FIELD : return ((ListFields) f.value).size() > 0;
                    default: return false;
                }
            }
        } else {
            return false;
        }
    }

    public void setString(String nameField, String value) {
        FieldSimpl f = getField(nameField);
        if (f != null) {
            f.value = value;
        } else {
            f = new FieldSimpl(nameField, FieldSimpl.TYPE_STRING, value);
            add(f);
        }
    }

    public void setInteger(String nameField, Integer value) {
        FieldSimpl f = getField(nameField);
        if (f != null) {
            f.value = value;
        } else {
            f = new FieldSimpl(nameField, FieldSimpl.TYPE_INTEGER, value);
            add(f);
        }
    }

    public void setBoolean(String nameField, Boolean value) {
        FieldSimpl f = getField(nameField);
        if (f != null) {
            f.value = value;
        } else {
            f = new FieldSimpl(nameField, FieldSimpl.TYPE_BOOLEAN, value);
            add(f);
        }
    }

    public void setFloat(String nameField, Float value) {
        FieldSimpl f = getField(nameField);
        if (f != null) {
            f.value = value;
        } else {
            f = new FieldSimpl(nameField, FieldSimpl.TYPE_FLOAT, value);
            add(f);
        }
    }

    public void deleteField(String name) {
        int ik = size();
        for (int i = 0; i < ik; i++) {
            if (name.equals(get(i).name)) {
                remove(i);
                break;
            }
        }
    }

    public Record addField(String name, int type, Object value) {
        return (addField(new FieldSimpl(name, type, value)));
    }

    public Record addField(FieldSimpl ff) {
        FieldSimpl field = getField(ff.name);
        if (field == null) {
            add(ff);
        } else {
            field.type = ff.type;
            field.value = ff.value;
        }
        return this;
    }

    public Record copyRecord() {
        Record result = new Record();
        for (FieldSimpl ff : this) {
            result.addField(new FieldSimpl(ff.name, ff.type, ff.value));
        }
        return result;
    }
/*
    @Override
    public String toString() {
        SimpleRecordToJson recordToJson = new SimpleRecordToJson();
        return recordToJson.recordToJson(this);
    }
*/
}
