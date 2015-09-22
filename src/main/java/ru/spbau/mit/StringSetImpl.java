package ru.spbau.mit;

import java.io.*;
import java.util.*;

public class StringSetImpl implements StringSet, StreamSerializable {
    private static class Node implements  StreamSerializable {
        private static final int ALPH = 26;
        private static final int byteNodeLen = (2 * ALPH + 2) / 8 + ((2 * ALPH + 2) % 8 > 0 ? 1 : 0);
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

                        if (!(rf1 == null && rf1 == null || rf1.equals(rf2))) {
                            return false;
                        }

                        rf1 = upper[i];
                        rf2 = nd.upper[i];

                        if (!(rf1 == null && rf1 == null || rf1.equals(rf2))) {
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
                BitSet bitNode = new BitSet(2 * ALPH + 2);
                bitNode.set(2 * ALPH + 1, true);
                bitNode.set(cur, terminated ? 1 : 0);
                cur++;

                for (Node l : lower) {
                    bitNode.set(cur, l == null ? false : true);
                    cur++;
                }

                for (Node u : upper) {
                    bitNode.set(cur, u == null ? false : true);
                    cur++;
                }

                out.write(bitNode.toByteArray());
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
                        lower[i] = new Node();
                    }
                    cur++;
                }

                for (int i = 0; i < ALPH; i++) {
                    if (getBit(byteNode, cur)) {
                        upper[i] = new Node();
                    }
                    cur++;
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
        return root.getCount();
    }

    @Override
    public int howManyStartsWithPrefix(String prefix) {
        Node ref = root;
        Node next = null;
        for (char c : prefix.toCharArray()) {
            next = ref.getNext(c);
            if (next == null) return 0;
            ref = next;
        }
        return ref.getCount();
    }

    private static class Triple {
        Node node;
        int l = 0;
        int u = 0;
        boolean used = false;

        Triple(Node n) {
            node = n;
        }
    }

    @Override
    public void serialize(OutputStream out) {
        Stack<Triple> stack = new Stack<Triple>();
        stack.push(new Triple(root));

        while (!stack.empty()) {
            Triple cur = stack.peek();
            Node node = cur.node;

            if (!cur.used) {
                node.serialize(out);
                cur.used = true;
            }

            boolean flag = false;

            for (; cur.l < Node.ALPH; cur.l++) {
                Node e = node.lower[cur.l];
                if (e != null) {
                    cur.l++;
                    flag = true;
                    stack.push(new Triple(e));
                    break;
                }
            }

            if (flag) continue;

            for (; cur.u < Node.ALPH; cur.u++) {
                Node e = node.upper[cur.u];
                if (e != null) {
                    cur.u++;
                    flag = true;
                    stack.push(new Triple(e));
                }
            }

            if (flag) continue;

            stack.pop();
        }
    }

    @Override
    public void deserialize(InputStream in) {
        root = new Node();
        Stack<Triple> stack = new Stack<Triple>();
        stack.push(new Triple(root));

        while (!stack.empty()) {
            Triple cur = stack.peek();
            Node node = cur.node;

            if (!cur.used) {
                node.deserialize(in);
                cur.used = true;
            }

            boolean flag = false;

            for (; cur.l < Node.ALPH; cur.l++) {
                Node e = node.lower[cur.l];
                if (e != null) {
                    cur.l++;
                    flag = true;
                    stack.push(new Triple(e));
                    break;
                }
            }

            if (flag) continue;

            for (; cur.u < Node.ALPH; cur.u++) {
                Node e = node.upper[cur.u];
                if (e != null) {
                    cur.u++;
                    flag = true;
                    stack.push(new Triple(e));
                    break;
                }
            }

            if (flag) continue;

            for (Node e : node.lower) {
                node.count += (e != null ? e.count : 0);
            }

            for (Node e : node.upper) {
                node.count += (e != null ? e.count : 0);
            }

            stack.pop();
        }
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