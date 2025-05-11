import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;


public class Main {


    public static class GameRunner extends Graphics {
        private static GameCharacter.Robot[] robotsList;
        public static ArrayList<GameCharacter.Bomb> bombsList = new ArrayList<>();
        GameCharacter.Bomber bomber;

        public GameBoard gb = createGameBoard();

        public static GameCharacter.Robot1 rbt1;
        public static GameCharacter.Robot2 rbt2;
        public static GameCharacter.Robot3 rbt3;
        public static GameCharacter.Robot4 rbt4;
        public static GameCharacter.Robot5 rbt5;
        public static GameCharacter.Robot6 rbt6;
        public static GameCharacter.Robot7 rbt7;

        public static GameCharacter.Robot findBot(int X, int Y) {
            for(GameCharacter.Robot r: robotsList) {
                if(r.X==X && r.Y == Y) return r;
            }
            return null;
        }

        private GameBoard createGameBoard() {
//            final GameBoard gameBoard = new GameBoard(20, 20);
            GameBoard gameBoard = null;
            try {
                gameBoard = new GameBoard("Bomber\\res\\GameBoard.xml");
                bomber = new GameCharacter.Bomber(gameBoard, 1, 1);
                rbt1 = new GameCharacter.Robot1(gameBoard, 16, 10);
                rbt2 = new GameCharacter.Robot2(gameBoard, 5, 8);
                rbt3 = new GameCharacter.Robot3(gameBoard, 18, 18);
                rbt4 = new GameCharacter.Robot4(gameBoard, 21, 25);
                rbt5 = new GameCharacter.Robot5(gameBoard, 24, 23);
                rbt6 = new GameCharacter.Robot6(gameBoard, 26, 16);
                rbt7 = new GameCharacter.Robot7(gameBoard, 24, 21);
                robotsList = new GameCharacter.Robot[]{rbt1, rbt2, rbt3, rbt4, rbt5, rbt6, rbt7};
            } catch (Exception e) {
                //throw new RuntimeException(e);
                System.out.println(e.toString());
            }
            return gameBoard;
        }

        public GameRunner(String title) { super(title); }

        public int frameRateRel = 8;
        public int frameRateRelCur = 0;

        @Override
        public void drawFrame() {
            //drawGrid(5, 5, 0.1f, 0.1f);
            if(frameRateRelCur>= frameRateRel) {
                for(GameCharacter.Robot r: robotsList) {
                    r.timeTick();
                }
                // Use manual counter to overcome:
                // Exception in thread "main" java.util.ConcurrentModificationException
                //	at java.base/java.util.ArrayList$Itr.checkForComodification(ArrayList.java:1042)
                //	at java.base/java.util.ArrayList$Itr.next(ArrayList.java:996)
                for (int i = bombsList.size()-1; i >= 0; i--) {
                    bombsList.get(i).timeTick();
                }
                frameRateRelCur = 0;
            } else {
                frameRateRelCur ++;
            }
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
                            case GLFW_KEY_UP: case GLFW_KEY_W:
                                //bomber.moveRel(0, 1);
                                bomber.вверх();
                                break;
                            case GLFW_KEY_DOWN: case GLFW_KEY_S:
                                bomber.вниз();
                                break;
                            case GLFW_KEY_LEFT: case GLFW_KEY_A:
                                bomber.влево();
                                break;
                            case GLFW_KEY_RIGHT: case GLFW_KEY_D:
                                bomber.вправо();
                                break;
                            case GLFW_KEY_SPACE:
                                bomber.nextType = GameCharacter.NextType.Bomb;
                                break;
                            default:
                                System.out.println("PRESS key: "+key+" scancode: "+scancode);
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
