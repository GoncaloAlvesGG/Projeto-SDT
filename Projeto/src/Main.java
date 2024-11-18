public class Main {
    public static void main(String[] args) throws Exception {

        // Lider
        ElementLider leaderNode = new ElementLider();
        //Esperar 1 segundo para os elementos começarem antes do lider
        Thread.sleep(1000);
        //Não Líder
        Element memberNode1 = new Element(false);
        memberNode1.start();
        Element memberNode2 = new Element(false);
        memberNode2.start();
        leaderNode.start();
    }
}
