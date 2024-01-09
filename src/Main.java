import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        FullCompiler compiler = new FullCompiler("Input\\", "Output\\");
        compiler.runCompiler();
    }
}