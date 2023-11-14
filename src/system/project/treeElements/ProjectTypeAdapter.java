package system.project.treeElements;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import com.google.gson.*;
import system.project.treeElements.ProjectFile;
import system.project.treeElements.ProjectFolder;
import system.project.treeElements.ProjectRoot;

import java.lang.reflect.Type;

public class ProjectTypeAdapter implements JsonDeserializer {
    @Override
    public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        // retrieve json
        JsonObject json = jsonElement.getAsJsonObject();

        // case switching for type
        if (json.get("type").getAsString().equals("file"))
            return jsonDeserializationContext.deserialize(jsonElement, ProjectFile.class);
        if (json.get("type").getAsString().equals("folder"))
            return jsonDeserializationContext.deserialize(jsonElement, ProjectFolder.class);
        if (json.get("type").getAsString().equals("root"))
            return jsonDeserializationContext.deserialize(jsonElement, ProjectRoot.class);

        // if unknown type, return nothing
        return null;
    }
}
