import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.awt.*;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Graphics {

    public enum  RunMode {
        SINGLE_FRAME,
        FRAME_LOOP
    }

    protected long window;
    protected int windowWidth = 800;  // Default width
    protected int windowHeight = 600; // Default height
    protected String title;
    public Graphics(String title) {
        this.title = title;
    }

    protected void beforeRun() { }
    protected void afterRun()  { }

    public void run(RunMode runMode) {
        System.out.println("Запуск LWJGL " + Version.getVersion() + "!");

        init();
        beforeRun();
        switch (runMode) {
            case SINGLE_FRAME:
                runSingleFrame();
                break;
            case FRAME_LOOP:
                runFrameLoop();
                break;
        }
        afterRun();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));
        if (!glfwInit())
            throw new IllegalStateException("Ошибка при подготовке к запуску!");

        window = glfwCreateWindow(windowWidth, windowHeight, title, NULL, NULL);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        updateViewport();

        // Set the clear color
        glClearColor(1f, 1f, 1f, 1f);

        setWindowCallback();
        glfwShowWindow(window);
    }

    private void updateViewport() {
        int m = Math.min(windowWidth, windowHeight);
        glViewport( (windowWidth-m)/2,
                    (windowHeight-m)/2, m, m);
    }

    public void setWindowCallback() {
        glfwSetWindowSizeCallback(window, (window, width, height) -> {
            windowWidth = width;
            windowHeight = height;
            updateViewport();
        });
    }

    public void drawFrame() {

    }

    public void runSingleFrame() {
        glClear(GL_COLOR_BUFFER_BIT);
        drawFrame();
        glfwSwapBuffers(window);
        while (!glfwWindowShouldClose(window)) {
            glfwPollEvents();
        }
    }
    public void runFrameLoop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);
            drawFrame();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    public static void drawBitmap(double px, double ph, Color[][] bmp, int rotation) {
        int M = bmp.length, N = bmp[0].length;
        glPushMatrix();
        //glScalef(2, 2, 1);
        rotation &=3;
        switch (rotation) {
            case 1:
                glTranslatef(1, 0, 0);
                glRotatef(90,0,0, 1);
                break;
            case 2:
                glTranslatef(1, 1, 0);
                glRotatef(180,0,0, 1);
                break;
            case 3:
                glTranslatef(0, 1, 0);
                glRotatef(270,0,0, 1);
                break;
        }
        glBegin(GL_QUADS);
        //glLineWidth(2.0f);
        drawPixels(px, ph, bmp, M, N);
        glEnd();
        glPopMatrix();
    }

    private static void drawPixels(double px, double ph, Color[][] bmp, int m, int n) {
        double y1 = 0;
        for (int i = 0; i< m; i++) {
            double y2 =y1 + ph;
            double x1 = 0;
            final Color[] curRow = bmp[i];
            for (int j = 0; j< n; j++) {
                double x2 = x1+px;
                Color pix = curRow[j];
                glColor3f(pix.getRed()/255f, pix.getGreen()/255f, pix.getBlue()/255f);
                putRect((float)x1, (float)y1, (float)x2,  (float)y2);
                x1 = x2;
            }
            y1 = y2;
        }
    }

    public static void putRect(float x1, float y1, float x2, float y2) {
        glVertex2f(x1, y1);
        glVertex2f(x1, y2);
        glVertex2f(x2, y2);
        glVertex2f(x2, y1);
    }

    public static void putColor(Color clr) {
        glColor3f(clr.getRed() / 255f, clr.getGreen() / 255f, clr.getBlue() / 255f);
    }


    public static void drawRound(float cx, float cy, float radius,
                           int N, double startAngle,
                           float BR, float BG, float BB,
                           float LR, float LG, float LB,
                           float lineWidth) {
        glPushMatrix();
        glTranslatef(cx, cy, 0);
        glScalef(radius, radius, 1);
        glBegin(GL_TRIANGLE_FAN);
        glColor3f(BR, BG, BB);
        putNPolyPoints(N, startAngle);
        glEnd();
        glBegin(GL_LINE_LOOP);
        glColor3f(LR, LG, LB);
        glLineWidth(lineWidth);
        putNPolyPoints(N, startAngle);
        glEnd();
        glPopMatrix();
    }

    public static void drawNPoly(float cx, float cy, float radius,
                           int N, double startAngle) {
        glPushMatrix();
        glTranslatef(cx, cy, 0);
        glScalef(radius, radius, 1);
        glBegin(GL_LINE_LOOP);
        putNPolyPoints(N, startAngle);
        glEnd();
        glPopMatrix();
    }

    public static void drawNPolyFilled(float cx, float cy, float radius,
                                 int N, double startAngle) {
        glPushMatrix();
        glTranslatef(cx, cy, 0);
        glScalef(radius, radius, 1);
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(0, 0);
        putNPolyPoints(N, startAngle);
        glEnd();
        glPopMatrix();
    }

    public static void putNPolyPoints(int N, double startAngle) {
        double angle = startAngle, da =  2 * Math.PI  / N;
        for(int i = 0; i <= N; i++){
            float pX = (float)Math.cos(angle);
            float pY = (float)Math.sin(angle);
            glVertex2f(pX, pY);
            angle += da;
        }
    }

    public static void drawTriangles_GL_TRIANGLE_FAN() {
        glBegin(GL_TRIANGLE_FAN);
        glColor3f(1f, 0.5f, 0.25f); // оранжевый
        glVertex2f(0f, 0f);
        glVertex2f(-0.5f, -0.25f);
        glVertex2f(0.5f, -0.25f);
        glVertex2f(-0.5f, 0.25f);
        glVertex2f(0.5f, 0.25f);
        glEnd();
    }

    public static void drawTriangles_GL_TRIANGLE_STRIP() {
        glBegin(GL_TRIANGLE_STRIP);
        glColor3f(1f, 0.5f, 0.25f); // оранжевый
        glVertex2f(-0.5f, -0.25f);
        glVertex2f(0.5f, -0.25f);
        glColor3f(1f, 0f, 0.5f); // розовый
        glVertex2f(0f, 0f);
        glVertex2f(-0.5f, 0.25f);
        glColor3f(1f, 1f, 0f); // жёлтый
        glVertex2f(0.5f, 0.25f);
        glEnd();
    }



    public static void drawTriangles_GL_TRIANGLES() {
        glBegin(GL_TRIANGLES);
        glColor3f(1f, 0.5f, 0.25f); // оранжевый
        glVertex2f(-0.5f, -0.25f);
        glVertex2f(0f, 0f);
        glVertex2f(-0.5f, 0.25f);
        glColor3f(1f, 0f, 0.5f); // розовый
        glVertex2f(0.5f, -0.25f);
        glVertex2f(0f, 0f);
        glVertex2f(0.5f, 0.25f);
        glEnd();
    }

    public static void drawCenteredRectangle() {
        // Задаём габариты
        float rectWidth = 1.0f; // Ширина
        float rectHeight = 0.5f; // Высота

        // Задаём границы прямоугольника
        // Центрирование по горизонтали
        float left = -rectWidth / 2;
        float right = rectWidth / 2;
        // Центрирование по вертикали
        float bottom = -rectHeight / 2;
        float top = rectHeight / 2;

        glPointSize(10.0f);
        glBegin(GL_QUADS);

        glLineWidth(2.0f);
        glColor3f(0f, 0f, 1f); // Синий цвет (R=0, G=0, B=1)
        glVertex2f(left, bottom);
        //glColor3f(1.0f, 1.0f, 1.0f);
        glVertex2f(right, bottom);
        //glColor3f(0.0f, 1.0f, 0.0f);
        glVertex2f(right, top);
        //glColor3f(1.0f, 0.0f, 0.0f);
        glVertex2f(left, top);
        glEnd();
    }

    public static void drawDiagonalLines() {
        // Первый отрезок
        glLineWidth(2.0f);
        glColor3f(1f, 0f, 0f); // Красный цвет (R=1, B=G=0)
        glBegin(GL_LINES);
        glVertex2f(-0.5f, -0.25f);
        glVertex2f(0.5f, 0.25f);
        glEnd();

        // Второй отрезок
        glLineWidth(4.0f);
        glColor3f(0f, 1f, 0f); // Зелёный цвет (R=0, G=1, B=0)
        glBegin(GL_LINES);
        glVertex2f(-0.5f, 0.25f);
        glVertex2f(0.5f, -0.25f);
        glEnd();
    }


}
