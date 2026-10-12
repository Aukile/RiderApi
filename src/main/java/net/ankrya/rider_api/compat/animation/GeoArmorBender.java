package net.ankrya.rider_api.compat.animation;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Pair;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.event.GeoRenderEvent;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.*;

/**
 * GeckoLib 盔甲弯曲适配。
 * 监听 GeoArmor 渲染事件，将玩家动画的 bend 变形应用到盔甲骨骼/网格，
 * 使 GeckoLib 盔甲能跟随 playeranim 动画的肢体弯曲。
 *
 * 完全客户端隔离，服务端不会加载。
 */
@OnlyIn(Dist.CLIENT)
public final class GeoArmorBender {
    private static final net.minecraft.resources.ResourceLocation GEO_ARMOR_BEND_OWNER_KEY =
        PlayerAnimator.GEO_ARMOR_BEND_OWNER_KEY;
    private static final Map<GeoArmorRenderer<?>, List<SavedBonePose>> SAVED_ARMOR_POSES =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Map<GeoArmorRenderer<?>, List<SavedBoneMesh>> SAVED_ARMOR_MESHES =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Map<GeoBone, List<GeoCube>> ACTIVE_ARMOR_MESHES =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static final float AUTO_JOINT_OFFSET = 6F / 16F;
    private static final float AUTO_BLEND_RADIUS = 2F / 16F;

    private GeoArmorBender() {}

    /**
     * 在客户端 setup（FMLClientSetupEvent）里调用一次以注册事件。
     */
    public static void  register() {
        MinecraftForge.EVENT_BUS.addListener(GeoArmorBender::onGeoArmorPreRender);
        MinecraftForge.EVENT_BUS.addListener(GeoArmorBender::onGeoArmorPostRender);
    }

    private static void onGeoArmorPreRender(GeoRenderEvent.Armor.Pre event) {
        restoreArmorState(event.getRenderer());
        if (!(event.getEntity() instanceof AbstractClientPlayer player)
                || !(player instanceof IAnimatedPlayer animatedPlayer)) {
            return; // restoreArmorState 已在上面执行，安全
        }

        AnimationApplier animation = animatedPlayer.playerAnimator_getAnimation();
        if (!animation.isActive()) {
            return; // 同样已还原，安全
        }

        if (!ownsGeoArmorBend(player))
            return;

        List<SavedBonePose> savedPoses = new java.util.ArrayList<>(4);
        List<SavedBoneMesh> savedMeshes = new java.util.ArrayList<>(4);
        Set<GeoBone> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        if (checkCubes(event.getModel(), "armorRightArm", "armorLeftArm")) {
            if (!applyArmorBend(event.getModel(), animation, "rightArm", savedPoses, visited,
                    "rightforearm", "right_forearm", "rightForearm", "armorRightForearm",
                    "rightLowerArm", "right_lower_arm"))
                applyAutomaticArmorBend(event.getRenderer(), event.getModel(), animation, "rightArm", savedMeshes,
                        "armorRightArm", "rightArm", "right_arm", "rightSleeve");
            if (!applyArmorBend(event.getModel(), animation, "leftArm", savedPoses, visited,
                    "leftforearm", "left_forearm", "leftForearm", "armorLeftForearm",
                    "leftLowerArm", "left_lower_arm"))
                applyAutomaticArmorBend(event.getRenderer(), event.getModel(), animation, "leftArm", savedMeshes,
                        "armorLeftArm", "leftArm", "left_arm", "leftSleeve");
        }
        if (checkCubes(event.getModel(), "armorLeftLeg", "armorLeftBoot", "armorRightLeg", "armorRightBoot")) {
            if (!applyArmorBend(event.getModel(), animation, "rightLeg", savedPoses, visited,
                    "rightforeleg", "right_foreleg", "rightForeleg", "armorRightForeleg",
                    "rightLowerLeg", "right_lower_leg"))
                applyAutomaticArmorBend(event.getRenderer(), event.getModel(), animation, "rightLeg", savedMeshes,
                        "armorRightLeg", "rightLeg", "right_leg", "armorRightBoot", "rightBoot", "right_boot");
            if (!applyArmorBend(event.getModel(), animation, "leftLeg", savedPoses, visited,
                    "leftforeleg", "left_foreleg", "leftForeleg", "armorLeftForeleg",
                    "leftLowerLeg", "left_lower_leg"))
                applyAutomaticArmorBend(event.getRenderer(), event.getModel(), animation, "leftLeg", savedMeshes,
                        "armorLeftLeg", "leftLeg", "left_leg", "armorLeftBoot", "leftBoot", "left_boot");
        }

        if (!savedPoses.isEmpty())
            SAVED_ARMOR_POSES.put(event.getRenderer(), savedPoses);
        if (!savedMeshes.isEmpty())
            SAVED_ARMOR_MESHES.put(event.getRenderer(), savedMeshes);
    }

