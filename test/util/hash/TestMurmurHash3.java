package util.hash;

import junit.framework.TestCase;

import java.nio.charset.Charset;
import java.util.Random;

/**
 * @author yonik
 */
public class TestMurmurHash3 extends TestCase {

  public void testCorrectValues() throws Exception {
    byte[] bytes = "Now is the time for all good men to come to the aid of their country".getBytes("UTF-8");
    int hash=0;
    for (int i=0; i<bytes.length; i++) {
      hash = hash*31+(bytes[i]&0xff);
      bytes[i] = (byte)hash;
    }

    // test different offsets.
    for (int offset = 0; offset<10; offset++) {
      byte[] arr = new byte[bytes.length + offset];
      System.arraycopy(bytes, 0, arr, offset, bytes.length);
      for (int len=0; len<bytes.length; len++) {
        int h = MurmurHash3.murmurhash3_x86_32(arr, offset, len, len);
        assertEquals(answers[len], h);
      }
    }
  }

  static int[] answers = new int[] {0x0,0xcf9ce026,0x7b1ebceb,0x8a59e474,0xcf337f94,0x8b678f66,0x813ff5a2,0x1c2f4b2b,0xa6fcba77,0xe658f908,0x9f2656af,0x826b85ca,0xebb6ceca,0x24c4112c,0x66eff5b0,0xa9aca7d5,0xf7f04d03,0x9d781105,0x6dcde4f3,0x69edd8a8,0x5cdcd417,0x18d67f6,0xea040c90,0xdf70ea4a,0x8fb349e6,0x79a89b03,0x7ef9fc34,0x6017f692,0x5be02058,0x9e3986f9,0x8fa6dd28,0x6733b993,0x26230d32,0x92051d69,0x8d6f37f7,0xa1653103,0x8491c23f,0x2e8f59ce,0x5ae9461e,0xfe286e6,0x844e6959,0x87e9065d,0xe302e21c,0x1b3b3296,0xd29849c9,0x4e625f26,0xa8c35ac0,0x71335a06,0xfd256d8f,0x4e5eb258,0x4e2320d1,0xba2e9832,0xb00df8eb,0xbd87594d,0x83b6dce3,0xcf8646d0,0x7e79f2e2,0xd41fcd97,0x556a93,0x4419437b,0x39aa0e4e,0x43a57251,0x9430922f,0xd784b08f,0xa2772512,0xa2a6ee4b,0x9cb1abae,0xebd2bef0};

  private final Charset utf8Charset = Charset.forName("UTF-8");

  private void doString(String s) {
    doString(s, 0, 0);
  }

  private void doString(String s, int pre, int post) {
    byte[] utf8 = s.getBytes(utf8Charset);
    int hash1 = MurmurHash3.murmurhash3_x86_32(utf8, pre, utf8.length-pre-post, 123456789);
    int hash2 = MurmurHash3.murmurhash3_x86_32(s, pre, s.length()-pre-post, 123456789);
    if (hash1 != hash2) {
      System.out.println(s);
      // second time for debugging...
      hash2 = MurmurHash3.murmurhash3_x86_32(s, pre, s.length()-pre-post, 123456789);
    }
    assertEquals(hash1, hash2);
  }

  public void testStringHash() {
    doString("hello!");
    doString("ABCD");
    doString("\u0123");
    doString("\u2345");
    doString("\u2345\u1234");

    Random r = new Random();
    StringBuilder sb = new StringBuilder(40);
    for (int i=0; i<100000; i++) {
      sb.setLength(0);
      int pre = r.nextInt(3);
      int post = r.nextInt(3);
      int len = r.nextInt(16);

      for (int j=0; j<pre; j++) {
        int codePoint = r.nextInt(0x80);
        sb.appendCodePoint(codePoint);
      }

      for (int j=0; j<len; j++) {
        int codePoint;
        do {
          int max = 0;
          switch (r.nextInt() & 0x3) {
            case 0: max=0x80; break;   // 1 UTF8 bytes
            case 1: max=0x800; break;  // up to 2 bytes
            case 2: max=0xffff+1; break; // up to 3 bytes
            case 3: max=Character.MAX_CODE_POINT+1; // up to 4 bytes
          }

          codePoint = r.nextInt(max);
        }  while (codePoint < 0xffff && (Character.isHighSurrogate((char)codePoint) || Character.isLowSurrogate((char)codePoint)));

        sb.appendCodePoint(codePoint);
      }

      for (int j=0; j<post; j++) {
        int codePoint = r.nextInt(0x80);
        sb.appendCodePoint(codePoint);
      }

      String s = sb.toString();
      String middle = s.substring(pre, s.length()-post);

      doString(s);
      doString(middle);
      doString(s, pre, post);
    }

  }

}


// Here is my C++ code to produce the list of answers to check against.
/*****************
 #include <iostream>
 #include "MurmurHash3.h"
 using namespace std;

 int main(int argc, char** argv) {
   char* val = strdup("Now is the time for all good men to come to the aid of their country");
   int max = strlen(val);
   int hash=0;
   for (int i=0; i<max; i++) {
     hash = hash*31 + (val[i] & 0xff);
      // we want to make sure that some high bits are set on the bytes
      // to catch errors like signed vs unsigned shifting, etc.
     val[i] = (char)hash;
   }
   for (int len=0; len<max; len++) {
     int result;
     MurmurHash3_x86_32(val, len, len, &result);
     cout << "0x" << hex << result << ",";
   }
   cout << endl;
 }
***************/
