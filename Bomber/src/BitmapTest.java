import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BitmapTest {
    public static int N4(int num) { // округлить до  кратного 4, не меньшего данного
        //int остаток = num % 4;
        //return  (остаток == 0)? num: num + (4 - остаток);
        return (num + 3) & ~3;
        // 01100 12     01111   15      01100   12
        // 01101 13     10000   16      10000   16
        // 01110 14     10001   17      10000   16
        // 01111 15     10010   18      10000   16
        // 10000 16     10011   19      10000   16
    }

    public static void main(String[] args) {
        File file = new File("Bomber\\res\\pic_v3.bmp");
        int fileSize = (int) file.length();
        try(FileInputStream fis = new FileInputStream(file)) {
            byte[] btBMP = new byte[fileSize];
            fis.read(btBMP);
            if (btBMP[0]==0x42 && btBMP[1]==0x4D) {
                System.out.println("Информация о картинке BMP:");
                ByteBuffer buffer = ByteBuffer.wrap(btBMP).order(ByteOrder.LITTLE_ENDIAN);
                // Big-endian: Старший байт (MSB) записывается по младшему адресу.
                // Так принято записывать число в арабской системе (справа налево).
                // Например, число 0x12345678 в памяти будет так:
                // Адрес:   0x00  0x01  0x02  0x03
                // Байты:   0x12  0x34  0x56  0x78
                // Little-endian: Младший байт (least significant byte, LSB) записывается по младшему адресу.
                // Так принято записывать текст в европейской системе (слева направо).
                // Например, число 0x12345678 в памяти будет так:
                // Адрес:   0x00  0x01  0x02  0x03
                // Байты:   0x78  0x56  0x34  0x12
                int bmpSize = buffer.getInt(2);
                System.out.println("Размер файла: "+bmpSize);
                int W = buffer.getInt(0x12), H = buffer.getInt(0x16), ci = buffer.getInt(0x1C);
                System.out.println(String.format("Разрешение и глубина цвета: %d x %d x %d", W, H, ci));
                if(ci==24) {
                    System.out.println("    >>>    TrueColor, 24 bits/pixel, RGB");
                    int cntBtsRow = 3*W, szPaddedRow = N4(cntBtsRow);
                    System.out.println("Минимальное количество байт на строку: "+cntBtsRow);
                    System.out.println("Реальное    количество байт на строку: "+szPaddedRow);
                    System.out.println("Растр:");
                    int iRowOffset = 0x36;
                    byte[] btClr = new byte[3];
                    for (int i = 0; i < H; i++, iRowOffset+=szPaddedRow) {
                        buffer = ByteBuffer.wrap(btBMP, iRowOffset, szPaddedRow).order(ByteOrder.LITTLE_ENDIAN);
                        for (int j = 0; j < W; j++) {
                            buffer.get(btClr, 0, 3);
                            System.out.print(String.format("%02X%02X%02X ", btClr[0],btClr[1], btClr[2]));
                        }
                        System.out.println();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("BitmapTest Error!");
            e.printStackTrace();
        }
    }
}
