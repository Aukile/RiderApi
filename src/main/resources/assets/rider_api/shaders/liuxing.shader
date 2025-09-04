Shader "Unlit/LiuXing"

{

  Properties

  {

    [Header(Color)]

    [Space(20)]



    _ShadowColor1("光照阴影颜色", Color) = (1.0,1.0,1.0,1.0)



    _ZhengTiColor("整体颜色", Color) = (0.2, 0.5, 0.6, 0.8)

    _ZhengTiColorInt("整体颜色强度", Range(0, 1)) = 0



    [Header(MainTex)]

    [Space(20)]



    [NoScaleOffset] _MainTex ("星云贴图", 2D) = "black" {}

    _MainTexSpeed("星云移动速度", Vector) = (0.01, 0.01, 0.0, 0.0)

    [Space(20)]

    _MainTexTiling("星云UV缩放", Vector) = (1.0, 1.0, 1.0, 1.0)

    [Space(20)]

    _MainTexPow("星云发光次幂", Range(0, 1)) = 0.5



    [Header(NormalTex)]

    [Space(20)]

    [Toggle(OpenNormal)]_OpenNormal("Normal",float)=0

    [NoScaleOffset] _NormalTex("法线贴图", 2D) = "bump"{}

    _NormalInt("法线强度", Float) = 1



    [Header(XingKongTex)]

    [Space(20)]



    [NoScaleOffset] _XingKong1 ("星星1贴图", 2D) = "black" {}

    _XingKong1Speed("星1移动速度", Vector) = (0.1, 0.1, 0.0, 0.0)

    [Space(20)]

    [NoScaleOffset] _XingKong2 ("星星2贴图", 2D) = "black" {}

    _XingKong2Speed("星2移动速度", Vector) = (0.0, 0.0, 0.0, 0.0)

    [Space(20)]

    _XingKongTling("星星UV缩放（两个同时）", Float) = 1

    _XingKongOffset("星星UV移动（两个同时）", Vector) = (0.0, 0.0, 0.0, 0.0)

    [Space(20)]

    _XingkongPow("星星高光次幂", Float) = 5

    _XingKongLiangDu("星星整体亮度", Float) = 100



    [Space(20)]

    [HDR] _XingKongColor3("星空颜色", Color) = (1.0, 0.5, 0.3, 0.2)



    [Space(20)]

    [Toggle(OpenColor2)]_OpenColor2("Color",float)=0

    [Space(20)]

    [HDR] _XingKongColor1("星空颜色1", Color) = (1.0, 0.5, 0.3, 0.2)

    [HDR] _XingKongColor2("星空颜色2", Color) = (1.0, 0.5, 0.3, 0.2)



    [Header(NoiseTex)]

    [Space(20)]



    [NoScaleOffset] _Noise ("噪声图", 2D) = "white"{}

    _NoiseSpeed("噪声图移动速度", Vector) = (0.1, 0.0, 0.0, 0.0)



    [Header(LiuXing)]

    [Space(20)]



    _UVSuoFang("流星UV缩放", Vector) = (2.0, 1.0, 0.0, 0.0)

    [Space(20)]

    _UVSuoFang1("流星UV移动", Vector) = (-0.5, -0.5, 0.0, 0.0)

    [Space(20)]

    _Tling("流星UV重复", Range(0, 10)) = 4

    _Roatae("流星旋转", Float) = 0

    _VniuquInt("流星扭曲强度", Float) = 0.15

    _VniuquV("流星V向扭曲", Range(-10, 10)) = 1

    _VniuquU("流星U向扭曲", Range(-10, 10)) = 1

    _LiuXingSpeed("流星速度", Range(0.5, 2)) = 0.5

    [Space(20)]

    _Tuowei("拖尾大小", Vector) = (3, 1, 0.0, 0.0)

    [Space(20)]

    _TuoweiYuHua("拖尾羽化", Range(0.0, 2.0)) = 0.5

    [Space(20)]

    _YuandianDaXiao("流星光点大小", Vector) = (6, 3, 0.0, 0.0)

    [Space(20)]

    _World_YPOW("流星显示区域遮罩", Range(0, 3)) = 1

    [HDR] _LiuXingColor("流星颜色", Color) = (1.0, 0.5, 0.3, 0.2)

    [Space(20)]

    _BlinnPhongPower("高光次幂", Range(0,10)) = 1



    [Header(Alpha)]

    [Space(20)]



    _AlphaTex("透明度贴图", 2D) = "white"{}

    _Clip("裁剪", Range(0, 1)) = 0.6

    [Toggle(OpenAlpha)]_OpenAlpha("Alpha",float)=0

    _Alpha("透明度", Range(0, 1)) = 1

  }

  SubShader

  {

    Tags { "Queue" = "Transparent"

        "RenderType"="Transparent"

			  "IgnoreProjector" = "True"

      }

    LOD 100



    Pass

    {

      Name "FORWARD"

      Tags {"LightMode" = "ForwardBase"}

      Blend One OneMinusSrcAlpha

      //ZWrite Off

      Cull Back



      CGPROGRAM

      #pragma vertex vert

      #pragma fragment frag





      #include "UnityCG.cginc"

      #include "Lighting.cginc"

      #include "AutoLight.cginc"



      #pragma multi_compile_fwdbase_fullshadows

      #pragma target 3.0



      struct appdata

      {

        float4 vertex : POSITION;

        float2 uv : TEXCOORD0;

        float3 normal : NORMAL;

        float4 tangent : TANGENT;

      };



      struct v2f

      {

        float2 uv : TEXCOORD0;

        float4 vertex : SV_POSITION;

        float3 world_normal : TEXCOORD1;

        float3 worldPos : TEXCOORD2;

        float3 tangentDir : TEXCOORD3;

        float3 binormalDir : TEXCOORD4;

        float3 view_world : TEXCOORD5;

        LIGHTING_COORDS(6, 7)

      };



      sampler2D _MainTex;

      sampler2D _XingKong1;

      sampler2D _XingKong2;

      sampler2D _Noise;

      sampler2D _NormalTex;

      sampler2D _AlphaTex;

      float4 _UVSuoFang;

      float4 _UVSuoFang1;

      float _Tling;

      float _Roatae;

      float _VniuquInt;

      float4 _Tuowei;

      float _TuoweiYuHua;

      float _World_YPOW;

      float4 _LiuXingColor;

      float4 _MainTexSpeed;

      float4 _MainTexTiling;

      float4 _XingKong1Speed;

      float4 _XingKong2Speed;

      float _XingKongTling;

      float4 _XingKongOffset;

      float _MainTexPow;

      float4 _NoiseSpeed;

      float _XingkongPow;

      float _XingKongLiangDu;

      float4 _XingKongColor1;

      float4 _XingKongColor2;

      float4 _YuandianDaXiao;

      float _NormalInt;

      float _BlinnPhongPower;

      float4 _ZhengTiColor;

      float _ZhengTiColorInt;

      float _Alpha;

      float4 _ShadowColor1;

      float _OpenNormal;

      float _OpenAlpha;

      float _OpenColor2;

      float _VniuquV;

      float _LiuXingSpeed;

      float _VniuquU;

      float _Clip;

      float4 _XingKongColor3;



      v2f vert (appdata v)

      {

        v2f o;

        o.vertex = UnityObjectToClipPos(v.vertex);

        o.world_normal = UnityObjectToWorldNormal(v.normal);

        o.worldPos = mul(unity_ObjectToWorld, v.vertex).xyz;

        o.tangentDir = normalize(mul(unity_ObjectToWorld, float4(v.tangent.xyz, 0.0)).xyz);

        float3 world = mul(unity_ObjectToWorld, v.vertex).xyz;

        o.binormalDir = normalize(cross(o.world_normal, o.tangentDir) * v.tangent.w);

        o.view_world = normalize(_WorldSpaceCameraPos.xyz - world);

        TRANSFER_SHADOW(o);

        o.uv = v.uv;

        return o;

      }



      fixed4 frag (v2f i) : SV_Target

      {

        //向量获取

        float3 viewDir = normalize(i.view_world);

        float3 nDir = normalize(i.world_normal);

        float3 tDir = normalize(i.tangentDir);

        float3 bDir = normalize(i.binormalDir);

        float3 worldPos = float3(i.worldPos);

        float3 lightDir = normalize(_WorldSpaceLightPos0.xyz);

        float3 vertexPos = mul( unity_WorldToObject, float4(worldPos, 1.0) );

        float3 ObjToView = UnityObjectToViewPos(float3(vertexPos)).xyz;

			  float3 objToView_One = UnityObjectToViewPos(float3(0.0, 0.0, 0.0)).xyz;

        float2 vSubT = float2(( ObjToView - objToView_One ).xy);



        //TBN矩阵

        if(_OpenNormal)

        {

          float4 normaltex = tex2D(_NormalTex, i.uv);

          float3 normaldata = UnpackNormal(normaltex);

          float3x3 TBN = float3x3(tDir, bDir, nDir);

          nDir = normalize(mul(normaldata, TBN));

        }



        //光照模型

        float3 hDir = normalize(viewDir + lightDir);

        float3 ndir = float3(nDir.x * _NormalInt, nDir.y * _NormalInt, nDir.z);

        float nDoth = saturate(dot(ndir, hDir));

        float blinnphong = pow(max(0.0, nDoth), _BlinnPhongPower * 30);

        float nDotl = dot(ndir, lightDir) * 0.5 + 0.5;



        //向量计算

        float Yzhezhao = 1 - pow(abs(nDir.y), _World_YPOW);



        //UV旋转

        float2 uv_R = i.uv - float2(0.5, 0.5);

        float cos_rotate = cos(_Roatae * 0.01745329);

        float sin_rotate = sin(_Roatae * 0.01745329);

        float add_uv = uv_R.x * sin_rotate + uv_R.y * cos_rotate;

        float sub_uv = uv_R.x * cos_rotate - uv_R.y * sin_rotate;

        float2 UV = float2(add_uv, sub_uv);



        //UV获取

        float2 uv1 =UV.xy * _UVSuoFang.xy * _Tling;

        float2 uv2 = float2(uv1.x, uv1.y + _Time.y * _LiuXingSpeed);

        float2 uv3 = frac(uv2) + _UVSuoFang1;

        float2 texUV = (vSubT + (_MainTexSpeed.xy * _Time.y)) * _MainTexTiling.xy;

        float2 xingkong1UV = (i.uv + (_XingKong1Speed.xy * _Time.y)) * _XingKongTling + _XingKongOffset.xy;

        float2 xingkong2UV = (i.uv + (_XingKong2Speed.xy * _Time.y)) * _XingKongTling + _XingKongOffset.xy;



        //星空贴图

        float noise = tex2D(_Noise, (i.uv + (_NoiseSpeed * _Time.y)));

        float tex = tex2D(_MainTex, texUV).r * _MainTexPow;

        float xingkong1 = tex2D(_XingKong1, xingkong1UV).r;

        float xingkong2 = tex2D(_XingKong2, xingkong2UV).r;

        float xingkong = max(pow((tex + xingkong1 + xingkong2), _XingkongPow), 0.0);

        float xingkongZheZhao = noise * xingkong * _XingKongLiangDu;



        //运动速度随机值

        float2 suiji = dot(floor(uv2), float2( 12.9898,78.233 ));

        float suiji1 = lerp( 0.0 , 1.0 , frac( ( sin( suiji ) * 43758.55 ) ));

        float Vspeed = cos(_Time.y + suiji1 * 3564.156) * -0.4;

        float Uspeed = tan(i.uv.y * _VniuquV * _VniuquU) * _VniuquInt;

        float u = (uv3.x - Uspeed);

        float v = (uv3.y - Vspeed);

        float2 uv4 = float2(u, v);



        //构成圆点

        float yuandian = smoothstep(_YuandianDaXiao.x * 0.01, _YuandianDaXiao.y * 0.01, length(uv4 / _UVSuoFang.xy));



        //拖尾

        float uZheZhao = smoothstep(_Tuowei.x * 0.01, _Tuowei.y * 0.01, abs(uv4.x));

        float VZheZhao = smoothstep(-0.02, 0.0, uv4.y);

        float smooth = smoothstep(0.5, -0.2, uv3.y);

        float tuowei = (uZheZhao * VZheZhao) * smooth * _TuoweiYuHua;



        //星空输出

        float3 xingkongColor = _XingKongColor3;

        if(_OpenColor2)

        {

        xingkongColor = lerp(_XingKongColor1, _XingKongColor2, vSubT.y);

        }

        float3 xingColor = (xingkongZheZhao + tex + xingkong1 + xingkong2) * xingkongColor;



        //流星

        float3 liuxing = (tuowei + yuandian) * _LiuXingColor * Yzhezhao;



        //总输出



        fixed shadow =SHADOW_ATTENUATION(i);

        float alphatex = tex2D(_AlphaTex, i.uv).a;

        clip(alphatex - _Clip);

        float Op = 1.0;

        if(_OpenAlpha)

        {

        Op = (xingkongZheZhao * (tuowei + yuandian)) + _Alpha;

        }

        float3 zhengtiColor = _ZhengTiColor * _ZhengTiColorInt;

        float3 col = (xingColor + liuxing) * nDotl + blinnphong + zhengtiColor;

        float3 Shadow = lerp(_ShadowColor1 * _LightColor0, col, shadow);





        return float4(Shadow * Op,Op);

      }

      ENDCG

    }

  }