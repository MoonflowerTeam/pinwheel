package gg.moonflower.pinwheel.impl.geometry;

import com.google.gson.*;
import gg.moonflower.pinwheel.api.FaceDirection;
import gg.moonflower.pinwheel.api.JsonTupleParser;
import gg.moonflower.pinwheel.api.geometry.GeometryModelData;
import gg.moonflower.pinwheel.impl.PinwheelGsonHelper;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ocelot
 */
@ApiStatus.Internal
public final class Geometry180Parser {

    static final Gson GSON = new GsonBuilder().
            registerTypeAdapter(GeometryModelData.Polygon.class, new GeometryModelData.Polygon.Deserializer()).
            registerTypeAdapter(GeometryModelData.PolyMesh.class, new GeometryModelData.PolyMesh.Deserializer()).
            create();

    private Geometry180Parser() {
    }

    public static GeometryModelData[] parseModel(JsonElement json) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        GeometryModelData data = null;
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (!entry.getKey().startsWith("geometry.")) {
                continue;
            }
            if (data != null) {
                throw new JsonSyntaxException("1.8.0 does not allow multiple geometry definitions per file.");
            }

            JsonObject object = PinwheelGsonHelper.convertToJsonObject(entry.getValue(), entry.getKey());

            // Description
            GeometryModelData.Description description = parseDescription(entry.getKey().substring(9), object);

            // Bones
            GeometryModelData.Bone[] bones;
            if (object.has("bones")) {
                Set<String> usedNames = new HashSet<>();
                JsonArray bonesJson = PinwheelGsonHelper.getAsJsonArray(object, "bones");
                bones = new GeometryModelData.Bone[bonesJson.size()];
                for (int j = 0; j < bones.length; j++) {
                    bones[j] = parseBone(PinwheelGsonHelper.convertToJsonObject(bonesJson.get(j), "bones[" + j + "]"));
                    if (!usedNames.add(bones[j].name())) {
                        throw new JsonSyntaxException("Duplicate bone: " + bones[j].name());
                    }
                }
            } else {
                bones = new GeometryModelData.Bone[0];
            }

