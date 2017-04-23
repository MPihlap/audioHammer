package server;

import java.nio.ByteBuffer;

/**
 * Created by Meelis on 23/04/2017.
 */
public class BufferTest {
    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        for (int i = 0; i < 10; i++) {
            byteBuffer.put((byte) i);
        }
        for (int i = 0; i < 10; i++) {
            System.out.println(byteBuffer.get(i));
        }
        byte[] endBytes = new byte[4];
        int position = byteBuffer.position();
        byteBuffer.position(10-4);
        byteBuffer.get(endBytes,0,4);
        for (byte b : endBytes) {
            System.out.println(b);
        }
    }
}
