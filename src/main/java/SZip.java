import javax.swing.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * Created by Daniel on 2014-03-07.
 */
public class SZip {
    private TreeMap<Character,Integer> frequencyTabel = new TreeMap<>();
    private HashMap<String,Character> codeTable= new HashMap<>();
    private File file;
    private Charset encoding;
    private char[] chars;
    private PriorityQueue<Node> forest;

    public SZip(File file, Charset encoding) {
        this.file = file;
        this.encoding = encoding;
        this.forest = new PriorityQueue();
        initFrequencyTabel();
        buildTree();
    }
    public SZip(TreeMap<Character,Integer> frequencyTabel, Charset encoding){
        this.frequencyTabel = frequencyTabel;
        this.encoding = encoding;
        this.forest = new PriorityQueue();
        buildTree();
    }
    private void buildTree() {
        for(Map.Entry<Character, Integer> entry: frequencyTabel.entrySet()){
            forest.add(new Node(entry.getValue(),entry.getKey()));
        }
        while(forest.size()>1){
            Node n1 = forest.poll(); //smallest
            Node n2 = forest.poll(); //second smallest
            int weight = n1.getWeight()+n2.getWeight();
            Node nonLeaf = new Node(weight,n1,n2);
            forest.add(nonLeaf);
        }
        buildCode(forest.peek(), "");
    }

    private String buildCode( Node x, String s) {
        if (x.isLeaf()) {
            x.code = s;
            codeTable.put(s, x.data);
            return s;
        }else {
           s =  buildCode(x.left, s + '0');
           s =  buildCode(x.right, s + '1');
        }
        return s;
    }

    private boolean initFrequencyTabel() {
        try {
            String fileAsText = readFile(file.getAbsolutePath(),encoding);
            chars = fileAsText.toCharArray();
            int frequency;
            for (int i = 0; i < chars.length; i++) {
                Character current = chars[i];
                frequency = frequencyTabel.containsKey(current) ? frequencyTabel.get(current)+1:1;
                frequencyTabel.put(current, frequency);
            }
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,"Could not read file"+e.getMessage(), "File read error",JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    private String getCode(char ch){
        for(Map.Entry<String,Character> entry : codeTable.entrySet()){
            if(entry.getValue().equals(ch))
                return entry.getKey();
        }
        return null;
    }
    private String buildOutput(Node x, String s) {
        try{
            if (x.isLeaf())
                    return s+x.code;
             s =buildOutput(x.left, s);
             s =buildOutput(x.right, s);
        }catch (Exception e){
            e.printStackTrace();
        }
        return s;
    }

    public static boolean zip(FileSelect fileDialog){
        try {
            FileOutputStream saveFile = new FileOutputStream(fileDialog.getSelectedFile().getName()+".szip");
            ObjectOutputStream oos = new ObjectOutputStream(saveFile);
            SZip cFile = new SZip(fileDialog.getSelectedFile(), StandardCharsets.UTF_8);
            String output = cFile.buildOutput(cFile.forest.peek(), "");
            oos.writeObject(cFile.frequencyTabel);
            oos.writeObject(output);
            return true;
        }catch(FileNotFoundException fnfe){
            JOptionPane.showMessageDialog(null,"Kunde inte hitta filen, eller så har den fel rättigheter");
        }catch (IOException ioe){
            JOptionPane.showMessageDialog(null,"Något io problem"+ioe.getMessage());
        }catch (Exception e ){
            JOptionPane.showMessageDialog(null,"Något random problem"+e.getMessage());
        }
        return false;
    }
    public static void main(String[] args) {

    }
    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }
    private String findNextCode(String str){
        for (int i = 0; i <= str.length(); i++) {
            String substr = str.substring(0,i);
            if(codeTable.containsKey(substr)){
                System.out.print(codeTable.get(substr));
                return str.substring(i);
            }
        }
        return null;
    }
    public static boolean unzip(FileSelect fileSelect){
        try{
            FileInputStream openFile = new FileInputStream(fileSelect.getSelectedFile().getName());
            ObjectInputStream ois = new ObjectInputStream(openFile);
            TreeMap<Character,Integer> freqTable = (TreeMap<Character, Integer>) ois.readObject();
            SZip zip = new SZip(freqTable,StandardCharsets.UTF_8);
            System.out.print(zip.codeTable);
            String bits = (String)ois.readObject();
            System.out.println(bits);
            while(bits != null){
                bits = zip.findNextCode(bits);

            } 
            return true;
        }catch(Exception e){
            JOptionPane.showMessageDialog(null,"Could not read file"+e.getMessage());
        }
        return false;
    }

    private class Node implements Comparable {
        private int weight;
        private char data;
        private String code;
        private Node left;
        private Node right;

        private Node(int weight, char data) {
            this.weight = weight;
            this.data = data;
        }
        private Node(int weight,Node left,Node right){
            this.weight = weight;
            this.left = left;
            this.right = right;
        }
        public int getWeight() {
            return weight;
        }
        @Override
        public String toString() {
            return String.valueOf(data)+" har freg "+frequencyTabel.get(data)+" och kod "+code;
        }

        @Override
        public int compareTo(Object other) {
            Node n = (Node)other;
            return weight-n.weight;
        }

        public boolean isLeaf() {
            return left==null && right==null;
        }
    }

}