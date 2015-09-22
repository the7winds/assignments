package ru.spbau.mit;

import java.io.*;
import java.util.*;

public class StringSetImpl implements StringSet, StreamSerializable {
    private static class Node implements  StreamSerializable {
        private static final int ALPH = 26;
        private Node[] upper = new Node[ALPH];
        private Node[] lower = new Node[ALPH];
        private boolean terminated = false;
        private int count = 0;

        public Node getNext(char c) {
            int idx = 0;
            if (Character.isUpperCase(c)) {
                idx = c - 'A';
                return upper[idx];
            } else {
                idx = c - 'a';
                return lower[idx];
            }
        }

        public void setTerm(boolean t) {
            terminated = t;
        }

        public boolean isTerm() {
            return terminated;
        }

        public void decCnt() {
            count--;
        }

        public void incCtn() {
            count++;
        }

        public int getCnt() {
            return count;
        }

        public void addRef(char c) {
            int idx = 0;
            if (Character.isUpperCase(c)) {
                idx = c - 'A';
                upper[idx] = new Node();
            } else {
                idx = c - 'a';
                lower[idx] = new Node();
            }
        }

        public void removeRef(char c) {
            int idx = 0;
            if (Character.isUpperCase(c)) {
                idx = c - 'A';
                upper[idx] = null;
            } else {
                idx = c - 'a';
                lower[idx] = null;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Node) {
                Node nd = (Node)obj;
                if (count == nd.count && terminated == nd.terminated) {
                    for (int i = 0; i < ALPH; i++) {
                        Node rf1 = lower[i];
                        Node rf2 = nd.lower[i];
                        if (!(rf1 == null && rf1 == null || rf1.equals(rf2)))
                            return false;
                        rf1 = upper[i];
                        rf2 = nd.upper[i];
                        if (!(rf1 == null && rf1 == null || rf1.equals(rf2)))
                            return false;
                    }
                    return true;
                }
            }
            return  false;
        }

        private void writeInt(OutputStream out, int n) throws IOException {
            byte[] bInt = new byte[4];
            bInt[0] = (byte)n;
            bInt[1] = (byte)(n >> 8);
            bInt[2] = (byte)(n >> 16);
            bInt[3] = (byte)(n >> 24);
            out.write(bInt);
        }

        @Override
        public void serialize(OutputStream out) {
            try {
                writeInt(out, terminated ? 1 : 0);
                writeInt(out, count);
                for (int i = 0; i < ALPH; i++) {
                    writeInt(out, lower[i] == null ? 0 : 1);
                }
                for (int i = 0; i < ALPH; i++) {
                    writeInt(out, upper[i] == null ? 0 : 1);
                }
                for (int i = 0; i < ALPH; i++) {
                    if (lower[i] != null) lower[i].serialize(out);
                }
                for (int i = 0; i < ALPH; i++) {
                    if (upper[i] != null) upper[i].serialize(out);
                }
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

        @Override
        public void deserialize(InputStream in) {
            try {
                terminated = (readInt(in) == 1);
                count = readInt(in);
                for (int i = 0; i < ALPH; i++) {
                    lower[i] = (readInt(in) == 1 ? new Node() : null);
                }
                for (int i = 0; i < ALPH; i++) {
                    upper[i] = (readInt(in) == 1 ? new Node() : null);
                }
                for (int i = 0; i < ALPH; i++) {
                    if (lower[i] != null) lower[i].deserialize(in);
                }
                for (int i = 0; i < ALPH; i++) {
                    if (upper[i] != null) upper[i].deserialize(in);
                }
            } catch (IOException e) {
                throw new SerializationException();
            }
        }

        @Override
        public int hashCode() {
            final int magic = 37;
            int hash = 1;
            for (int i = 0; i < ALPH; i++)
                hash += hash * magic + (lower[i] == null ? 0 : i);
            for (int i = 0; i < ALPH; i++)
                hash += hash * magic + (upper[i] == null ? 0 : i);
            return hash;
        }
    }

    private Node root = new Node();

    @Override
    public boolean add(String element) {
        if (!contains(element)) {
            Node ref = root;
            Node next = null;
            ref.incCtn();
            for (char c : element.toCharArray()) {
                next = ref.getNext(c);
                if (next == null) {
                    ref.addRef(c);
                    next = ref.getNext(c);
                }
                ref = next;
                ref.incCtn();
            }
            ref.setTerm(true);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(String element) {
        if (contains(element)) {
            Node ref = root;
            Node pref = null;
            ref.decCnt();
            for (char c : element.toCharArray()) {
                pref = ref;
                ref = ref.getNext(c);
                ref.decCnt();
                if (ref.getCnt() == 0) {
                    pref.removeRef(c);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(String element) {
        Node ref = root;
        Node next = null;
        for (char c : element.toCharArray()) {
            next = ref.getNext(c);
            if (next == null) return false;
            ref = next;
        }
        return (ref.isTerm() ? true : false);
    }

    @Override
    public int size() {
        return root.getCnt();
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        Node ref = root;
        Node next = null;
        for (int i = 0; i < prefix.length(); i++) {
            next = ref.getNext(prefix.charAt(i));
            if (next == null) return 0;
            ref = next;
        }
        return ref.getCnt();
    }

    @Override
    public void serialize(OutputStream out) {
        root.serialize(out);
    }

    @Override
    public void deserialize(InputStream in) {
        root.deserialize(in);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StringSetImpl) {
            StringSetImpl ref = (StringSetImpl)obj;
            return root.equals(ref.root);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return root.hashCode();
    }
}