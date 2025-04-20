public class GameCharacter {
    protected int X, Y;
    protected Cell cell;
    protected GameBoard parentGameBoard;

    public GameCharacter(GameBoard gb, int X, int Y, String bmpId) throws Exception {
        this.parentGameBoard = gb;
        this.X = X;
        this.Y = Y;
        cell = gb.setCellsXY(X, Y, new BitmapCell(bmpId));
        //if(moveTo(X,Y)==false) throw new Exception("Персонаж вне игрового поля!");
    }

    public boolean moveTo(int newX, int newY) {
        boolean bAboveLowerBound = newY >= 0;
        boolean bBelowUpperBound = newY < parentGameBoard.getCellRows() - 1;
        boolean bRightToLeftBorder = newX >= 1;
        boolean bLeftToRightBorder = newX < parentGameBoard.getCellColumns() - 1;

        boolean bInsideGameBoard = bAboveLowerBound && bBelowUpperBound &&
                bRightToLeftBorder && bLeftToRightBorder;

        if(bInsideGameBoard) {
            boolean EmptyCell = isEmptyCell(newX, newY);
            if(EmptyCell) {
                parentGameBoard.setCellsXY(X, Y,null);
                X = newX; Y = newY;
                parentGameBoard.setCellsXY(X, Y, cell);
                return  true;
            }
        }
        return  false;
    }

    private boolean isEmptyCell(int X, int Y) {
        Cell cl = parentGameBoard.getCellsXY(X, Y);
        boolean EmptyCell = cl == null || cl instanceof EmptyCell;
        return EmptyCell;
    }

    public boolean moveRel(int newX, int newY) {
        return moveTo(X+newX, Y+newY);
    }

    public boolean вверх() {
        return moveRel(0, 1);
    }

    public boolean вниз() {
        return moveRel(0, -1);
    }

    public boolean влево() {
        return moveRel(-1, 0);
    }

    public boolean вправо() {
        return moveRel(1, 0);
    }

    public boolean сверху_свободно() {
        return isEmptyCell(X, Y+1);
    }
    public boolean снизу_свободно() {
        return isEmptyCell(X, Y-1);
    }
    public boolean слева_свободно() {
        return isEmptyCell(X-1, Y);
    }
    public boolean справа_свободно() {
        return isEmptyCell(X+1, Y);
    }

    public void timeTick() {
        int d = (int)(Math.random()*100)%4;
        switch (d) {
            case 0:       вверх(); break;
            case 1:       вниз(); break;
            case 2:       влево(); break;
            case 3:       вправо(); break;
        }


        /*
        нц_пока(не(снизу_свободно()), {
            вправо()
        }).then({

        })*/
        /*
алг Робот2
нач
нц пока справа свободно
вправо
кц
нц
нц пока справа не свободно
вверх
кц
вправо
нц пока снизу не свободно
вправо
кц
вниз
нц пока (слева не свободно) и (снизу свободно)
вниз
кц
нц пока слева свободно
влево
кц
нц пока (снизу свободно) и (слева не свободно)
вниз
кц
влево
нц пока (слева свободно) и (сверху не свободно)
влево
кц
вверх
нц пока справа не свободно
вверх
кц
вправо
нц пока (справа свободно) и (снизу не свободно)
вправо
кц
кц
кон

         */
    }


    public static class Bomber extends GameCharacter {
        public Bomber(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "bomber");
        }
    }
    public static class Robot1 extends GameCharacter {
        public Robot1(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "robot1");
        }
    }
    public static class Robot2 extends GameCharacter {
        public Robot2(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "robot2");
        }
    }
}
