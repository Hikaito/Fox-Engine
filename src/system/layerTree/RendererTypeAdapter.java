package system.layerTree;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.*;
import system.layerTree.data.Folder;
import system.layerTree.data.Layer;

import java.lang.reflect.Type;

public class RendererTypeAdapter implements JsonDeserializer {
    @Override
    public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        // retrieve json
        JsonObject json = jsonElement.getAsJsonObject();

        // case switching for type
        // if type is image, use ImageLayer
        if (json.get("type").getAsString().equals("layer"))
            return jsonDeserializationContext.deserialize(jsonElement, Layer.class);
        // if type is folder, use FolderLayer
        if (json.get("type").getAsString().equals("folder"))
            return jsonDeserializationContext.deserialize(jsonElement, Folder.class);

        // if unknown type, return nothing
        return null;
    }
}
