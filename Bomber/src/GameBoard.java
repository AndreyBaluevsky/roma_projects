import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class GameBoard
implements IDrawable {

    public GameCharacter.Bomber bomber;
    public ArrayList<GameCharacter.Robot> robotsList = new ArrayList<>();
    public ArrayList<GameCharacter.Bomb>  bombsList = new ArrayList<>();

    private final int cellRows, cellColumns;
    public int getCellRows() {
        return cellRows;
    }

    public int getCellColumns() {
        return cellColumns;
    }

    private float cellWidth, cellHeight;
    private float cellOffsetX, cellOffsetY;
    private Cell[][] m_Cells;

    private Cell defaultCell = new EmptyCell();

    public GameBoard(int cellRows, int cellColumns) throws Exception {
        this.cellRows = cellRows;
        this.cellColumns = cellColumns;
        initGBSize();
        putDemoGameboardItems();

    }

    private void initGBSize() {
        final float GBW = 0.85f, GBH=0.85f;
        this.m_Cells = new Cell[cellRows][cellColumns];
        cellWidth  = GBW/cellColumns;
        cellHeight = GBH/cellRows;
        cellHeight = cellWidth = Math.min(cellWidth, cellHeight);
        cellOffsetX = (1.0f-cellWidth*cellColumns)*0.5f;
        cellOffsetY = (1.0f-cellHeight*cellRows)*0.5f;
        putWallBorders();
    }

    public GameBoard(String filePath) throws Exception {
        final Element docElem = loadXml(filePath);
        this.cellRows       = getInt(docElem, "Rows");
        this.cellColumns    = getInt(docElem, "Cols");
        initGBSize();
        loadFromXml(docElem);
    }

    private void loadFromXml(Element docElem) throws Exception {
        NodeList nList = docElem.getElementsByTagName("WallH");
        for (int i=0; i < nList.getLength(); i++) {
            final Node nWH = nList.item(i);
            final Element lmWH = (Element) nWH;
            final int x = getInt(lmWH, "StartX");
            final int y = getInt(lmWH, "StartY");
            final int k = getInt(lmWH, "Count");
            putWallH(x, y, k);
        }
        nList = docElem.getElementsByTagName("WallV");
        for (int i=0; i < nList.getLength(); i++) {
            final Node nWH = nList.item(i);
            final Element lmWH = (Element) nWH;
            final int x = getInt(lmWH, "StartX");
            final int y = getInt(lmWH, "StartY");
            final int k = getInt(lmWH, "Count");
            putWallV(x, y, k);
        }
        NodeList nListZigZag = docElem.getElementsByTagName("WallZigZag");
        for (int i=0; i < nListZigZag.getLength(); i++) {
            final Node nWZ = nListZigZag.item(i);
            final Element lmWZ = (Element) nWZ;
            final NodeList nlPoints = lmWZ.getElementsByTagName("Point");
            final int cntZigZag = nlPoints.getLength();
            final Point[] pointsXY = new Point[cntZigZag];
            for(int j = 0; j < cntZigZag; j++) {
                Element lmPoint = (Element) nlPoints.item(j);
                final int pX = getInt(lmPoint, "X");
                final int pY = getInt(lmPoint, "Y");
                pointsXY[j] = new Point(pX, pY);
            }
            putWallZigZag(pointsXY);
        }
        NodeList nListBmp = docElem.getElementsByTagName("Bitmap");
        for(int j = 0; j < nListBmp.getLength(); j++) {
            final Node nBmp = nListBmp.item(j);
            final Element lmBmp = (Element) nBmp;
            final int x = getInt(lmBmp, "X");
            final int y = getInt(lmBmp, "Y");
            final String id = lmBmp.getAttribute("Id");
            setCellsXY(x, y, new BitmapCell(id));
        }

        NodeList nListRobot = docElem.getElementsByTagName("Robot");
        for(int j = 0; j < nListRobot.getLength(); j++) {
            final Element lmRobot = (Element) nListRobot.item(j);
            final int x = getInt(lmRobot, "X");
            final int y = getInt(lmRobot, "Y");
            final String robotType = lmRobot.getAttribute("Type");
            final GameCharacter.Robot newRbt;
            switch (robotType) {
                case "Universal": break;
                default:
                    newRbt = makeStdRobot(x, y, robotType);
                    robotsList.add(newRbt);
            }
        }

        NodeList nListBomber = docElem.getElementsByTagName("Bomber");
        if(nListBomber.getLength()>0) {
            final Element lmBomber = (Element)nListBomber.item(0);
            final int x = getInt(lmBomber, "X");
            final int y = getInt(lmBomber, "Y");
            bomber = new GameCharacter.Bomber(this, x, y);
        }
		
        NodeList nListBomb = docElem.getElementsByTagName("Bomb");
		for(int j = 0; j < nListBomb.getLength(); j++) {
			final Element lmBomb = (Element) nListBomb.item(j);
            final int x = getInt(lmBomb, "X");
            final int y = getInt(lmBomb, "Y");
            final GameCharacter.Bomb bomb =
                    lmBomb.hasAttribute("TimeOut")?
                        new GameCharacter.Bomb(this, x, y,
                                getInt(lmBomb, "TimeOut")):
                        new GameCharacter.Bomb(this, x, y);
            bombsList.add(bomb);
		}
    }

    private int getInt(Element xlm, String x) {
        return Integer.parseInt(xlm.getAttribute(x));
    }

    private GameCharacter.Robot makeStdRobot(int x, int y, String robotType) throws Exception {
        Class<? extends GameCharacter.Robot> clsRobot =
                (Class<? extends GameCharacter.Robot>)Class.forName("GameCharacter$"+robotType);
        Constructor<?> ctor = clsRobot.getConstructor(GameBoard.class, int.class, int.class);
        return (GameCharacter.Robot)ctor.newInstance(new Object[] {this, x, y });
    }

    private Element loadXml(String filePath) throws ParserConfigurationException, SAXException, IOException {
        // System.out.println("Calling loadXml ...");
        File fXmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc;
        if(fXmlFile.exists()) {
            doc = dBuilder.parse(fXmlFile);
        } else {
            final String fileName = fXmlFile.getName();
            // System.out.println("loadXml: try reading with getResourceAsStream: "+fileName);
            InputStream resStream = Main.class.getClassLoader().getResourceAsStream(fileName);
            doc = dBuilder.parse(resStream);
        }
        final Element docElem = doc.getDocumentElement();
        docElem.normalize();
        return docElem;
    }


    private void putDemoGameboardItems() throws Exception {
        putWallH(2, 2, 1);
        putWallH(4, 3, 2);
        putWallH(3, 5, 3);
        putBrickcellWallH (6, 7, 5);
        setCellsXY(8, 4, new BitmapCell("pic_v3"));
        bomber = new GameCharacter.Bomber(this, 1, 1);

        robotsList.add(new GameCharacter.LoopWalker2(this, 16, 10));
        robotsList.add(new GameCharacter.Robot2(this, 5, 8));
        robotsList.add(new GameCharacter.Wanderer(this, 18, 18));
        robotsList.add(new GameCharacter.LoopWalker(this, 21, 25));
        robotsList.add(new GameCharacter.Robot5(this, 24, 23));
        robotsList.add(new GameCharacter.Robot6(this, 26, 16));
        robotsList.add(new GameCharacter.Robot7(this, 24, 21));
    }


    private BrickCell[] putBrickcellWallH(int x, int y, int k) {
        BrickCell StartCell = new BrickCell();
        setCellsXY(x, y, StartCell);
        int d = 1;
        if(k<0) {
            d = -1; k=-k;
        }
        for (int i = 0; i < k-1; i++) {
            x += d;
            setCellsXY(x, y, StartCell);
        }
        return new BrickCell[]{StartCell};
    }



    public WallCell[] putWallZigZag(Point[] pointsXY) {
        Point p1 = pointsXY[0];
        WallCell w1 = null, w2 = null;
        for (Point p2: pointsXY) {
            boolean qX = p1.x == p2.x;
            boolean qY = p1.y == p2.y;
            WallCell[] wallCells;
            if(qX && !qY) {
                w1 = w2;
                wallCells = putWallV(p1.x, p1.y, p2.y-p1.y, w1, null, null );
                w2 = wallCells[wallCells.length-1];
            } else if(!qX && qY) {
                w1 = w2;
                wallCells = putWallH(p1.x, p1.y, p2.x-p1.x, w1, null, null);
                w2 = wallCells[wallCells.length-1];
            }
            p1 = p2;
        }
        return null;
    }


    public void putBrickBorders(int x, int y, int k) {

    }


    public WallCell[] putWallH(int x, int y, int k) {
        return putWallH(x, y, k, null, null, null);
    }
    public WallCell[] putWallH(int x, int y, int k, WallCell StartCell, WallCell InnerCell, WallCell LastCell) {
        if (k==0) return null;
        final float brdlwThin = 0.05f;
        if(StartCell==null) {
            StartCell = new WallCell( brdlwThin,
                    Color.WHITE, Color.WHITE,
                    Color.BLACK, Color.BLACK );
            StartCell.setStyle(4);
        }
        setCellsXY(x, y, StartCell);
        int d=1;
        if(k<0) {
            d = -1;
            k = -k;
        }
        if(k==1) {
            return  new WallCell[]{StartCell};
        } else if(k==2) {
            if(LastCell==null) {
                LastCell = new WallCell( brdlwThin,
                        Color.WHITE, Color.WHITE,
                        Color.BLACK, Color.BLACK );
                LastCell.setStyle(4);
            }
            setCellsXY(x+d, y, LastCell);
            // объединить, убрав внутренние границы
            StartCell.removeBorderRight();
            LastCell.removeBorderLeft();
            return  new WallCell[]{StartCell, LastCell};
        } else {
            for (int i = 0; i < k-1; i++) {
                x += d;
                setCellsXY(x, y, StartCell);
            }

            return  new WallCell[]{StartCell, InnerCell, LastCell};
        }
    }

    public WallCell[] putWallV(int x, int y, int k) {
        return putWallV(x, y, k, null, null, null);
    }
    public WallCell[] putWallV(int x, int y, int k, WallCell StartCell, WallCell InnerCell, WallCell LastCell) {
        if (k==0) return null;
        final float brdlwThin = 0.05f;
        if(StartCell==null) {
            StartCell = new WallCell( brdlwThin,
                    Color.WHITE, Color.WHITE,
                    Color.BLACK, Color.BLACK );
            StartCell.setStyle(4);
        }
        setCellsXY(x, y, StartCell);
        int d = k<0? -1: 1;
        k = Math.abs(k);
        if(k==1) {
            return  new WallCell[]{StartCell};
        } else if(k==2) {
            if(LastCell==null) {
                LastCell = new WallCell( brdlwThin,
                        Color.WHITE, Color.WHITE,
                        Color.BLACK, Color.BLACK );
                LastCell.setStyle(4);
            }
            setCellsXY(x, y+d, LastCell);
            // объединить, убрав внутренние границы
            StartCell.removeBorderTop();
            LastCell.removeBorderBottom();
            return  new WallCell[]{StartCell, LastCell};
        } else {

            for (int i = 0; i < k-1; i++) {y += d;
                setCellsXY(x, y, StartCell);
            }
            return  new WallCell[]{StartCell, InnerCell, LastCell};
        }
    }


    private void putWallBorders() {
        final float brdlw = 0.1f;
        Color ltGr = Color.LIGHT_GRAY.darker();

        final WallCell wallCellTL = new WallCell(brdlw,
                Color.BLACK, Color.BLACK, null, null );
        wallCellTL.setStyle(3);

        final WallCell wallCellTR = new WallCell(brdlw,
                null, Color.BLACK, Color.BLACK, null);
        wallCellTR.setStyle(3);
        final WallCell wallCellBL = new WallCell(brdlw,
                Color.BLACK, null, null, Color.BLACK);
        wallCellBL.setStyle(3);
        final WallCell wallCellBR = new WallCell(brdlw,
                null, null, Color.BLACK, Color.BLACK);
        wallCellBR.setStyle(3);

        final WallCell wallCellV = new WallCell(brdlw, Color.BLACK, null, Color.BLACK, null);
        wallCellV.setStyle(2);

        final WallCell wallCellH = new WallCell(brdlw, null, Color.BLACK, null, Color.BLACK);
        wallCellH.setStyle(2);

        setCellsRC(0, 0, wallCellTR);
        setCellsRC(cellRows-1, 0, wallCellBR);
        setCellsRC(0, cellColumns-1, wallCellTL);
        setCellsRC(cellRows-1, cellColumns-1, wallCellBL);

        for (int i=1; i<cellColumns-1; ++i) {
            setCellsRC(0, i, wallCellH);
            setCellsRC(cellRows-1, i, wallCellH);
        }
        for (int i=1; i<cellRows-1; ++i) {
            setCellsRC(i, 0, wallCellV);
            setCellsRC(i, cellColumns-1, wallCellV);
        }
    }

    public Cell getCellsRC(int R, int C) { // 0-based
        if(m_Cells[R]==null)
            m_Cells[R] = new Cell[cellColumns];
        Cell cl = m_Cells[R][C];
        if(cl == null) {
            cl = defaultCell;
            m_Cells[R][C] = cl;
        }
        return cl;
    }
    public Cell setCellsRC(int R, int C, Cell newCell) {
        if(m_Cells[R]==null)
            m_Cells[R] = new Cell[cellColumns];
        m_Cells[R][C] = newCell;
        return newCell;
    }

    public Cell setCellsXY(int X, int Y, Cell newCell) { // 1-based, B-L -> T-R
        return setCellsRC(cellRows-Y-1, cellColumns-X-1, newCell);
    }
    public Cell getCellsXY(int X, int Y) { // 1-based, B-L -> T-R
        return getCellsRC(cellRows-Y-1,cellColumns-X-1);
    }
	
	public boolean hasBomber(int X, int Y) {
		return bomber!=null && bomber.X==X && bomber.Y==Y;
	}

	public GameCharacter.Robot findRobot(int X, int Y) {
		for(GameCharacter.Robot r: this.robotsList) {
			if(r.X==X && r.Y == Y) return r;
		}
		return null;
	}
	public GameCharacter.Alive findAlive(int X, int Y) {
		GameCharacter.Alive foundAlive = null;
		if(hasBomber(X, Y))
			foundAlive = bomber;
		else 
			foundAlive = findRobot(X, Y);
		return foundAlive;
	}
	
    @Override
    public void draw(Graphics g) {
        //final float CW = 0.95f, CH=0.95f;
        //final float CW = 0.99f, CH=0.99f;
        final float CW = 1f, CH=1f;
        glPushMatrix();
        glTranslatef(1, 1, 0);
        glScalef(2, 2, 1);
        float y1 = cellOffsetY;
        for (int i=0; i<cellRows; i++) {
            float y2 = y1+cellHeight;
            float x1 = cellOffsetX;
            for (int j=0; j<cellColumns; j++) {
                float x2 = x1+cellWidth;
                glPushMatrix();
                glTranslatef(-x2, -y2, 0);
                glScalef(cellWidth*CW, cellHeight*CH, 1);
                getCellsRC(i,j).draw(g);
                glPopMatrix();
                x1 = x2;
            }
            y1 = y2;
        }
        glPopMatrix();
    }
}
