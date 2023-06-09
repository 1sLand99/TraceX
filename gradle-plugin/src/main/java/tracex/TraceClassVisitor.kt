package tracex

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class TraceClassVisitor(
    api: Int,
    next: ClassVisitor,
) : ClassVisitor(api, next) {

    private var className: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        className = name?.replace('/', '.')
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        return TraceMethodVisitor(api, mv, access, name, descriptor)
    }

    private inner class TraceMethodVisitor(
        api: Int,
        methodVisitor: MethodVisitor?,
        access: Int,
        name: String?,
        descriptor: String?
    ) : AdviceAdapter(
        api,
        methodVisitor,
        access,
        name,
        descriptor
    ) {

        override fun onMethodEnter() {
            mv.visitLdcInsn("$className#$name")
            mv.visitMethodInsn(
                INVOKESTATIC,
                "android/os/Trace",
                "beginSection",
                "(Ljava/lang/String;)V",
                false
            )
        }

        override fun onMethodExit(opcode: Int) {
            mv.visitMethodInsn(
                INVOKESTATIC,
                "android/os/Trace",
                "endSection",
                "()V",
                false
            )
        }

    }
}