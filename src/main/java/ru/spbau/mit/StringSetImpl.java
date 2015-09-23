package ru.spbau.mit;

import java.io.*;
import java.util.*;

public class StringSetImpl implements StringSet, StreamSerializable {
    private static class Node implements  StreamSerializable {
        private static final int ALPH = 2 * 26;
        private static final int byteNodeLen = (ALPH + 2) / 8 + ((ALPH + 2) % 8 > 0 ? 1 : 0);
        private Node[] alph = new Node[ALPH];
        private boolean terminated = false;
        private int count = 0;

        private int getCode(char c) {
            return c - 'A' + (Character.isLowerCase(c) ? + 'Z' + 1 - 'a' : 0);
        }

        public Node getNext(char c) {
            return alph[getCode(c)];
        }

        public void setTerm(boolean t) {
            terminated = t;
        }

        public boolean isTerm() {
            return terminated;
        }

        public void decCount() {
            count--;
        }

        public void incCount() {
            count++;
        }

        public int getCount() {
            return count;
        }

        public void addRef(char c) {
            alph[getCode(c)] = new Node();
        }

        public void removeRef(char c) {
            alph[getCode(c)] = null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Node) {
                Node nd = (Node)obj;
                if (count == nd.count && terminated == nd.terminated) {
                    for (int i = 0; i < ALPH; i++) {
                        Node rf1 = alph[i];
                        Node rf2 = nd.alph[i];

                        if (!(rf1 == null && rf2 == null || rf1 != null && rf1.equals(rf2))) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return  false;
        }

        @Override
        public void serialize(OutputStream out) {
            try {
                int cur = 0;
                BitSet bitNode = new BitSet(ALPH + 2);
                bitNode.set(ALPH + 1, true);
                bitNode.set(cur, terminated ? 1 : 0);
                cur++;

                for (Node a : alph) {
                    bitNode.set(cur, a != null);
                    cur++;
                }

                out.write(bitNode.toByteArray());

                for (Node a : alph) {
                    if (a != null) {
                        a.serialize(out);
                    }
                }
            } catch (IOException e) {
                throw new SerializationException();
            }
        }

        boolean getBit(byte[] arr, int i ) {
            return ((arr[i / 8] >> (i % 8)) & 1) == 1;
        }

        @Override
        public void deserialize(InputStream in) {
            try {
                byte[] byteNode = new byte[byteNodeLen];

                in.read(byteNode);

                int cur = 0;

                terminated = getBit(byteNode, cur);
                cur++;

                if (terminated) {
                    count++;
                }

                for (int i = 0; i < ALPH; i++) {
                    if (getBit(byteNode, cur)) {
                        alph[i] = new Node();
                    }
                    cur++;
                }

                for (Node a : alph) {
                    if (a != null) {
                        a.deserialize(in);
                        count += a.count;
                    }
                }
            } catch (IOException e) {
                throw new SerializationException();
            }
        }

        @Override
        public int hashCode() {
            final int magic = 37;
            int hash = 1;

            for (int i = 0; i < ALPH; i++) {
                hash += hash * magic + (alph[i] == null ? 0 : i);
            }

            return hash;
        }
    }

    private Node root = new Node();

    @Override
    public boolean add(String element) {
        if (!contains(element)) {
            Node ref = root;
            Node next = null;
            ref.incCount();
            for (char c : element.toCharArray()) {
                next = ref.getNext(c);
                if (next == null) {
                    ref.addRef(c);
                    next = ref.getNext(c);
                }
                ref = next;
                ref.incCount();
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
            ref.decCount();
            for (char c : element.toCharArray()) {
                pref = ref;
                ref = ref.getNext(c);
                ref.decCount();
                if (ref.getCount() == 0) {
                    pref.removeRef(c);
                    break;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return root.getCount();
    }

    Node folowString(String element) {
        Node ref = root;
        Node next = null;
        for (char c : element.toCharArray()) {
            next = ref.getNext(c);
            if (next == null) {
                return null;
            }
            ref = next;
        }

        return ref;
    }

    @Override
    public boolean contains(String element) {
        Node node = folowString(element);
        return (node != null && node.isTerm());
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        Node node = folowString(prefix);
        return (node != null ? node.getCount() : 0);
    }

    @Override
    public void serialize(OutputStream out) {
        root.serialize(out);
    }

    @Override
    public void deserialize(InputStream in) {
        root = new Node();
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