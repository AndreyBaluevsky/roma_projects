import javax.naming.spi.DirectoryManager;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.jar.JarFile;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class BitmapCell extends Cell {
    private byte[] btBMP;
    private int W = 64;

    public int getHeight() { return H; }
    public int getWidth()  { return W; }

    private int H = 64;
    private int ci = 0;
    private ByteBuffer[] bmpLineBuffer = null;
    private Color[][] bitmap = null;
    public int rotation = 0; // поворот на угол, кратный 90 градусов. Значения: 0, 1, 2, 3

    public String getBmpId() {
        return bmpId;
    }
    public void setBmpId(String bmpId) {
        loadBmpFileFromId(bmpId);
        this.bmpId = bmpId;
    }

    protected String bmpId;
    public Color TransparentColor = null;

    public BitmapCell(String bmpId) {
        setBmpId(bmpId);
        background = Color.WHITE;
        // TransparentColor = super.background;
    }

    protected void loadBmpFileFromId(String bmpId) {
        loadBmpFile(new File("Bomber\\res\\"+bmpId+".bmp"));
    }
    protected void loadBmpFile(File bmpFile) {
        if(loadBmp(bmpFile)) {
            bitmap = new Color[H][];
            loadBmpPixels(W, H, bmpLineBuffer, bitmap);
            TransparentColor = bitmap[0][0];
        }
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
        boolean     loadResult = false;
        try(InputStream fis = getBitmapStream(file)) {
            btBMP = readResourceFile(file, fis);
            int fileSize = btBMP.length;
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

    private byte[] readResourceFile0(File file, InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = inputStream.read(data)) != -1) {
            outputStream.write(data, 0, nRead);
        }
        // Now 'outputStream.toByteArray()' contains all the bytes from the resource
        return outputStream.toByteArray();
    }

    private byte[] readResourceFile(File file, InputStream fis) throws IOException, URISyntaxException {
        int  fileSize = getResourceItemLength(file);
        btBMP = new byte[fileSize];
        fis.read(btBMP);
        return btBMP;
    }

    private int getResourceItemLength(File file) throws IOException, URISyntaxException {
        int length = (int) file.length();
        if(length==0) {
            // Step 1: Get the current classloader's protection domain
            ProtectionDomain protectionDomain = Main.class.getProtectionDomain();
            // Step 2: Extract the code source location
            CodeSource codeSource = protectionDomain.getCodeSource();
            URL locationUrl = codeSource.getLocation();
            // Step 3: Check if the location refers to a JAR file
            String jarPath = null;
            if ("jar".equals(locationUrl.getProtocol())) { // It's a JAR file
                jarPath = locationUrl.getPath();
                // Remove "!/" suffix (Java-specific separator)
                int indexOfBangSlash = jarPath.indexOf('!');
                if (indexOfBangSlash > 0) {
                    jarPath = jarPath.substring(0, indexOfBangSlash); // Only keep the part before !/
                    length = getLengthInJar(file, jarPath);
                }
            } else if ("file".equals(locationUrl.getProtocol())) { // It could be a direct classpath
                File jarFile = new File(locationUrl.toURI());
                if (jarFile.isDirectory()) {
                    File resFile = new File(jarFile, file.getName());
                    length = (int)resFile.length();
                } else {
                    jarPath = jarFile.getAbsolutePath();
                    length = getLengthInJar(file, jarPath);
                }
            }
        }
        return length;
    }

    private int getLengthInJar(File file, String jarPath) throws IOException {
        int length;
        JarFile jarFile = new JarFile(jarPath);
        Long size = jarFile.getEntry(file.getName()).getSize();
        //System.out.println("getResourceItemLength: resouce size: " + size.toString());
        //You can get the compressed size too:
        //Object compressedSize = jarFile.getEntry("file.txt").getCompressedSize();
        //System.out.println("CompressedSize: " + compressedSize);
        return size.intValue();
    }

    private InputStream getBitmapStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            //System.out.println("BitmapCell: FileNotFound: "+file.getAbsolutePath());
            final String fileName = file.getName();
            //System.out.println("try reading with getResourceAsStream: "+fileName);
            return Main.class.getClassLoader().getResourceAsStream(fileName); //getClass()
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        Graphics.drawBitmap(1.0/W, 1.0/H, bitmap, rotation);
    }

    // alpha-blend lines with a color tint
    public void fadeLines(Color mixColor, double mixIntensity, int bmpStripe1, int bmpStripe2) {
        final double mi1 = 1 - mixIntensity;
        final double MR = mixColor.getRed()  *mixIntensity;
        final double MG = mixColor.getGreen()*mixIntensity;
        final double MB = mixColor.getBlue() *mixIntensity;
        for (int r = bmpStripe2; r < bmpStripe1; r++) {
            for (int j = 0; j < W; j++) {
                Color clr1 = bitmap[r][j];
                Color clr2 =
                        //(clr1.equals(TransparentColor))? mixColor: clr1.brighter().brighter(); // просто - для отладки
                        (clr1.equals(TransparentColor))?
                                mixColor:
                         new Color(
                                (int)(clr1.getRed()*mi1   + MR),
                                (int)(clr1.getGreen()*mi1 + MG),
                                (int)(clr1.getBlue()*mi1  + MB)
                        ).brighter(); //.brighter();
                bitmap[r][j] = clr2;
            }
        }
        TransparentColor = bitmap[0][0];
    }


}
