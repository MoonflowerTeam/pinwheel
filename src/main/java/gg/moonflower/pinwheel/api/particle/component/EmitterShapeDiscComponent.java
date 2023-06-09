package gg.moonflower.pinwheel.api.particle.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import gg.moonflower.pinwheel.api.JsonTupleParser;
import gg.moonflower.pinwheel.api.particle.ParticleInstance;
import gg.moonflower.pinwheel.impl.PinwheelGsonHelper;
import io.github.ocelot.molangcompiler.api.MolangEnvironment;
import io.github.ocelot.molangcompiler.api.MolangExpression;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Random;

/**
 * Component that spawns particles in a disc.
 *
 * @author Ocelot
 * @since 1.0.0
 */
public record EmitterShapeDiscComponent(MolangExpression[] normal,
                                        MolangExpression[] offset,
                                        MolangExpression radius,
                                        boolean surfaceOnly,
                                        @Nullable MolangExpression[] direction,
                                        boolean inwards) implements ParticleEmitterShape {

    public static EmitterShapeDiscComponent deserialize(JsonElement json) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        MolangExpression[] normal;
        if (jsonObject.has("plane_normal")) {
            JsonElement planeJson = jsonObject.get("plane_normal");
            if (planeJson.isJsonPrimitive()) {
                String plane = PinwheelGsonHelper.convertToString(planeJson, "plane_normal");
                if ("x".equalsIgnoreCase(plane)) {
                    normal = new MolangExpression[]{
                            MolangExpression.of(1),
                            MolangExpression.ZERO,
                            MolangExpression.ZERO
                    };
                } else if ("y".equalsIgnoreCase(plane)) {
                    normal = new MolangExpression[]{
                            MolangExpression.ZERO,
                            MolangExpression.of(1),
                            MolangExpression.ZERO
                    };
                } else if ("z".equalsIgnoreCase(plane)) {
                    normal = new MolangExpression[]{
                            MolangExpression.ZERO,
                            MolangExpression.ZERO,
                            MolangExpression.of(1)
                    };
                } else {
                    throw new JsonSyntaxException("Expected plane_normal to be an axis(x, y, or z), was " + plane);
                }
            } else {
                normal = JsonTupleParser.getExpression(jsonObject, "plane_normal", 3, () -> new MolangExpression[]{
                        MolangExpression.ZERO,
                        MolangExpression.of(1),
                        MolangExpression.ZERO
                });
            }
        } else {
            normal = new MolangExpression[]{MolangExpression.ZERO, MolangExpression.of(1), MolangExpression.ZERO};
        }

        MolangExpression[] offset = JsonTupleParser.getExpression(jsonObject, "offset", 3, () -> new MolangExpression[]{
                MolangExpression.ZERO,
                MolangExpression.ZERO,
                MolangExpression.ZERO
        });
        MolangExpression radius = JsonTupleParser.getExpression(jsonObject, "radius", () -> MolangExpression.of(1));
        boolean surfaceOnly = PinwheelGsonHelper.getAsBoolean(jsonObject, "surface_only", false);
        Either<Boolean, MolangExpression[]> dir = ParticleComponent.parseDirection(jsonObject, "direction");
        MolangExpression[] direction = dir.right().orElse(null);
        boolean inwards = dir.left().orElse(false);
        return new EmitterShapeDiscComponent(normal, offset, radius, surfaceOnly, direction, inwards);
    }

    @Override
    public void emitParticles(ParticleEmitterShape.Spawner spawner, int count) {
        MolangEnvironment runtime = spawner.getEnvironment();
        Random random = spawner.getRandom();
        float normalX = this.normal[0].safeResolve(runtime);
        float normalY = this.normal[1].safeResolve(runtime);
        float normalZ = this.normal[2].safeResolve(runtime);
        float length = (float) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
        normalX /= length;
        normalY /= length;
        normalZ /= length;

        float a = (float) Math.atan(normalX);
        float b = (float) Math.atan(normalY);
        float c = (float) Math.atan(normalZ);

        Quaternionf quaternion = new Quaternionf(0, 0, 0, 1).rotateZYX(c, b, a);
        Vector3f pos = new Vector3f();
        for (int i = 0; i < count; i++) {
            ParticleInstance particle = spawner.createParticle();
            runtime = particle.getEnvironment();

            float offsetX = this.offset[0].safeResolve(runtime);
            float offsetY = this.offset[1].safeResolve(runtime);
            float offsetZ = this.offset[2].safeResolve(runtime);
            float radius = this.radius.safeResolve(runtime);

            double r = this.surfaceOnly ? radius : radius * Math.sqrt(random.nextFloat());
            double theta = 2 * Math.PI * random.nextFloat();

            float x = (float) (r * Math.cos(theta));
            float y = (float) (r * Math.sin(theta));

            // ax + by + cz = 0

            float dx;
            float dy;
            float dz;
            if (this.direction != null) {
                float directionX = Objects.requireNonNull(this.direction[0]).safeResolve(runtime);
                float directionY = Objects.requireNonNull(this.direction[1]).safeResolve(runtime);
                float directionZ = Objects.requireNonNull(this.direction[2]).safeResolve(runtime);
                pos.set(directionX, directionY, directionZ);
                quaternion.transform(pos);
                dx = pos.x();
                dy = pos.y();
                dz = pos.z();
            } else {
                pos.set(x, 0, y);
                quaternion.transform(pos);

                dx = pos.x();
                dy = pos.y();
                dz = pos.z();
                if (this.inwards) {
                    dx = -dx;
                    dy = -dy;
                    dz = -dz;
                }
            }

            pos.set(x, 0, y);
            quaternion.transform(pos);

            spawner.setPositionVelocity(particle, offsetX + pos.x(), offsetY + pos.y(), offsetZ + pos.z(), dx, dy, dz);
            spawner.spawnParticle(particle);
        }
    }
}
