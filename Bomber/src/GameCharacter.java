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
    private boolean isWallOrBrickCell(int X, int Y) {
        Cell cl = parentGameBoard.getCellsXY(X, Y);
        return cl != null && (
                (cl instanceof WallCell) || (cl instanceof BrickCell));
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
    public boolean сверху_стена() {
        return isWallOrBrickCell(X, Y+1);
    }
    public boolean снизу_стена() {
        return isWallOrBrickCell(X, Y-1);
    }
    public boolean слева_стена() {
        return isWallOrBrickCell(X-1, Y);
    }
    public boolean справа_стена() {
        return isWallOrBrickCell(X+1, Y);
    }

    public void timeTick() {


    }




    public static class Bomber extends GameCharacter {
        public Bomber(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "bomber");
        }

        private boolean tryPushRobot(int newX, int newY, int newX2, int newY2) {
            Cell cl = parentGameBoard.getCellsXY(newX, newY);
            if(cl instanceof BitmapCell) {
                BitmapCell bmpCl = (BitmapCell)cl;
                boolean isRobot = bmpCl.getBmpId().contains("bot");
                if(isRobot) {
                    Robot r = Main.GameRunner.findBot(newX, newY);
                    return r.moveTo(newX2, newY2);
                }
            }
            return false;
        }

        @Override
        public boolean вверх() {
            boolean result = super.вверх();
            if(result == false) {
                if (tryPushRobot(X, Y+1, X, Y+2))
                    result = super.вверх();
            }
            return result;
        }

        @Override
        public boolean вниз() {
            boolean result = super.вниз();
            if(result == false) {
                if (tryPushRobot(X, Y-1, X, Y-2))
                    result = super.вниз();
            }
            return result;
        }

        @Override
        public boolean влево() {
            boolean result = super.влево();
            if(result == false) {
                if (tryPushRobot(X-1, Y, X-2, Y))
                    result = super.влево();
            }
            return result;
        }

        @Override
        public boolean вправо() {
            boolean result = super.вправо();
            if(result == false) {
                if (tryPushRobot(X+1, Y, X+2, Y))
                    result = super.вправо();
            }
            return result;
        }
    }
    public static abstract class Robot extends GameCharacter {
        static final RobotProgramBlock.MotionCommand ВВЕРХ  = RobotProgramBlock.MotionCommand.вверх;
        static final RobotProgramBlock.MotionCommand ВЛЕВО  = RobotProgramBlock.MotionCommand.влево;
        static final RobotProgramBlock.MotionCommand ВНИЗ   = RobotProgramBlock.MotionCommand.вниз;
        static final RobotProgramBlock.MotionCommand ВПРАВО = RobotProgramBlock.MotionCommand.вправо;
        static final RobotProgramBlock.SimplePredicateCommand справа_свободно = RobotProgramBlock.SimplePredicateCommand.справа_свободно;
        static final RobotProgramBlock.SimplePredicateCommand слева_свободно = RobotProgramBlock.SimplePredicateCommand.слева_свободно;
        static final RobotProgramBlock.SimplePredicateCommand сверху_свободно = RobotProgramBlock.SimplePredicateCommand.сверху_свободно;
        static final RobotProgramBlock.SimplePredicateCommand снизу_свободно = RobotProgramBlock.SimplePredicateCommand.снизу_свободно;
        static final RobotProgramBlock.SimplePredicateCommand справа_не_свободно = RobotProgramBlock.SimplePredicateCommand.справа_не_свободно;
        static final RobotProgramBlock.SimplePredicateCommand слева_не_свободно = RobotProgramBlock.SimplePredicateCommand.слева_не_свободно;
        static final RobotProgramBlock.SimplePredicateCommand сверху_не_свободно = RobotProgramBlock.SimplePredicateCommand.сверху_не_свободно;
        static final RobotProgramBlock.SimplePredicateCommand снизу_не_свободно = RobotProgramBlock.SimplePredicateCommand.снизу_не_свободно;
        static final RobotProgramBlock.SimplePredicateCommand снизу_стена = RobotProgramBlock.SimplePredicateCommand.снизу_стена;
        static final RobotProgramBlock.SimplePredicateCommand сверху_стена = RobotProgramBlock.SimplePredicateCommand.сверху_стена;
        static final RobotProgramBlock.SimplePredicateCommand слева_стена = RobotProgramBlock.SimplePredicateCommand.слева_стена;
        static final RobotProgramBlock.SimplePredicateCommand справа_стена = RobotProgramBlock.SimplePredicateCommand.справа_стена;
        static final RobotProgramBlock.Predicate снизу_не_стена = RobotProgramBlock.ComplexPredicate.
                НЕ(RobotProgramBlock.SimplePredicateCommand.снизу_стена);
        static final RobotProgramBlock.Predicate сверху_не_стена = RobotProgramBlock.ComplexPredicate.
                НЕ(RobotProgramBlock.SimplePredicateCommand.сверху_стена);
        static final RobotProgramBlock.Predicate слева_не_стена = RobotProgramBlock.ComplexPredicate.
                НЕ(RobotProgramBlock.SimplePredicateCommand.слева_стена);
        static final RobotProgramBlock.Predicate справа_не_стена = RobotProgramBlock.ComplexPredicate.
                НЕ(RobotProgramBlock.SimplePredicateCommand.справа_стена);

        protected RobotProgram.алг alg;
        public Robot(GameBoard gb, int X, int Y, String bmpId) throws Exception {
            super(gb, X, Y, bmpId);
        }
        @Override
        public void timeTick() {
            if(this.alg!=null) {
                if (this.alg.isDone()) this.alg.restart();
                this.alg.doStep();
            }
        }
    }
    public static class Robot1 extends Robot {
        public Robot1(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "robot1");
            this.alg = new RobotProgram.алг(this);
            alg.put(ВВЕРХ, ВВЕРХ,
                    ВЛЕВО, ВЛЕВО,
                    ВНИЗ,  ВНИЗ)
                    .put(ВПРАВО)
                    .put(ВПРАВО);
        }
    }

    public static class Robot2 extends Robot {
        public Robot2(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "robot2");
            alg = new RobotProgram.алг(this);
            alg.нц_пока(справа_не_стена)
                    .put(ВПРАВО);
            alg.нц_пока(справа_стена)
                    .put(ВВЕРХ);
            alg.put(ВПРАВО);
            alg.нц_пока(снизу_стена)
                    .put(ВПРАВО);
            alg.put(ВНИЗ);
            alg.нц_пока(RobotProgramBlock.ComplexPredicate.
                        И(слева_стена, снизу_не_стена))
                    .put(ВНИЗ);
            alg.нц_пока(слева_не_стена)
                    .put(ВЛЕВО);
            alg.нц_пока(RobotProgramBlock.ComplexPredicate.
                    И(снизу_не_стена, слева_стена))
                    .put(ВНИЗ);
            alg.put(ВЛЕВО);
            alg.нц_пока(RobotProgramBlock.ComplexPredicate.
                    И(слева_не_стена, сверху_стена))
                    .put(ВЛЕВО);
            //alg.put(ВЛЕВО);
            alg.put(ВВЕРХ);
            alg.нц_пока(справа_стена)
                    .put(ВВЕРХ);
            alg.put(ВПРАВО);
            alg.нц_пока(RobotProgramBlock.ComplexPredicate.
                    И(справа_не_стена, снизу_стена))
                    .put(ВПРАВО);
        }
    }
    public static class Robot3 extends Robot {
        public Robot3(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "robot2");
        }
        @Override
        public void timeTick() {
            int d = (int)(Math.random()*100)%4;
            switch (d) {
                case 0:       вверх(); break;
                case 1:       вниз(); break;
                case 2:       влево(); break;
                case 3:       вправо(); break;
            }
        }
    }
}