    private static boolean checkCubes(BakedGeoModel model, String... bones){
        for (String bone : bones) {
            java.util.Optional<GeoBone> geoBone = model.getBone(bone);
            if (geoBone.isEmpty()) continue;
            if (!geoBone.get().getCubes().isEmpty()) return true;
        }
        return false;
    }
    /**
     * 检查当前是否由本 API 持有 GeoArmor 弯曲权。
     * 多个模组都实现了 GeoArmorBend 时，通过共享 key 协调，只有持有者执行变形。
     */
    private static boolean ownsGeoArmorBend(AbstractClientPlayer player) {
        var data = PlayerAnimationAccess.getPlayerAssociatedData(player);

        // 取 API 当前活跃的层（ANIMATION 优先，其次 RIDE）
        IAnimation ownLayer =
                data.get(PlayerAnimator.instance().ANIMATION);
        if (ownLayer == null || !ownLayer.isActive())
            ownLayer = data.get(PlayerAnimator.instance().RIDE);
        if (ownLayer == null || !ownLayer.isActive())
            return false;

        IAnimation owner = data.get(GEO_ARMOR_BEND_OWNER_KEY);
        if (owner == ownLayer)
            return true; // 本 API 已持有
        if (owner == null || !owner.isActive()) {
            // 无人持有或持有者已失效，本 API 抢占
            data.set(GEO_ARMOR_BEND_OWNER_KEY, ownLayer);
            return true;
        }
        return false; // 其他模组持有，让出
    }

    private static void onGeoArmorPostRender(GeoRenderEvent.Armor.Post event) {
        restoreArmorState(event.getRenderer());
    }

    private static boolean applyArmorBend(BakedGeoModel model,
                                          AnimationApplier animation, String playerPart,
                                          List<SavedBonePose> savedPoses,
                                          Set<GeoBone> visited,
                                          String... armorBoneNames) {
        List<GeoBone> bones =
                findArmorBones(model, armorBoneNames);
        bones.removeIf(bone -> !hasVisibleArmorGeometry(bone));
        if (bones.isEmpty())
            return false;

        Pair<Float, Float> bend = animation.getBend(playerPart);
        if (Math.abs(bend.getRight()) < 0.00001F)
            return true;

        float axis = bend.getLeft();
        Quaternionf rotation = new Quaternionf().rotateAxis(
                bend.getRight(), (float)Math.cos(axis), 0, (float)Math.sin(axis));
        float x = (float)Math.atan2(2F * (rotation.w() * rotation.x() + rotation.y() * rotation.z()),
                1F - 2F * (rotation.x() * rotation.x() + rotation.y() * rotation.y()));
        float yInput = 2F * (rotation.w() * rotation.y() - rotation.z() * rotation.x());
        float y = (float)Math.asin(Math.max(-1F, Math.min(1F, yInput)));
        float z = (float)Math.atan2(2F * (rotation.w() * rotation.z() + rotation.x() * rotation.y()),
                1F - 2F * (rotation.y() * rotation.y() + rotation.z() * rotation.z()));

        for (GeoBone bone : bones) {
            if (visited.add(bone)) {
                savedPoses.add(new SavedBonePose(bone));
                bone.updateRotation(bone.getRotX() - x, bone.getRotY() - y, bone.getRotZ() + z);
            }
        }
        return true;
    }

