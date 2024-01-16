package dk.dbc.ocbtools.testengine.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.type.TypeFactory;

public class Json {
    private static Json instance = new Json();
    private ObjectMapper mapper = new ObjectMapper();

    public Json() {
    }

    public <T> T readValue(String content, Class<T> clazz) throws IOException {
        return this.mapper.readValue(content, clazz);
    }

    public <T> T readValue(InputStream src, Class<T> clazz) throws IOException {
        return this.mapper.readValue(src, clazz);
    }

    public <T> T readValue(File src, Class<T> clazz) throws IOException {
        return this.mapper.readValue(src, clazz);
    }

    public <T> List<T> readArrayValue(String content, Class<T> clazz) throws IOException {
        return (List)this.mapper.readValue(content, TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
    }

    public <T> List<T> readArrayValue(File src, Class<T> clazz) throws IOException {
        return (List)this.mapper.readValue(src, TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
    }

    public String writeValue(Object value) throws IOException {
        return this.mapper.writeValueAsString(value);
    }

    public String writePrettyValue(Object value) throws IOException {
        ObjectWriter objectWriter = this.mapper.writerWithDefaultPrettyPrinter();
        return this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    public static <T> T decode(String content, Class<T> clazz) throws IOException {
        return instance.readValue(content, clazz);
    }

    public static <T> T decode(InputStream src, Class<T> clazz) throws IOException {
        return instance.readValue(src, clazz);
    }

    public static <T> T decode(File src, Class<T> clazz) throws IOException {
        return instance.readValue(src, clazz);
    }

    public static <T> List<T> decodeArray(String content, Class<T> clazz) throws IOException {
        return instance.readArrayValue(content, clazz);
    }

    public static <T> List<T> decodeArray(File src, Class<T> clazz) throws IOException {
        return instance.readArrayValue(src, clazz);
    }

    public static String encode(Object value) throws IOException {
        return instance.writeValue(value);
    }

    public static String encodePretty(Object value) throws IOException {
        return instance.writePrettyValue(value);
    }
}
