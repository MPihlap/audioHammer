package server;

import java.nio.ByteBuffer;


/**
 * Created by Meelis on 23/04/2017.
 */
public class BufferTest {
    private static boolean isFinished(ByteBuffer byteBuffer, int nBytes) {
        byte[] endBytes = new byte[nBytes];
        int startPosition = byteBuffer.position();
        System.out.println(byteBuffer.position() - nBytes - 1);
        byteBuffer.position(startPosition - nBytes);
        byteBuffer.get(endBytes, 0, nBytes);
        System.out.print("Endbytes:");
        for (int i = 0; i < nBytes; i++) {
            System.out.print(endBytes[i]);
            if (endBytes[i] != 1)
                return false;
        }
        byteBuffer.position(startPosition);
        return true;
    }

    private static boolean isFinished(byte[] bytes, int nBytes) {
        if (bytes.length < 8)
            return false;
        byte[] endBytes = new byte[nBytes];
        for (int i = 0; i < 8; i++) {
            System.out.println(bytes.length);
            System.out.println(bytes.length - 1 - i);
            endBytes[i] = bytes[bytes.length - 1 - i];
        }
        System.out.print("Endbytes: ");
        for (int i = 0; i < nBytes; i++) {
            System.out.print(endBytes[i]);
            if (endBytes[i] != 1)
                return false;
        }
        return true;
    }

    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(18);
        byte[] bytes = new byte[]{0,1,2,3,4,5,6,7,8,9,1,1,1,1,1,1,1,1};
        for (int i = 0; i < 10; i++) {
            byteBuffer.put((byte) i);

        }
        byteBuffer.put(new byte[]{1, 1, 1, 1, 1, 1, 1, 1}, 0, 8);
        System.out.println("Finished:"+isFinished(byteBuffer,8));
        System.out.println("Finished:"+isFinished(bytes,8));
        for (int i = 0; i < 10; i++) {
            System.out.println(byteBuffer.get(i));
        }
        byte[] endBytes = new byte[4];
        int position = byteBuffer.position();
        byteBuffer.position(10 - 4);
        byteBuffer.get(endBytes, 0, 4);
        for (byte b : endBytes) {
            System.out.println(b);
        }
    }
}