    private static void applyAutomaticArmorBend(
            GeoArmorRenderer<?> renderer,
            BakedGeoModel model,
            AnimationApplier animation, String playerPart,
            List<SavedBoneMesh> savedMeshes, String... armorBoneNames) {
        Pair<Float, Float> bend = animation.getBend(playerPart);
        if (Math.abs(bend.getRight()) < 0.00001F)
            return;

        Quaternionf rotation = toGeckoBendRotation(bend);
        List<GeoBone> bones =
                findRendererLimbBones(renderer, playerPart);
        boolean hasRendererMapping = !bones.isEmpty();
        if (!hasRendererMapping)
            bones = findArmorBones(model, armorBoneNames);
        bones.removeIf(bone -> !hasVisibleArmorGeometry(bone));
        if (bones.isEmpty() && !hasRendererMapping)
            bones = findLikelyLimbBones(model, playerPart);
        for (GeoBone bone : bones) {
            deformArmorBone(bone, rotation, savedMeshes, false);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<GeoBone> findRendererLimbBones(
            GeoArmorRenderer<?> renderer, String playerPart) {
        List<GeoBone> bones =
                new java.util.ArrayList<>(2);
        GeoArmorRenderer rawRenderer = renderer;
        software.bernie.geckolib.model.GeoModel geoModel = rawRenderer.getGeoModel();
        if ("rightArm".equals(playerPart)) {
            addUniqueBone(bones, rawRenderer.getRightArmBone());
        } else if ("leftArm".equals(playerPart)) {
            addUniqueBone(bones, rawRenderer.getLeftArmBone());
        } else if ("rightLeg".equals(playerPart)) {
            addUniqueBone(bones, rawRenderer.getRightLegBone());
            addUniqueBone(bones, rawRenderer.getRightBootBone());
        } else if ("leftLeg".equals(playerPart)) {
            addUniqueBone(bones, rawRenderer.getLeftLegBone());
            addUniqueBone(bones, rawRenderer.getLeftBootBone());
        }
        return bones;
    }

    private static void addUniqueBone(
            List<GeoBone> bones,
            GeoBone bone) {
        if (bone != null && !bones.contains(bone))
            bones.add(bone);
    }

    private static Quaternionf toGeckoBendRotation(
            Pair<Float, Float> bend) {
        float axis = bend.getLeft();
        Quaternionf rotation = new Quaternionf().rotateAxis(
                bend.getRight(), (float)Math.cos(axis), 0, (float)Math.sin(axis));
        float x = (float)Math.atan2(2F * (rotation.w() * rotation.x() + rotation.y() * rotation.z()),
                1F - 2F * (rotation.x() * rotation.x() + rotation.y() * rotation.y()));
        float yInput = 2F * (rotation.w() * rotation.y() - rotation.z() * rotation.x());
        float y = (float)Math.asin(Math.max(-1F, Math.min(1F, yInput)));
        float z = (float)Math.atan2(2F * (rotation.w() * rotation.z() + rotation.x() * rotation.y()),
                1F - 2F * (rotation.y() * rotation.y() + rotation.z() * rotation.z()));
        return new Quaternionf().rotationZYX(z, -y, -x);
    }

    private static List<GeoBone> findArmorBones(
            BakedGeoModel model, String... names) {
        Set<String> normalizedNames = new java.util.HashSet<>();
        for (String name : names)
            normalizedNames.add(normalizeBoneName(name));

        List<GeoBone> matches = new java.util.ArrayList<>();
        for (GeoBone root : model.topLevelBones())
            collectArmorBones(root, normalizedNames, matches);
        return matches;
    }

    private static List<GeoBone> findLikelyLimbBones(
            BakedGeoModel model, String playerPart) {
        String side = playerPart.startsWith("right") ? "right" : "left";
        String limb = playerPart.endsWith("Arm") ? "arm" : "leg";
        List<GeoBone> all = new java.util.ArrayList<>();
        for (GeoBone root : model.topLevelBones())
            collectRenderableBones(root, all);

        List<GeoBone> named = new java.util.ArrayList<>();
        for (GeoBone bone : all) {
            String name = normalizeBoneName(bone.getName());
            if (name.contains(side) && name.contains(limb)
                    && !name.contains("fore") && !name.contains("lower"))
                named.add(bone);
        }
        if (!named.isEmpty())
            return named;

        GeoBone best = null;
        float bestScore = Float.MAX_VALUE;
        float expectedX = side.equals("right") ? 5F : -5F;
        float expectedY = limb.equals("arm") ? 22F : 12F;
        for (GeoBone bone : all) {
            if ((side.equals("right") && bone.getPivotX() <= 0)
                    || (side.equals("left") && bone.getPivotX() >= 0))
                continue;
            float score = Math.abs(bone.getPivotX() - expectedX)
                    + Math.abs(bone.getPivotY() - expectedY) * 1.5F;
            if (score < bestScore) {
                best = bone;
                bestScore = score;
            }
        }
        return best != null && bestScore < 10F
                ? new java.util.ArrayList<>(List.of(best))
                : new java.util.ArrayList<>();
    }

    private static void collectRenderableBones(GeoBone bone,
                                               List<GeoBone> matches) {
        if (!bone.getCubes().isEmpty() && isArmorBoneVisible(bone))
            matches.add(bone);
        for (GeoBone child : bone.getChildBones())
            collectRenderableBones(child, matches);
    }

    private static void collectArmorBones(GeoBone bone,
                                          Set<String> names,
                                          List<GeoBone> matches) {
        if (names.contains(normalizeBoneName(bone.getName())))
            matches.add(bone);
        for (GeoBone child : bone.getChildBones())
            collectArmorBones(child, names, matches);
    }

    private static String normalizeBoneName(String name) {
        return name.toLowerCase(java.util.Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
    }

    private static boolean isArmorBoneVisible(GeoBone bone) {
        if (bone.isHidden())
            return false;
        for (GeoBone parent = bone.getParent();
             parent != null; parent = parent.getParent()) {
            if (parent.isHidingChildren())
                return false;
        }
        return true;
    }

    private static boolean hasVisibleArmorGeometry(GeoBone bone) {
        if (!bone.getCubes().isEmpty() && isArmorBoneVisible(bone))
            return true;
        for (GeoBone child : bone.getChildBones()) {
            if (hasVisibleArmorGeometry(child))
                return true;
        }
        return false;
    }

    private static void deformArmorBone(GeoBone bone,
                                        Quaternionf fullRotation, List<SavedBoneMesh> savedMeshes,
                                        boolean rigidHinge) {
        float jointY = bone.getPivotY() / 16F - AUTO_JOINT_OFFSET;
        Vector3f joint = new Vector3f(
                bone.getPivotX() / 16F, jointY, bone.getPivotZ() / 16F);
        Set<GeoBone> visited =
                Collections.newSetFromMap(new IdentityHashMap<>());
        deformArmorBoneTree(bone, joint, fullRotation, new Matrix4f(),
                savedMeshes, rigidHinge, visited);
    }

    private static void deformArmorBoneTree(GeoBone bone,
                                            Vector3f joint, Quaternionf fullRotation,
                                            Matrix4f boneTransform,
                                            List<SavedBoneMesh> savedMeshes, boolean rigidHinge,
                                            Set<GeoBone> visited) {
        if (!visited.add(bone))
            return;

        List<GeoCube> originalCubes;
        synchronized (ACTIVE_ARMOR_MESHES) {
            List<GeoCube> stale =
                    ACTIVE_ARMOR_MESHES.remove(bone);
            if (stale != null) {
                bone.getCubes().clear();
                bone.getCubes().addAll(stale);
            }
            originalCubes = List.copyOf(bone.getCubes());
        }
        if (!originalCubes.isEmpty() && isArmorBoneVisible(bone)) {
            List<GeoCube> deformed =
                    new java.util.ArrayList<>(originalCubes.size());
            boolean changed = false;

            for (GeoCube cube : originalCubes) {
                GeoCube result =
                        deformArmorCube(cube, joint, fullRotation, boneTransform, rigidHinge);
                deformed.add(result);
                changed |= result != cube;
            }

            if (changed) {
                synchronized (ACTIVE_ARMOR_MESHES) {
                    ACTIVE_ARMOR_MESHES.put(bone, originalCubes);
                }
                savedMeshes.add(new SavedBoneMesh(bone, originalCubes));
                bone.getCubes().clear();
                bone.getCubes().addAll(deformed);
            }
        }

        for (GeoBone child : bone.getChildBones()) {
            Matrix4f childTransform =
                    new Matrix4f(boneTransform).mul(localBoneTransform(child));
            deformArmorBoneTree(child, joint, fullRotation, childTransform,
                    savedMeshes, rigidHinge, visited);
        }
    }

    private static GeoCube deformArmorCube(
            GeoCube cube, Vector3f joint,
            Quaternionf fullRotation, Matrix4f boneTransform,
            boolean rigidHinge) {
        Matrix4f cubeTransform =
                new Matrix4f(boneTransform).mul(cubeTransform(cube));
        Matrix4f inverseCubeTransform = new Matrix4f(cubeTransform).invert();
        Matrix3f cubeNormalTransform = new Matrix3f(cubeTransform);
        Matrix3f inverseCubeNormalTransform = new Matrix3f(cubeTransform).invert();
        List<GeoQuad> output = new java.util.ArrayList<>();
        boolean changed = false;

        for (GeoQuad quad : cube.quads()) {
            if (quad == null)
                continue;

            List<BendVertex> polygon = new java.util.ArrayList<>(quad.vertices().length);
            for (GeoVertex vertex : quad.vertices()) {
                Vector3f position = cubeTransform.transformPosition(
                        new Vector3f(vertex.position()));
                polygon.add(new BendVertex(position, vertex.texU(), vertex.texV()));
                changed |= rigidHinge
                        ? position.y() < joint.y() - 0.00001F
                        : bendWeight(position.y(), joint.y()) > 0.00001F;
            }

            if (!changed && output.isEmpty()) {
                output.add(quad);
                continue;
            }

            Vector3f sourceNormal = cubeNormalTransform.transform(
                    new Vector3f(quad.normal())).normalize();
            List<List<BendVertex>> slices = rigidHinge
                    ? slicePolygonAtJoint(polygon, joint.y())
                    : slicePolygon(polygon, joint.y());
            for (List<BendVertex> slice : slices)
                emitBentPolygon(slice, sourceNormal, quad.direction(), joint, fullRotation,
                        inverseCubeTransform, inverseCubeNormalTransform, output, rigidHinge);
        }

        if (!changed)
            return cube;

        return new GeoCube(
                output.toArray(GeoQuad[]::new),
                cube.pivot(), cube.rotation(), cube.size(), cube.inflate(), cube.mirror());
    }

    private static Matrix4f cubeTransform(GeoCube cube) {
        float px = (float)cube.pivot().x() / 16F;
        float py = (float)cube.pivot().y() / 16F;
        float pz = (float)cube.pivot().z() / 16F;
        return new Matrix4f()
                .translate(px, py, pz)
                .rotateZ((float)cube.rotation().z())
                .rotateY((float)cube.rotation().y())
                .rotateX((float)cube.rotation().x())
                .translate(-px, -py, -pz);
    }

    private static Matrix4f localBoneTransform(
            GeoBone bone) {
        float px = bone.getPivotX() / 16F;
        float py = bone.getPivotY() / 16F;
        float pz = bone.getPivotZ() / 16F;
        return new Matrix4f()
                .translate(-bone.getPosX() / 16F, bone.getPosY() / 16F, bone.getPosZ() / 16F)
                .translate(px, py, pz)
                .rotateZ(bone.getRotZ())
                .rotateY(bone.getRotY())
                .rotateX(bone.getRotX())
                .scale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ())
                .translate(-px, -py, -pz);
    }

    private static List<List<BendVertex>> slicePolygon(
            List<BendVertex> polygon, float jointY) {
        float[] cuts = new float[] {
                jointY - AUTO_BLEND_RADIUS,
                jointY - AUTO_BLEND_RADIUS * 0.5F,
                jointY,
                jointY + AUTO_BLEND_RADIUS * 0.5F,
                jointY + AUTO_BLEND_RADIUS
        };
        List<List<BendVertex>> slices = new java.util.ArrayList<>(6);
        for (int i = 0; i <= cuts.length; i++) {
            List<BendVertex> slice = polygon;
            if (i > 0)
                slice = clipPolygonAtY(slice, cuts[i - 1], true);
            if (i < cuts.length)
                slice = clipPolygonAtY(slice, cuts[i], false);
            slice = removeDuplicateVertices(slice);
            if (slice.size() >= 3)
                slices.add(slice);
        }
        return slices;
    }

    private static List<List<BendVertex>> slicePolygonAtJoint(
            List<BendVertex> polygon, float jointY) {
        List<List<BendVertex>> slices = new java.util.ArrayList<>(2);
        List<BendVertex> lower =
                removeDuplicateVertices(clipPolygonAtY(polygon, jointY, false));
        List<BendVertex> upper =
                removeDuplicateVertices(clipPolygonAtY(polygon, jointY, true));
        if (lower.size() >= 3)
            slices.add(lower);
        if (upper.size() >= 3)
            slices.add(upper);
        return slices;
    }

    private static List<BendVertex> clipPolygonAtY(
            List<BendVertex> input, float boundary, boolean keepAbove) {
        List<BendVertex> output = new java.util.ArrayList<>();
        if (input.isEmpty())
            return output;

        BendVertex previous = input.get(input.size() - 1);
        boolean previousInside = keepAbove
                ? previous.position().y() >= boundary - 0.000001F
                : previous.position().y() <= boundary + 0.000001F;
        for (BendVertex current : input) {
            boolean currentInside = keepAbove
                    ? current.position().y() >= boundary - 0.000001F
                    : current.position().y() <= boundary + 0.000001F;
            if (currentInside != previousInside) {
                float denominator = current.position().y() - previous.position().y();
                float t = Math.abs(denominator) < 0.000001F ? 0F
                        : (boundary - previous.position().y()) / denominator;
                Vector3f intersection = new Vector3f(previous.position())
                        .lerp(current.position(), t);
                output.add(new BendVertex(intersection,
                        previous.u() + (current.u() - previous.u()) * t,
                        previous.v() + (current.v() - previous.v()) * t));
            }
            if (currentInside)
                output.add(current);
            previous = current;
            previousInside = currentInside;
        }
        return output;
    }

    private static List<BendVertex> removeDuplicateVertices(List<BendVertex> input) {
        List<BendVertex> output = new java.util.ArrayList<>(input.size());
        for (BendVertex vertex : input) {
            if (output.isEmpty() || output.get(output.size() - 1).position()
                    .distanceSquared(vertex.position()) > 0.0000000001F)
                output.add(vertex);
        }
        if (output.size() > 1 && output.get(0).position()
                .distanceSquared(output.get(output.size() - 1).position()) <= 0.0000000001F)
            output.remove(output.size() - 1);
        return output;
    }

    private static void emitBentPolygon(List<BendVertex> polygon,
                                        Vector3f sourceNormal, Direction direction,
                                        Vector3f joint, Quaternionf fullRotation,
                                        Matrix4f inverseCubeTransform,
                                        Matrix3f inverseCubeNormalTransform,
                                        List<GeoQuad> output,
                                        boolean rigidHinge) {
        List<BendVertex> bent = new java.util.ArrayList<>(polygon.size());
        float averageWeight = 0;
        float rigidWeight = 0;
        if (rigidHinge) {
            float averageY = 0;
            for (BendVertex vertex : polygon)
                averageY += vertex.position().y();
            rigidWeight = averageY / polygon.size() < joint.y() - 0.000001F ? 1F : 0F;
        }
        for (BendVertex vertex : polygon) {
            float weight = rigidHinge ? rigidWeight : bendWeight(vertex.position().y(), joint.y());
            averageWeight += weight;
            Quaternionf rotation = new Quaternionf().slerp(fullRotation, weight);
            Vector3f position = new Vector3f(vertex.position())
                    .sub(joint).rotate(rotation).add(joint);
            inverseCubeTransform.transformPosition(position);
            bent.add(new BendVertex(position, vertex.u(), vertex.v()));
        }
        averageWeight /= polygon.size();

        Vector3f normal = new Vector3f(sourceNormal)
                .rotate(new Quaternionf().slerp(fullRotation, averageWeight));
        inverseCubeNormalTransform.transform(normal).normalize();

        if (bent.size() == 4) {
            output.add(makeGeoQuad(bent.get(0), bent.get(1), bent.get(2), bent.get(3), normal, direction));
        }
        else {
            for (int i = 1; i + 1 < bent.size(); i++)
                output.add(makeGeoQuad(bent.get(0), bent.get(i), bent.get(i + 1), bent.get(i + 1),
                        normal, direction));
        }
    }

    private static GeoQuad makeGeoQuad(
            BendVertex a, BendVertex b, BendVertex c, BendVertex d,
            Vector3f normal, Direction direction) {
        return new GeoQuad(
                new GeoVertex[] {
                        new GeoVertex(a.position(), a.u(), a.v()),
                        new GeoVertex(b.position(), b.u(), b.v()),
                        new GeoVertex(c.position(), c.u(), c.v()),
                        new GeoVertex(d.position(), d.u(), d.v())
                }, new Vector3f(normal), direction);
    }

    private static float bendWeight(float y, float jointY) {
        float t = (jointY + AUTO_BLEND_RADIUS - y) / (AUTO_BLEND_RADIUS * 2F);
        t = Math.max(0F, Math.min(1F, t));
        return t * t * (3F - 2F * t);
    }

    private static void restoreArmorState(GeoArmorRenderer<?> renderer) {
        List<SavedBonePose> savedPoses = SAVED_ARMOR_POSES.remove(renderer);
        if (savedPoses != null)
            for (SavedBonePose pose : savedPoses)
                pose.restore();
        List<SavedBoneMesh> savedMeshes = SAVED_ARMOR_MESHES.remove(renderer);
        if (savedMeshes != null)
            for (SavedBoneMesh mesh : savedMeshes)
                mesh.restore();
    }

    private record BendVertex(Vector3f position, float u, float v) {}

    private static final class SavedBonePose {
        private final GeoBone bone;
        private final float x;
        private final float y;
        private final float z;

        private SavedBonePose(GeoBone bone) {
            this.bone = bone;
            this.x = bone.getRotX();
            this.y = bone.getRotY();
            this.z = bone.getRotZ();
        }

        private void restore() {
            this.bone.updateRotation(this.x, this.y, this.z);
        }
    }

    private static final class SavedBoneMesh {
        private final GeoBone bone;
        private final List<GeoCube> cubes;

        private SavedBoneMesh(GeoBone bone,
                              List<GeoCube> originalCubes) {
            this.bone = bone;
            this.cubes = List.copyOf(originalCubes);
        }

        private void restore() {
            synchronized (ACTIVE_ARMOR_MESHES) {
                ACTIVE_ARMOR_MESHES.remove(this.bone);
            }
            this.bone.getCubes().clear();
            this.bone.getCubes().addAll(this.cubes);
        }
    }

    /**
     * 强制清理所有残留的盔甲变形状态。
     * 在客户端登出/维度切换时调用，防止 static map 残留污染共享模型。
     */
    public static void clearAll() {
        synchronized (ACTIVE_ARMOR_MESHES) {
            // 还原所有被修改过的共享模型骨骼
            ACTIVE_ARMOR_MESHES.forEach((bone, originalCubes) -> {
                bone.getCubes().clear();
                bone.getCubes().addAll(originalCubes);
            });
            ACTIVE_ARMOR_MESHES.clear();
        }
        SAVED_ARMOR_POSES.clear();
        SAVED_ARMOR_MESHES.clear();
    }

    /**
     * 手动施加弯曲（用于绕过 GeoRenderEvent 的自定义渲染路径，如第一人称手臂）。
     * 必须与 {@link #applyBendManually} 配对调用 {@link #restoreManually}。
     */
    public static void applyBendManually(
            GeoArmorRenderer<?> renderer,
            BakedGeoModel model,
            net.minecraft.world.entity.Entity entity) {
        restoreArmorState(renderer);

        if (!(entity instanceof AbstractClientPlayer player)
                || !(player instanceof IAnimatedPlayer animatedPlayer))
            return;

        AnimationApplier animation =
                animatedPlayer.playerAnimator_getAnimation();
        if (!animation.isActive())
            return;

        if (!ownsGeoArmorBend(player))
            return;

        List<SavedBonePose> savedPoses = new java.util.ArrayList<>(4);
        List<SavedBoneMesh> savedMeshes = new java.util.ArrayList<>(4);
        Set<GeoBone> visited =
                Collections.newSetFromMap(new IdentityHashMap<>());

        // 第一人称只渲染手臂，只处理手臂部分
        if (!applyArmorBend(model, animation, "rightArm", savedPoses, visited,
                "rightforearm", "right_forearm", "rightForearm", "armorRightForearm",
                "rightLowerArm", "right_lower_arm"))
            applyAutomaticArmorBend(renderer, model, animation, "rightArm", savedMeshes,
                    "armorRightArm", "rightArm", "right_arm", "rightSleeve");
        if (!applyArmorBend(model, animation, "leftArm", savedPoses, visited,
                "leftforearm", "left_forearm", "leftForearm", "armorLeftForearm",
                "leftLowerArm", "left_lower_arm"))
            applyAutomaticArmorBend(renderer, model, animation, "leftArm", savedMeshes,
                    "armorLeftArm", "leftArm", "left_arm", "leftSleeve");

        if (!savedPoses.isEmpty())
            SAVED_ARMOR_POSES.put(renderer, savedPoses);
        if (!savedMeshes.isEmpty())
            SAVED_ARMOR_MESHES.put(renderer, savedMeshes);
    }

    /** 与 {@link #applyBendManually} 配对，渲染完成后还原。 */
    public static void restoreManually(
            GeoArmorRenderer<?> renderer) {
        restoreArmorState(renderer);
    }
}