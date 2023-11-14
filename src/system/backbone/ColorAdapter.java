package system.backbone;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

// gson adapter for color class

import com.google.gson.*;

import java.awt.*;
import java.lang.reflect.Type;

public class ColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
    @Override
    public Color deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Color.decode(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(Color color, Type type, JsonSerializationContext jsonSerializationContext) {
        // get numbers representation
        return new JsonPrimitive(color.getRGB());
    }
}
