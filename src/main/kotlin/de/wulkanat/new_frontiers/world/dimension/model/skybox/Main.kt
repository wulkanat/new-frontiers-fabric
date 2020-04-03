package de.wulkanat.new_frontiers.world.dimension.model.skybox

import de.wulkanat.new_frontiers.world.dimension.model.skybox.opengl.init
import de.wulkanat.new_frontiers.world.dimension.model.skybox.opengl.loop
import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*

fun main() {
    println("Hello LWJGL ${Version.getVersion()}!")

    val window = init()
    loop(window)

    glfwFreeCallbacks(window)
    glfwDestroyWindow(window)

    glfwTerminate()
    glfwSetErrorCallback(null)?.free()
}