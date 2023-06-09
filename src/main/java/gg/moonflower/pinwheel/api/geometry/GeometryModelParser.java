package gg.moonflower.pinwheel.api.geometry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import gg.moonflower.pinwheel.api.texture.TextureTable;
import gg.moonflower.pinwheel.impl.geometry.GeometryModelParserImpl;

import java.io.Reader;

/**
 * Helper to read {@link GeometryModelData} from JSON.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public interface GeometryModelParser {

    /**
     * Creates a new geometry model from the specified reader.
     *
     * @param reader The reader to get data from
     * @return A new geometry model from the reader
     */
    static GeometryModelData[] parseModel(Reader reader) throws JsonParseException {
        return parseModel(JsonParser.parseReader(reader));
    }

    /**
     * Creates a new geometry model from the specified reader.
     *
     * @param reader The reader to get data from
     * @return A new geometry model from the reader
     */
    static GeometryModelData[] parseModel(JsonReader reader) throws JsonParseException {
        return parseModel(JsonParser.parseReader(reader));
    }

    /**
     * Creates a new geometry model from the specified JSON string.
     *
     * @param json The raw json string
     * @return A new geometry model from the json
     */
    static GeometryModelData[] parseModel(String json) throws JsonParseException {
        return parseModel(JsonParser.parseString(json));
    }

    /**
     * Creates a new geometry model from the specified JSON element.
     *
     * @param json The parsed json element
     * @return A new geometry model from the json
     */
    static GeometryModelData[] parseModel(JsonElement json) throws JsonParseException {
        return GeometryModelParserImpl.parseModel(json);
    }

    /**
     * Creates a new texture table from the specified reader.
     *
     * @param reader The reader to get data from
     * @return A new texture table from the reader
     */
    static TextureTable parseTextures(Reader reader) throws JsonParseException {
        return parseTextures(JsonParser.parseReader(reader));
    }

    /**
     * Creates a new texture table from the specified reader.
     *
     * @param reader The reader to get data from
     * @return A new texture table from the reader
     */
    static TextureTable parseTextures(JsonReader reader) throws JsonParseException {
        return parseTextures(JsonParser.parseReader(reader));
    }

    /**
     * Creates a new texture table from the specified JSON string.
     *
     * @param json The raw json string
     * @return A new texture table from the json
     */
    static TextureTable parseTextures(String json) throws JsonParseException {
        return parseTextures(JsonParser.parseString(json));
    }

    /**
     * Creates a new texture table from the specified JSON element.
     *
     * @param json The parsed json element
     * @return A new texture table from the json
     */
    static TextureTable parseTextures(JsonElement json) throws JsonParseException {
        return GeometryModelParserImpl.parseTextures(json);
    }
}
