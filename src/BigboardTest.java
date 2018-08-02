import org.junit.jupiter.api.Test;


public class BigboardTest {
    @Test
    public void test() {
        Bigboard b = new Bigboard(8, 9);
        b = b.or(9);
        System.out.println(b + "\n");
        b = b.left(64);
        System.out.println(b + "\n");
        b = b.right(64);
        System.out.println(b + "\n");
    }
}