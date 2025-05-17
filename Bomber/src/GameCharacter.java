import java.awt.*;

public abstract class GameCharacter {

    protected int X, Y;
    protected Cell cell;
    protected GameBoard parentGameBoard;

    interface Alive {
        public boolean isAlive();
        public int applyDamage(int damagePercent);
    }

    public GameCharacter(GameBoard gb, int X, int Y, String bmpId) throws Exception {
        this.parentGameBoard = gb;
        this.X = X;
        this.Y = Y;
        if(bmpId!=null)
            cell = gb.setCellsXY(X, Y, new BitmapCell(bmpId));
        //if(moveTo(X,Y)==false) throw new Exception("Персонаж вне игрового поля!");
    }

    abstract public void    timeTick();


    protected boolean isEmptyCell(Cell cl) {
        boolean EmptyCell = cl == null || cl instanceof EmptyCell;
        return EmptyCell;
	}
    protected boolean isBomberCell(int X, int Y) {
        return parentGameBoard.bomber!=null &&
               parentGameBoard.bomber.X==X &&
               parentGameBoard.bomber.Y==Y;
    }
    protected boolean isEmptyCell(int X, int Y) {
        Cell cl = parentGameBoard.getCellsXY(X, Y);
		return isEmptyCell(cl);

    }
    protected boolean isWallOrBrickCell(Cell cl) {
        return cl != null && (
                (cl instanceof WallCell) || (cl instanceof BrickCell));		
	}
    protected boolean isWallOrBrickCell(int X, int Y) {
        Cell cl = parentGameBoard.getCellsXY(X, Y);
		return isWallOrBrickCell(cl);
    }
	protected boolean isTeleportCell(Cell cl) {
		if(cl != null && (cl instanceof BitmapCell)) {
			BitmapCell bmpCell = (BitmapCell)cl;
			return bmpCell.getBmpId().startsWith("pic_v2");
		}
        return false;
	}		
	protected boolean isTeleportCell(int X, int Y) {
		Cell cl = parentGameBoard.getCellsXY(X, Y);
		return isTeleportCell(cl);
	}
	protected boolean isRobotCell(Cell cl) {
		if(cl instanceof BitmapCell) {
			BitmapCell bmpCl = (BitmapCell)cl;
			return bmpCl.getBmpId().contains("bot");
		}
		return false;
	}
	protected boolean isRobotCell(int X, int Y) {
        Cell cl = parentGameBoard.getCellsXY(X, Y);
        return isRobotCell(cl);
	}

