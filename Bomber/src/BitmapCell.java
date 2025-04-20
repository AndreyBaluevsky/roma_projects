import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BitmapCell extends Cell {
    private byte[] btBMP;
    private int W = 0, H = 0, ci = 0;
    private ByteBuffer[] bmpLineBuffer = null;
    private Color[][] bitmap = null;

    public String getBmpId() {
        return bmpId;
    }

    protected String bmpId;
    //public Color TransparentColor = null;

    public  BitmapCell(String bmpId) {
        this(new File("Bomber\\res\\"+bmpId+".bmp"));
        this.bmpId = bmpId;
    }

    public  BitmapCell(File bmpFile) {
        if(loadBmp(bmpFile)) {
            bitmap = new Color[H][];
            loadBmpPixels(W, H, bmpLineBuffer, bitmap);
        }
        background = Color.WHITE;
        // TransparentColor = super.background;
    }

    private static void loadBmpPixels(int W, int H, ByteBuffer[] bmpLineBuffer, Color[][] bitmap) {
        byte[] btClr = new byte[3];
        for (int i = 0; i < H; i++) {
            ByteBuffer buffer = bmpLineBuffer[i];
            final Color[] curRow = new Color[W];
            bitmap[i] = curRow;
            for (int j = 0; j < W; j++) {
                buffer.get(btClr, 0, 3);
                int r = btClr[2], g = btClr[1], b = btClr[0];
                if(r<0) r+= 256; if(g<0) g+= 256; if(b<0) b+= 256;
                curRow[j] = new Color(r,g,b);
            }
        }
    }

    public Color getPixel(int x, int y) {
        return bitmap[H-y-1][x];
    }
    public void setPixel(int x, int y, Color pixelColor) {
        this.bitmap[H-y-1][x] = pixelColor;
    }

    private static int N4(int num) { // округлить до  кратного 4, не меньшего данного
        return (num + 3) & ~3;
    }
    private boolean loadBmp(File file) {
        int         fileSize = (int) file.length();
        boolean     loadResult = false;
        try(FileInputStream fis = new FileInputStream(file)) {
            btBMP = new byte[fileSize];
            fis.read(btBMP);
            if (btBMP[0]==0x42 && btBMP[1]==0x4D) {
                ByteBuffer buffer = ByteBuffer.wrap(btBMP).order(ByteOrder.LITTLE_ENDIAN);
                int bmpSize = buffer.getInt(2);
                if(bmpSize == fileSize) {
                    W = buffer.getInt(0x12); H = buffer.getInt(0x16); ci = buffer.getInt(0x1C);
                    if(ci==24) {
                        int cntBtsRow = 3*W, szPaddedRow = N4(cntBtsRow);
                        bmpLineBuffer = new ByteBuffer[H];
                        int iRowOffset = 0x36;
                        for (int i = 0; i < H; i++, iRowOffset+=szPaddedRow) {
                            buffer = ByteBuffer.wrap(btBMP, iRowOffset, szPaddedRow).order(ByteOrder.LITTLE_ENDIAN);
                            bmpLineBuffer[i] = buffer;
                        }
                        loadResult = true;
                    } else {
                        System.out.println("loadBmp: Картинка должна быть в формате TrueColor (24 bpp; 16М цветов)! file: "+file);
                    }
                } else {
                    System.out.println("loadBmp: Размер файла не соответствует заданному! file: "+file);
                }
            } else {
                System.out.println("loadBmp: Файл не является картинкой Windows BMP! file: "+file);
            }
        } catch (Exception e) {
            System.out.println("loadBmp Error! file");
            e.printStackTrace();
        }
        return loadResult;
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        Graphics.drawBitmap(1.0/W, 1.0/H, bitmap);
    }
}
