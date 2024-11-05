public class Main {

    public static void main(String[] args) throws InterruptedException {
        //Não Líder
        Node memberNode1 = new Node(false);
        memberNode1.start();
        Node memberNode2 = new Node(false);
        memberNode2.start();
        //Esperar 1 segundo para os elementos começarem antes do lider
        Thread.sleep(1000);
        // Lider
        Node node = new Node(true);
        node.start();
    }


}
