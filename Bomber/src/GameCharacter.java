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

    private boolean isEmptyCell(int newX, int newY) {
        final Cell cl = parentGameBoard.getCellsXY(newX, newY);
        return cl == null || cl instanceof EmptyCell;
    }

    public boolean moveRel(int newX, int newY) {
        return moveTo(X+newX, Y+newY);
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
