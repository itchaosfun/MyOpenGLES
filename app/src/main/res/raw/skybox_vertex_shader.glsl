uniform mat4 u_Matrix;
attribute vec3 a_Position;
varying vec3 v_Position;

void main()
{
    /**
    首先把顶点的位置传递给片段着色器，接着反转其z分量；
    这个传递给片段着色器的位置就是立方体上每个面之间将被插值的位置，这使我们以后可以使用这个位置查看天空盒的纹理上正确的部分。
    其z分量被反转了，使得我们可以把世界的右手坐标空间转换为天空盒所期望的左手坐标空间。
    */
    v_Position = a_Position;
    v_Position.z = -v_Position.z;

    /**
    通过用a_Position乘以矩阵把那个位置投影到剪裁空间坐标之后，需要下面的代码把其z分量设置为与其w分量相等的值
    这是一种技巧，它确保天空盒的每一部分都将位于归一化设备坐标的远平面上以及场景中的其他一切后面。
    原理：通过透视除法把一切都除以w，并且w除以它自己，结果等于1.透视除法之后，z最终就在值为1的元平面上了。
    */
    gl_Position = u_Matrix * vec4(a_Position,1.0);
    gl_Position = gl_Position.xyww;
}