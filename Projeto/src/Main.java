public class Main {
    public static void main(String[] args) throws Exception {

        // Lider
        Server leaderNode = new Server();
        //Esperar 1 segundo para os elementos começarem antes do lider
        Thread.sleep(1000);
        //Não Líder
        Client memberNode1 = new Client(false);
        memberNode1.start();
        Client memberNode2 = new Client(false);
        memberNode2.start();

        leaderNode.start();
    }
}
