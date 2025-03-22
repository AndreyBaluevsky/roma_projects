import org.lwjgl.glfw.GLFWKeyCallback;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main {


    private static class GameRunner extends Graphics {
        GameCharacter.Bomber bomber;

        public GameBoard gb = createGameBoard();

        private GameBoard createGameBoard() {
//            final GameBoard gameBoard = new GameBoard(20, 20);
            GameBoard gameBoard = null;
            try {
                gameBoard = new GameBoard("Bomber\\res\\GameBoard.xml");
                bomber = new GameCharacter.Bomber(gameBoard, 3, 6);
            } catch (Exception e) {
                //throw new RuntimeException(e);
                System.out.println(e.toString());
            }
            return gameBoard;
        }

        public GameRunner(String title) { super(title); }

        @Override
        public void drawFrame() {
            //drawGrid(5, 5, 0.1f, 0.1f);
            gb.draw(this);
        }

        private GLFWKeyCallback keyCallback;

        @Override
        protected void beforeRun() {
            activateKeyHandler();
        }
        private void activateKeyHandler() {
            keyCallback = new GLFWKeyCallback() {
                @Override
                public void invoke(long window, int key, int scancode, int action, int mods) {
                    if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                        switch (key) {
                            case GLFW_KEY_UP:
                                bomber.moveRel(0, 1);
                                break;
                            case GLFW_KEY_DOWN:
                                bomber.moveRel(0, -1);
                                break;
                            case GLFW_KEY_LEFT:
                                bomber.moveRel(-1, 0);
                                break;
                            case GLFW_KEY_RIGHT:
                                bomber.moveRel(1, 0);
                                break;
                        }
                    }
                }
            };
            glfwSetKeyCallback(window, keyCallback);
        }

        private void testKeys() {
            keyCallback = new GLFWKeyCallback() {
                @Override
                public void invoke(long window, int key, int scancode, int action, int mods) {
                    if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                        switch (key) {
                            case GLFW_KEY_UP:
                                System.out.println("UP");
                                break;
                            case GLFW_KEY_DOWN:
                                System.out.println("DOWN");
                                break;
                            case GLFW_KEY_LEFT:
                                System.out.println("LEFT");
                                break;
                            case GLFW_KEY_RIGHT:
                                System.out.println("RIGHT");
                                break;
                        }
                    }
                }
            };
            glfwSetKeyCallback(window, keyCallback);
        }

        /* // Образец смены и отключения keyCallback.
           // Вручную отключать НЕ НАДО!
        @Override
        protected void afterRun() {
            GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
                @Override
                public void invoke(long window, int key, int scancode, int action, int mods) {
                    // Key handling logic
                }
            };
            glfwSetKeyCallback(window, keyCallback);
            // To explicitly clean up the callback (optional)
            keyCallback.close();
        }
        */
    }

    public static void main(String[] args) {
        new GameRunner("Bomber").run(Graphics.RunMode.FRAME_LOOP);
    }

    //
//    private void init() {
//        // Setup an error callback. The default implementation
//        // will print the error message in System.err.
//        GLFWErrorCallback.createPrint(System.err).set();
//
//        // Initialize GLFW. Most GLFW functions will not work before doing this.
//        if ( !glfwInit() )
//            throw new IllegalStateException("Ошибка при подготовке к запуску!");
//
//        // Configure GLFW
//        glfwDefaultWindowHints(); // optional, the current window hints are already the default
//        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
//        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
//
//        // Create the window
//        window = glfwCreateWindow(windowWidth, windowHeight, "08.12.2024", NULL, NULL);
//        if ( window == NULL )
//            throw new RuntimeException("Failed to create the GLFW window");
//
//        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
//        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
//            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
//                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
//        });
//
//        // Get the thread stack and push a new frame
//        try ( MemoryStack stack = stackPush() ) {
//            IntBuffer pWidth = stack.mallocInt(1); // int*
//            IntBuffer pHeight = stack.mallocInt(1); // int*
//
//            // Get the window size passed to glfwCreateWindow
//            glfwGetWindowSize(window, pWidth, pHeight);
//
//            // Get the resolution of the primary monitor
//            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
//
//            // Center the window
//            glfwSetWindowPos(
//                    window,
//                    (vidmode.width() - pWidth.get(0)) / 2,
//                    (vidmode.height() - pHeight.get(0)) / 2
//            );
//        } // the stack frame is popped automatically
//
//        // Make the OpenGL context current
//        glfwMakeContextCurrent(window);
//        // Enable v-sync
//        glfwSwapInterval(1);
//
//        // Make the window visible
//        glfwShowWindow(window);
//    }


}