            data = new GeometryModelData(description, bones);
        }
        return data != null ? new GeometryModelData[]{data} : new GeometryModelData[0];
    }

    private static GeometryModelData.Description parseDescription(String identifier, JsonObject json) throws JsonParseException {
        float visibleBoundsWidth = PinwheelGsonHelper.getAsFloat(json, "visible_bounds_width", 0);
        float visibleBoundsHeight = PinwheelGsonHelper.getAsFloat(json, "visible_bounds_height", 0);
        float[] visibleBoundsOffset = JsonTupleParser.getFloat(json, "visible_bounds_offset", 3, () -> new float[3]);
        int textureWidth = PinwheelGsonHelper.getAsInt(json, "texturewidth", 256);
        int textureHeight = PinwheelGsonHelper.getAsInt(json, "textureheight", 256);
        boolean preserveModelPose2588 = PinwheelGsonHelper.getAsBoolean(json, "preserve_model_pose2588", false);
        if (textureWidth == 0) {
            throw new JsonSyntaxException("Texture width must not be zero");
        }
        if (textureHeight == 0) {
            throw new JsonSyntaxException("Texture height must not be zero");
        }
        return new GeometryModelData.Description(identifier, visibleBoundsWidth, visibleBoundsHeight, new Vector3f(visibleBoundsOffset[0], visibleBoundsOffset[1], visibleBoundsOffset[2]), textureWidth, textureHeight, preserveModelPose2588);
    }

    private static GeometryModelData.Bone parseBone(JsonObject json) throws JsonParseException {
        String name = PinwheelGsonHelper.getAsString(json, "name");
        boolean reset2588 = PinwheelGsonHelper.getAsBoolean(json, "reset", false);
        boolean neverRender2588 = PinwheelGsonHelper.getAsBoolean(json, "neverrender", false);
        String parent = PinwheelGsonHelper.getAsString(json, "parent", null);
        float[] pivot = JsonTupleParser.getFloat(json, "pivot", 3, () -> new float[3]);
        float[] rotation = JsonTupleParser.getFloat(json, "rotation", 3, () -> new float[3]);
        float[] bindPoseRotation2588 = JsonTupleParser.getFloat(json, "bind_pose_rotation2588", 3, () -> new float[3]);
        boolean mirror = PinwheelGsonHelper.getAsBoolean(json, "mirror", false);
        float inflate = PinwheelGsonHelper.getAsFloat(json, "inflate", 0);
        boolean debug = PinwheelGsonHelper.getAsBoolean(json, "debug", false);

        GeometryModelData.Cube[] cubes = json.has("cubes") ? parseCubes(json) : new GeometryModelData.Cube[0];
        GeometryModelData.Locator[] locators = json.has("locators") ? Geometry110Parser.parseLocators(json) : new GeometryModelData.Locator[0];

        GeometryModelData.PolyMesh polyMesh = json.has("poly_mesh") ? GSON.fromJson(json.get("poly_mesh"), GeometryModelData.PolyMesh.class) : null;

        // TODO texture_mesh

        return new GeometryModelData.Bone(name, reset2588, neverRender2588, parent, new Vector3f(pivot[0], pivot[1], pivot[2]), new Vector3f(rotation[0], rotation[1], rotation[2]), new Vector3f(bindPoseRotation2588[0], bindPoseRotation2588[1], bindPoseRotation2588[2]), mirror, inflate, debug, cubes, locators, polyMesh);
    }

    static GeometryModelData.Cube[] parseCubes(JsonObject json) {
        JsonArray cubesJson = PinwheelGsonHelper.getAsJsonArray(json, "cubes");
        GeometryModelData.Cube[] cubes = new GeometryModelData.Cube[cubesJson.size()];
        for (int i = 0; i < cubesJson.size(); i++)
            cubes[i] = parseCube(PinwheelGsonHelper.convertToJsonObject(cubesJson.get(i), "cubes[" + i + "]"));
        return cubes;
    }

    private static GeometryModelData.Cube parseCube(JsonObject json) throws JsonParseException {
        JsonObject cubeJson = json.getAsJsonObject();
        float[] origin = JsonTupleParser.getFloat(cubeJson, "origin", 3, () -> new float[3]);
        float[] size = JsonTupleParser.getFloat(cubeJson, "size", 3, () -> new float[3]);
        float[] rotation = JsonTupleParser.getFloat(cubeJson, "rotation", 3, () -> new float[3]);
        float[] pivot = JsonTupleParser.getFloat(cubeJson, "pivot", 3, () -> new float[]{origin[0] + size[0] / 2F, origin[1] + size[1] / 2F, origin[2] + size[2] / 2F});
        boolean overrideInflate = cubeJson.has("inflate");
        float inflate = PinwheelGsonHelper.getAsFloat(cubeJson, "inflate", 0);
        boolean overrideMirror = cubeJson.has("mirror");
        boolean mirror = PinwheelGsonHelper.getAsBoolean(cubeJson, "mirror", false);
        GeometryModelData.CubeUV[] uv = parseUV(cubeJson, size);
        if (uv.length != FaceDirection.values().length) {
            throw new JsonParseException("Expected uv to be of size " + FaceDirection.values().length + ", was " + uv.length);
        }
        return new GeometryModelData.Cube(new Vector3f(origin[0], origin[1], origin[2]), new Vector3f(size[0], size[1], size[2]), new Vector3f(rotation[0], rotation[1], rotation[2]), new Vector3f(pivot[0], pivot[1], pivot[2]), overrideInflate, inflate, overrideMirror, mirror, uv);
    }

    private static GeometryModelData.CubeUV[] parseUV(JsonObject cubeJson, float[] size) {
        if (!cubeJson.has("uv")) {
            return new GeometryModelData.CubeUV[6];
        }

        if (cubeJson.get("uv").isJsonArray()) {
            return Geometry110Parser.parseUV(cubeJson, size);
        }
        if (cubeJson.get("uv").isJsonObject()) {
            JsonObject uvJson = cubeJson.getAsJsonObject("uv");
            GeometryModelData.CubeUV[] uvs = new GeometryModelData.CubeUV[6];
            for (FaceDirection direction : FaceDirection.values()) {
                if (!uvJson.has(direction.getName())) {
                    continue;
                }

                JsonObject faceJson = PinwheelGsonHelper.getAsJsonObject(uvJson, direction.getName());
                float[] uv = JsonTupleParser.getFloat(faceJson, "uv", 2, null);
                float[] uvSize = JsonTupleParser.getFloat(faceJson, "uv_size", 2, () -> new float[2]);
                String material = PinwheelGsonHelper.getAsString(faceJson, "material_instance", "texture");
                uvs[direction.get3DDataValue()] = new GeometryModelData.CubeUV(uv[0], uv[1], uvSize[0], uvSize[1], material);
            }
            return uvs;
        }
        throw new JsonSyntaxException("Expected uv to be a JsonArray or JsonObject, was " + PinwheelGsonHelper.getType(cubeJson.get("uv")));
    }
}
