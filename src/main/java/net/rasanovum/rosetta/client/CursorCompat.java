package net.rasanovum.rosetta.client;

import java.nio.ByteBuffer;
//? if >=26.3 {
/*import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLPixels;
import org.lwjgl.sdl.SDLSurface;
*///?} else {
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
//?}

/** Native RGBA cursors. Create, select, and destroy on the client thread. */
public final class CursorCompat {
    private CursorCompat() {}

    public static long create(int width, int height, ByteBuffer pixels, int hotspotX, int hotspotY) {
        //? if >=26.3 {
        /*var surface = SDLSurface.SDL_CreateSurfaceFrom(width, height, SDLPixels.SDL_PIXELFORMAT_RGBA32, pixels, width * 4);
        if (surface == null) return 0;
        try {
            return SDLMouse.SDL_CreateColorCursor(surface, hotspotX, hotspotY);
        } finally {
            SDLSurface.SDL_DestroySurface(surface);
        }
        *///?} else {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            //? if <1.19 {
            /*GLFWImage image = GLFWImage.mallocStack(stack).width(width).height(height).pixels(pixels);
            *///?} else {
            GLFWImage image = GLFWImage.malloc(stack).width(width).height(height).pixels(pixels);
            //?}
            return GLFW.glfwCreateCursor(image, hotspotX, hotspotY);
        }
        //?}
    }

    public static void select(long window, long cursor) {
        //? if >=26.3 {
        /*SDLMouse.SDL_SetCursor(cursor == 0 ? SDLMouse.SDL_GetDefaultCursor() : cursor);
        *///?} else {
        GLFW.glfwSetCursor(window, cursor);
        //?}
    }

    public static void destroy(long cursor) {
        //? if >=26.3 {
        /*SDLMouse.SDL_DestroyCursor(cursor);
        *///?} else {
        GLFW.glfwDestroyCursor(cursor);
        //?}
    }
}
