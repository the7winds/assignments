package ru.spbau.mit;

import java.io.*;
import java.util.*;

public class StringSetImpl implements StringSet, StreamSerializable {
    private static final int ALPHABET = 100;
    private static final int MAX_N    = 10000;
    private static final String END   = new Character((char) 164).toString();
    private int len = 0;
    private int trieSize = 0;
    private Queue<Integer> free;
    private int[][] counter;
    private int[][] next;


    public StringSetImpl() {
        free = new LinkedList<Integer>();
        counter = new int[MAX_N][ALPHABET];
        next = new int[MAX_N][ALPHABET];
    }

    private int getCode(char c) {
        return (int)c - (int)'A';
    }

    public boolean add(String element) {
        trieSize++;
        element = element + END;

        int cur  = 0;
        int code = 0;
        for (char c : element.toCharArray()) {
            code = getCode(c);
            counter[cur][code]++;

            if (next[cur][code] == 0) {
                next[cur][code] = (free.isEmpty() ? ++len : ((Integer)free.poll()).intValue());
            }

            cur = next[cur][code];
        }

        counter[cur][code]++;

        return (counter[cur][code] == 1 ? true : false);
    }

    public boolean contains(String element) {
        element = element + END;
        int cur = 0;

        for (char c : element.toCharArray()) {
            int code = getCode(c);

            if (counter[cur][code] == 0) {
                return false;
            }

            cur = next[cur][code];
        }

        return true;
    }

    public boolean remove(String element) {
        if (contains(element)) {
            element = element + END;
            int cur = 0;
            trieSize--;

            for (char c : element.toCharArray()) {
                int code = getCode(c);
                counter[cur][code]--;
                int tmp = next[cur][code];

                if (counter[cur][code] == 0) {
                    free.add(tmp);
                    next[cur][code] = 0;
                }

                cur = tmp;
            }
            return true;
        }
        return false;
    }

    public int size() {
        return trieSize;
    }

    public int howManyStartsWithPrefix(String prefix) {
        int cur = 0;
        int res = 0;

        for (char c : prefix.toCharArray()) {
            int code = getCode(c);

            if (counter[cur][code] == 0) {
                return 0;
            }

            res = counter[cur][code];
            cur = next[cur][code];
        }

        return res;
    }

    private void writeInt(OutputStream out, int n) throws IOException {
        byte[] bInt = new byte[4];
        bInt[0] = (byte)(n & ((1 << 8) - 1));
        bInt[1] = (byte)((n >> 8) & ((1 << 8) - 1));
        bInt[2] = (byte)((n >> 16) & ((1 << 8) - 1));
        bInt[3] = (byte)((n >> 24) & ((1 << 8) - 1));
        out.write(bInt);
    }

    public void serialize(OutputStream out) throws SerializationException {
        try {
            writeInt(out, len);

            for (int i = 0; i < len; i++)
                for (int j = 0; j < ALPHABET; j++)
                    writeInt(out, counter[i][j]);

            for (int i = 0; i < len; i++)
                for (int j = 0; j < ALPHABET; j++)
                    writeInt(out, next[i][j]);

            Object[] freeArr = free.toArray();

            writeInt(out, freeArr.length);

            for (Object a : freeArr)
                writeInt(out, (Integer)a);

            writeInt(out, trieSize);
        } catch (IOException e) {
            throw new SerializationException();
        }
    }

    private int readInt(InputStream in) throws IOException {
        byte[] bInt  = new byte[4];

        in.read(bInt);

        int res = 0;
        int pow = 1;

        for (byte b : bInt) {
            res += b * pow;
            pow <<= 8;
        }

        return res;
    }

    public void deserialize(InputStream in) throws SerializationException {
        try {
            len = readInt(in);

            for (int i = 0; i < len; i++)
                for (int j = 0; j < ALPHABET; j++)
                    counter[i][j] = readInt(in);

            for (int i = 0; i < len; i++)
                for (int j = 0; j < ALPHABET; j++)
                    next[i][j] = readInt(in);

            int l = readInt(in);
            free = new LinkedList<Integer>();

            for (int a = 0, i = 0; i < l; i++)
                free.add(readInt(in));

            trieSize = readInt(in);
        } catch (IOException e) {
            throw new SerializationException();
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof StringSetImpl) {
            StringSetImpl ref = (StringSetImpl) obj;

            if (trieSize == ref.trieSize && len == ref.len && free.equals(ref.free)) {
            
                for (int i = 0; i < len; ++i) {
                    for (int j = 0; j < ALPHABET; ++j) {
                        if (!(counter[i][j] == ref.counter[i][j] && next[i][j] == ref.next[i][j]))
                            return false;
                    }
                }
            
                return true;
            }
            else return false;
        }
        else return false;
    }
}