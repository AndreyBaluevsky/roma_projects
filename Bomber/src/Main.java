public class Main {


    private static class Gr2024_12_15 extends Graphics {

        public GameBoard gb = createGameBoard();

        private GameBoard createGameBoard() {
            //final GameBoard gameBoard = new GameBoard(20, 20);


            GameBoard gameBoard;
            try {
                gameBoard = new GameBoard("Bomber\\res\\GameBoard.xml");

            } catch (Exception e) {
                //throw new RuntimeException(e);
                System.out.println(e.toString());
                gameBoard = null;
            }


            return gameBoard;



        }

        public Gr2024_12_15(String title) { super(title); }

        @Override
        public void drawFrame() {
            //drawGrid(5, 5, 0.1f, 0.1f);
            gb.draw(this);
        }

    }

    public static void main(String[] args) {
        new Gr2024_12_15("Проект 15.12.2024").run(Graphics.RunMode.FRAME_LOOP);
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