    public static abstract class Movable extends GameCharacter {
		protected int originalX, originalY;
        public Movable(GameBoard gb, int X, int Y, String bmpId) throws Exception {
            super(gb, X, Y, bmpId);
			originalX = X; originalY = Y;
        }
        enum NextType {
            Bomb;
        };
        public NextType nextType = null;
        public boolean moveTo(int newX, int newY) {
            boolean bAboveLowerBound = newY >= 0;
            boolean bBelowUpperBound = newY < parentGameBoard.getCellRows() - 1;
            boolean bRightToLeftBorder = newX >= 1;
            boolean bLeftToRightBorder = newX < parentGameBoard.getCellColumns() - 1;

            boolean bInsideGameBoard = bAboveLowerBound && bBelowUpperBound &&
                    bRightToLeftBorder && bLeftToRightBorder;

            final Cell cl = parentGameBoard.getCellsXY(newX, newY);
            if(bInsideGameBoard) {
                if(isEmptyCell(cl)) {
                    Cell newCell = null;
                    if(nextType!=null)
                        switch (nextType) {
                            case Bomb:
                                try {
                                    Bomb newBomb = new Bomb(parentGameBoard, X, Y);
                                    parentGameBoard.bombsList.add(newBomb);
                                    newCell = newBomb.cell;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                nextType = null;
                                break;
                        }
                    parentGameBoard.setCellsXY(X, Y, newCell);
                    X = newX; Y = newY;
                    parentGameBoard.setCellsXY(X, Y, cell);
                    return  true;
                } else if(isTeleportCell(cl)) {
					return teleport(X,Y);
				}
            }
            return  false;
        }

		public boolean teleport(int fromX, int fromY) {
			return moveTo(originalX, originalY);
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
    }

    public static class AliveMixin implements Alive {
        protected int livePercent = 100;
        protected BitmapCell cell;
        private int lastBmpStripe;

        public AliveMixin(BitmapCell cell) {
            this.cell = cell;
            if(cell!=null)
                lastBmpStripe = cell.getHeight();
        }

        @Override
        public boolean isAlive() {
            return livePercent>0;
        }

        static final Color damageColor = Color.RED.darker().darker();
        private void updateHealthStatus() {
            final int H = cell.getHeight();
            int newBmpStripe = H-Math.max(Math.min((int)(H*(1-livePercent/100.0)), H), 0);
            cell.fadeLines(damageColor, 0.5, lastBmpStripe, newBmpStripe);
            lastBmpStripe = newBmpStripe; // - 1;
        }

        @Override
        public int applyDamage(int damagePercent) {
            livePercent -= damagePercent;
            if(cell!=null) {
                updateHealthStatus();
            }
            return livePercent;

        }
    }


    public static class Bomber extends Movable implements Alive {
        public Bomber(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "bomber");
        }

        private boolean tryPushRobot(int newX, int newY, int newX2, int newY2) {
            if(isRobotCell(newX, newY)) {                
				Robot r = parentGameBoard.findRobot(newX, newY);
				return r.moveTo(newX2, newY2);                
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



        @Override
        public void timeTick() {

        }
		
		@Override
		public boolean teleport(int fromX, int fromY) {
			//переход к следующему уровню или к игровому полю "GameBoard_YouWin.xml"
            try {
                Main.activeGamePlay.youWin();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
		}

        protected AliveMixin alive = new AliveMixin((BitmapCell) this.cell);

        @Override public boolean isAlive() { return alive.isAlive(); }
        @Override public int applyDamage(int damagePercent) {
            if(alive.applyDamage(damagePercent)<=0) {
                try {
                    Main.activeGamePlay.gameOver();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return alive.livePercent;
        }
    }

    public static class Bomb extends GameCharacter implements Alive {
        int curFrame = 0;
        public Flame flame;
        protected AliveMixin alive = new AliveMixin(null);
        public Bomb(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "bomb00");
        }
        public Bomb(GameBoard gb, int X, int Y, int timeOut) throws Exception {
            super(gb, X, Y, "bomb00");
            this.alive.livePercent = 10*timeOut;
        }

        @Override public boolean isAlive() { return alive.isAlive(); }
        @Override public int applyDamage(int damagePercent) {
            return alive.applyDamage(damagePercent);
        }
		
		void nextFrame() {
			BitmapCell bmpCell = (BitmapCell)parentGameBoard.getCellsXY(X, Y);
			bmpCell.setBmpId("bomb0"+String.valueOf(curFrame));
			curFrame += 1;			
		}

        @Override
        public void timeTick() {
            if(isAlive()) {
                applyDamage(10);
 				if(alive.livePercent<=10)
					nextFrame();
                return;
            } else if(curFrame<=7) {
                if(flame == null)
                try {
                    flame = new Flame(parentGameBoard, X, Y);
                } catch (Exception e) {
                    e.printStackTrace();
                }
				nextFrame();
            }
            if(flame.isAlive())
                flame.timeTick();
            else  {
                parentGameBoard.bombsList.remove(this);
                parentGameBoard.setCellsXY(X,Y,null);
            }
        }
    }

    public static class Flame extends GameCharacter implements Alive {
        @Override
        public boolean isAlive() {
            return (timeToLive > 0) || (clrRad<putRad);
        }

        @Override
        public int applyDamage(int damagePercent) {
            timeToLive--;
            return timeToLive;
        }

        int timeToLive = 3;
        int putRad = 0, clrRad = 0;
        int[][] flameRadDir = new int[][]{
            {1,1,1},
            {1,0,1},
            {1,1,1}
        };
        public Flame(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, null); //"flame00"
        }
        private boolean clearMyCell(int dx, int dy, int R) {
            final int radDir = flameRadDir[1 + dx][1 + dy];
            if(R <= Math.abs(radDir)-1 ) {
                final int newX = X + R * dx;
                final int newY = Y + R * dy;
                final Cell cell = parentGameBoard.getCellsXY(newX, newY);
                if (cell instanceof BitmapCell) {
                    final BitmapCell bmpCell = (BitmapCell) cell;                    
                    if (bmpCell.getBmpId().startsWith("flame")) {
                        parentGameBoard.setCellsXY(newX, newY, null);
                        return true;
                    }
                }
            }
            return false;
        }
        private boolean putMyCell(int dx, int dy, int R) {
            if(flameRadDir[1+dx][1+dy]>=1) {
                final int newX = X + R*dx;
                final int newY = Y + R*dy;
                final Cell cell = parentGameBoard.getCellsXY(newX, newY);
                if (isEmptyCell(newX, newY)) {
                    final int xy = dx * dy;
                    final String bmpId;
                    final int rotation;
                    if(xy == 0) {
                        bmpId = "flame00";
                        rotation = Math.abs(dy) * (1 - dy) + (1 - dx);
                    } else {
                        bmpId = "flamer00";
                        rotation = 1 - dy + ((1 - xy)>>1);
                    }
                    final BitmapCell flameCell = new BitmapCell(bmpId);
                    flameCell.rotation = rotation;
                    parentGameBoard.setCellsXY(newX, newY, flameCell);
                    flameRadDir[1 + dx][1 + dy] ++;
                    return true;
                } else {
					Alive a = parentGameBoard.findAlive(newX, newY);
					if(a != null) {
						int damage = 100/R; //(R**R);
						a.applyDamage(damage);
					}
                    flameRadDir[1 + dx][1 + dy] *= -1;
                }
            }
            return false;
        }
        private void flameAround(boolean putFlame) {
            if(putFlame) {
                putMyCell(1,  1,   putRad);
                putMyCell(1,  -1,  putRad);
                putMyCell(-1,  1,  putRad);
                putMyCell(-1,  -1, putRad);
                putMyCell(1,  0,  putRad);
                putMyCell(-1, 0,  putRad);
                putMyCell(0,  1,  putRad);
                putMyCell(0,  -1, putRad);
            } else {
                clearMyCell(1,  1,   clrRad);
                clearMyCell(1,  -1,  clrRad);
                clearMyCell(-1,  1,  clrRad);
                clearMyCell(-1,  -1, clrRad);
                clearMyCell(1, 0,  clrRad);
                clearMyCell(-1, 0, clrRad);
                clearMyCell(0, 1,  clrRad);
                clearMyCell(0, -1, clrRad);
            }
        }
        @Override
        public void timeTick() {
            if(timeToLive>0) {
                putRad++;
                flameAround(true);
                applyDamage(1);
            } else if(clrRad<putRad) {
                clrRad++;
                flameAround(false);
            }
        }
    }

    public static abstract class Robot extends Movable implements Alive
    {
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
		@Override
		public boolean teleport(int fromX, int fromY) {
			if(super.teleport(fromX, fromY))
			    if(alg!=null)
				    alg.restart();
			return false;
		}

        protected AliveMixin alive = new AliveMixin((BitmapCell)cell);
        @Override public boolean isAlive() { return alive.isAlive(); }
        @Override public int applyDamage(int damagePercent) {
            final int newLifePercent = alive.applyDamage(damagePercent);
            if(newLifePercent <= 0) {
                parentGameBoard.robotsList.remove(this);
                parentGameBoard.setCellsXY(X,Y,null);
            }
            return newLifePercent;
        }

        @Override
        public boolean moveTo(int newX, int newY) {
            if(isBomberCell(newX,newY)) {
                parentGameBoard.bomber.applyDamage(30);
                return true;
            }
            else
                return super.moveTo(newX, newY);
        }
    }

    public static class LoopWalker2 extends Robot {
        public LoopWalker2(GameBoard gb, int X, int Y) throws Exception {
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
    public static class Wanderer extends Robot {
        public Wanderer(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "robot3");
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
    public static class LoopWalker extends Robot {
        public LoopWalker(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "robot4");
            alg = new RobotProgram.алг(this);
            alg.put(ВПРАВО);
            alg.put(ВНИЗ);
            alg.put(ВЛЕВО);
            alg.put(ВВЕРХ);
        }
    }
    public static class Robot5 extends Robot {
        public Robot5(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "robot5");
            alg = new RobotProgram.алг(this);
            alg.put(ВПРАВО);
            alg.put(ВНИЗ);
            alg.put(ВЛЕВО);
            alg.put(ВВЕРХ);
        }
    }
    public static class Robot6 extends Robot {
        public Robot6(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "robot6");
            alg = new RobotProgram.алг(this);
            alg.put(ВПРАВО);
            alg.put(ВНИЗ);
            alg.put(ВЛЕВО);
            alg.put(ВВЕРХ);
        }
    }
    public static class Robot7 extends Robot {
        public Robot7(GameBoard gb, int X, int Y) throws Exception {
            super(gb, X, Y, "robot7");
            alg = new RobotProgram.алг(this);
            alg.put(ВПРАВО);
            alg.put(ВНИЗ);
            alg.put(ВЛЕВО);
            alg.put(ВВЕРХ);
        }
    }
}
