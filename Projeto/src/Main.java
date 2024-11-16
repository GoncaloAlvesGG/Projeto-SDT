public class Main {
    public static void main(String[] args) throws Exception {
        //Não Líder
        Client memberNode1 = new Client(false);
        memberNode1.start();
        Client memberNode2 = new Client(false);
        memberNode2.start();
        //Esperar 1 segundo para os elementos começarem antes do lider
        Thread.sleep(1000);
        // Lider
        Server leaderNode = new Server();
        leaderNode.start();
    }
}
