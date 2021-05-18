#define PI 3.1415926538

#flwbuiltins
#flwinclude <"create:core/quaternion.glsl">
#flwinclude <"create:core/matutils.glsl">
#flwinclude <"create:core/diffuse.glsl">

#[InstanceData]
struct Belt {
    vec2 light;
    vec4 color;
    vec3 pos;
    float speed;
    float offset;
    vec4 rotation;
    vec2 sourceTexture;
    vec4 scrollTexture;
    float scrollMult;
};

#flwinclude <"create:data/modelvertex.glsl">
#flwinclude <"create:data/blockfragment.glsl">

BlockFrag FLWMain(Vertex v, Belt instance) {
    vec3 rotated = rotateVertexByQuat(v.pos - .5, instance.rotation) + instance.pos + .5;

    vec4 worldPos = vec4(rotated, 1.);

    vec3 norm = rotateVertexByQuat(v.normal, instance.rotation);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    float scrollSize = instance.scrollTexture.w - instance.scrollTexture.y;
    float scroll = fract(instance.speed * uTime / (31.5 * 16.) + instance.offset) * scrollSize * instance.scrollMult;

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords - instance.sourceTexture + instance.scrollTexture.xy + vec2(0, scroll);
    b.light = instance.light;

    #if defined(RAINBOW_DEBUG)
    b.color = instance.color;
    #else
    b.color = vec4(1.);
    #endif

    return b;
}
