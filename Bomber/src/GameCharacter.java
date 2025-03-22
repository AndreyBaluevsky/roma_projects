public class GameCharacter {
    protected int X, Y;
    protected Cell cell;
    protected GameBoard parentGameBoard;

    public GameCharacter(GameBoard gb, int X, int Y, String bmpId) throws Exception {
        this.parentGameBoard = gb;
        cell = gb.setCellsXY(X, Y, new BitmapCell(bmpId));
        if(moveTo(X,Y)==false) throw new Exception("Персонаж вне игрового поля!");
    }

    public boolean moveTo(int newX, int newY) {
        if( newY>=1 && newY<parentGameBoard.getCellRows()-1 &&
            newX>=1 && newX<parentGameBoard.getCellColumns()-1) {
            parentGameBoard.setCellsXY(X, Y,null);
            X = newX; Y = newY;
            parentGameBoard.setCellsXY(X, Y, cell);
            return  true;
        }
        return  false;
    }

    public boolean moveRel(int newX, int newY) {
        return moveTo(X+newX, Y+newY);
    }

    public static class Bomber extends GameCharacter {
        public Bomber(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "pic_v3");
        }
    }
}
