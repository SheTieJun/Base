package me.shetj.compilers

import com.squareup.kotlinpoet.*
import me.shetj.compilers.ClassNameUtils.RxHttp
import me.shetj.compilers.ClassNameUtils.RxUtil
import java.util.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
open class FactoryCreatorProxy(
        private val elementUtils: Elements,
        private val classElement: TypeElement,
        private val name: String
) {

    private fun getPageName() = elementUtils.getPackageOf(classElement).qualifiedName.toString()
    private val fileName = "${classElement.simpleName.toString()}$name"

    /**
     * 创建文件
     */
    fun buildTo(): FileSpec {
        val funList = ArrayList<FunSpec>()
        //得到类的所有element(方法，类型等等)
        val enclosedElements = classElement.enclosedElements
        enclosedElements.forEach { executableElement ->
            //这些只处理方法
            if (executableElement is ExecutableElement) {
                val methodName = executableElement.simpleName.toString()
                executableElement.parameters
                val func = createFunSpec(methodName, executableElement)
                funList.add(func)
            }
        }

        return FileSpec.builder(getPageName()+".$name", fileName)  //创建文件
                .addType(createTypeSpec(funList)) //创建class
                .build()
    }

    private fun createTypeSpec(funList: ArrayList<FunSpec>): TypeSpec {
        return TypeSpec.objectBuilder(fileName)
                .addKdoc("这是APT自动生成的[$fileName]")
                .addFunction(addApiFunc(classElement.simpleName.toString()))
                .apply {
                    funList.forEach {
                        addFunction(it)
                    }
                }
                .addProperty(createPropertySpec())
                .build()
    }

    private fun createPropertySpec(): PropertySpec {
        return PropertySpec.builder("api", classElement.javaToKotlinType(), KModifier.PRIVATE)
                .mutable()
                .initializer("%T.getInstance().getApiManager(%T::class.java)", RxHttp,classElement.javaToKotlinType())
                .build()
    }

    /**
     * 添加可以修改api的方法
     */
    private fun addApiFunc(methodName: String): FunSpec {
        return FunSpec.builder("set$methodName")
                .addAnnotation(JvmStatic::class)
                .addParameter("reApi", classElement.javaToKotlinType())
                .addCode("api=reApi")
                .build()
    }

    private fun createFunSpec(methodName: String, executableElement: ExecutableElement): FunSpec {
        return FunSpec.builder(methodName)
                .apply {
                    //给方法添加参数
                    executableElement.parameters.forEach { vElement ->
                        this.addParameter(
                                vElement.simpleName.toString(),
                                vElement.javaToKotlinType()
                        )
                    }
                }
                .apply {
                    //添加实现代码
                    val para = StringJoiner(", ")
                    executableElement.parameters.forEach { varElement ->
                        para.add(varElement.simpleName.toString().getFixName())
                    }
                    val info = para.toString()
                    addCode("" +
                            "return api.${methodName}($info)\n" +
                            ".compose(%T.io_main())\n",
                            RxUtil
                    )
                }
                .addAnnotation(JvmStatic::class)
                .returns(executableElement.returnType.asTypeName().javaToKotlinType())
                .build()
    }


    private fun String.getFixName(): String {
        return when (this) {
            "object" -> "`$this`"
            else -> this
        }
    }
}