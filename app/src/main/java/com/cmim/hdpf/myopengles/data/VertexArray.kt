package com.cmim.hdpf.myopengles.data

import android.opengl.GLES20.*
import com.cmim.hdpf.myopengles.BYTES_PER_FLOAT
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class VertexArray {
    //本地代码中存储顶点矩阵数据
    private lateinit var floatBuffer: FloatBuffer

    constructor(vertexData: FloatArray) {
        /**
         * 使用 ByteBuffer.allocateDirect() 分配了一块本地内存，这块内存不会被垃圾回收器管理。这个方法需要知道要分配多少字节的内存块；
         * 因为顶点都存储在一个浮点数组里，并且每个浮点数有4个字节，所以这款内存的大小应该是 tableVerticesWithTriangles.size * BYTES_PER_FLOAT
         * 下一行告诉字节缓冲区(byte buffer),按照本地字节序(native byte order)组织它的内容；
         * 本地字节序是指，当一个值占用多少个字节时，字节按照从最重要位到最不重要位或者相反顺序排列；
         * 怎么排序的无关紧要，重要的是作为一个平台要使用同样的排序；调用 order(ByteOrder.nativeOrder()) 可以保证这一点
         * 最后，我们调用 asFloatBuffer() 得到一个可以反映底层字节的 FloatBuffer 类实例；然后就可以赋值给 vertexData，
         * 这就把数据从 Android虚拟机的内存复制到本地内存了。当本地进程结束的时候，这块内存会被释放掉。
         */
        this.floatBuffer = ByteBuffer.allocateDirect(vertexData.size * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexData)
    }

    fun setVertexAttribPointer(dataOffset:Int,attributeLocation:Int,componentCount:Int,stride:Int){
        floatBuffer.position(dataOffset)
        /**
         * index 属性的位置
         * size 每个属性的数据的技术，上述我们每个顶点使用两个浮点数用来表达位置的一个x坐标和一个y坐标
         *      我们为每个顶点只传递两个分量，但是在着色器中，a_Position被定义为vec4，它有4个分量。
         *      如果一个分量没有被指定值，默认情况下，OpenGL会把前3个分量设为0，最后一个分量设为1
         * type 数据类型。我们上述定义的为浮点数的类型
         * normalized 只有使用整型数据的时候，才有意义
         * stride 只有当一个数组存储多于一个属性时，才有意义
         * ptr 告诉OpenGL去哪里读取数据。它会从缓冲区的当前位置读取，如果我们没有调用 vertexData?.position(0)，
         *      它可能尝试读取缓冲区结尾后面的内容，并使我们的应用程序崩溃。
         *
         * 传递不正确的参数给 glVertexAttribPointer 会导致奇怪的结果，甚至导致程序崩溃
         * glVertexAttribPointer(int indx,int size,int type,boolean normalized,int stride,java.nio.Buffer ptr)
         */
        glVertexAttribPointer(attributeLocation,componentCount,GL_FLOAT,false,stride,floatBuffer)

        glEnableVertexAttribArray(attributeLocation)

        floatBuffer.position(0)
    }
}