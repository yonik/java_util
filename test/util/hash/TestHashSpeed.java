package util.hash;

import junit.framework.TestCase;


/**
 * @author yonik
 */
public class TestHashSpeed {

  public static void main(String[] args) {
    int arg = 0;
    int size = Integer.parseInt(args[arg++]);
    int iter = Integer.parseInt(args[arg++]);

    byte[] arr = new byte[size];
    for (int i=0; i<arr.length; i++) {
      arr[i] = (byte)(i & 0xff);
    }

    int ret = 0;
    long start = System.currentTimeMillis();
    for (int i = 0; i<iter; i++) {
      ret += MurmurHash3.murmurhash3_x86_32(arr, 0, arr.length, i);
    }
    long end = System.currentTimeMillis();

    System.out.println("result="+ ret + " throughput = " + 1000 * ((double)size)*iter/(end-start) );

  }

}
