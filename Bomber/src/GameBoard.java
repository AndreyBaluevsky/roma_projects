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

import static org.lwjgl.opengl.GL11.*;

public class GameBoard
implements IDrawable {
    private final int cellRows, cellColumns;
    private float cellWidth, cellHeight;
    private float cellOffsetX, cellOffsetY;
    private Cell[][] m_Cells;

    private Cell defaultCell = new EmptyCell();

    public GameBoard(int cellRows, int cellColumns) {
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
        this.cellRows       = Integer.parseInt( docElem.getAttribute("Rows"));
        this.cellColumns    = Integer.parseInt( docElem.getAttribute("Cols"));
        initGBSize();
        loadFromXml(docElem);
    }

    private void loadFromXml(Element docElem) {
        NodeList nList = docElem.getElementsByTagName("WallH");
        for (int i=0; i < nList.getLength(); i++) {
            final Node nWH = nList.item(i);
            final Element lmWH = (Element) nWH;
            final int x = Integer.parseInt(lmWH.getAttribute("StartX"));
            final int y = Integer.parseInt(lmWH.getAttribute("StartY"));
            final int k = Integer.parseInt(lmWH.getAttribute("Count"));
            putWallH(x, y, k);
        }
        nList = docElem.getElementsByTagName("WallV");
        for (int i=0; i < nList.getLength(); i++) {
            final Node nWH = nList.item(i);
            final Element lmWH = (Element) nWH;
            final int x = Integer.parseInt(lmWH.getAttribute("StartX"));
            final int y = Integer.parseInt(lmWH.getAttribute("StartY"));
            final int k = Integer.parseInt(lmWH.getAttribute("Count"));
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
                final int pX = Integer.parseInt(lmPoint.getAttribute("X"));
                final int pY = Integer.parseInt(lmPoint.getAttribute("Y"));
                pointsXY[j] = new Point(pX, pY);
            }
            putWallZigZag(pointsXY);
        }
    }

    private Element loadXml(String filePath) throws ParserConfigurationException, SAXException, IOException {
        File fXmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        final Element docElem = doc.getDocumentElement();
        docElem.normalize();
        return docElem;
    }


    private void putDemoGameboardItems() {
        putWallH(2, 2, 1);
        putWallH(4, 3, 2);
        putWallH(3, 5, 3);
        putBrickcellWallH (6, 7, 5);
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
        return getCellsRC(Y-1,X-1);
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
