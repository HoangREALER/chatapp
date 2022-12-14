package Server;

class User {
    public static int id = 0;
    private int peer_id;
    private String username;
    private String host;
    private String port;

    public User(String username, String host, String port) {
        setId(++id);
        this.username = username;
        this.host = host;
        this.port = port;
    }
    private void setId (int id) {
        peer_id = id;
    }
    public String getUsername() {
        return this.username;
    }

    public String getPort() {
        return this.port;
    }

    public String getHost() {
        return this.host;
    }

    public int getId() {
        return this.peer_id;
    }
}
