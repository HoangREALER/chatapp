package Server;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class FileInfo implements Serializable {
    private static int count_file = 0;
    private int file_id;
    private Set<Integer> peer_id = new HashSet<Integer>();
    private String fileName;

    public FileInfo(int peer_id, String fileName) {
        setID(++count_file);
        this.peer_id.add(peer_id);
        this.fileName = fileName;
    }
    public void addPeer(int peer_id) {
        this.peer_id.add(peer_id);
    }
    private void setID(int id) {
        this.file_id = id;
    }

    public int getFileId() {
        return this.file_id;
    }

    public String getFileName() {
        return this.fileName;
    }

    public Set<Integer> getPeerId() {
        return this.peer_id;
    }
}