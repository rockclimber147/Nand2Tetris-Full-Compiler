import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import jackCompiler.*;
import vmTranslator.*;
import hackAssembler.*;

public class FullCompiler {
    private final String inputDirectory;
    private final String outputDirectory;
    private String fileName;

    public FullCompiler(String inputDirectory, String outputDirectory){
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
    }

    public void runCompiler() throws IOException {
        File[] inputItems = new File(inputDirectory).listFiles();
        if (inputItems == null){
            System.out.println("No files detected!");
            return;
        }
        for (File toTranslate: inputItems) {
            fileName = toTranslate.getName();
            new File(outputDirectory + fileName + "\\vm").mkdirs();
            new File(outputDirectory + fileName + "\\xml").mkdirs();
            new File(outputDirectory + fileName + "\\asm").mkdirs();
            new File(outputDirectory + fileName + "\\hack").mkdirs();
            compileInputDirectory(toTranslate);
            translateVMDirectory();
            translateASMDirectory();
        }
    }
    public void compileInputDirectory(File toTranslate) throws IOException {
            File[] filesList; // for use with CompilationEngine

            if (!toTranslate.exists()) {
                throw new FileNotFoundException("Not found");
            }
            if (toTranslate.isDirectory()) {
                try {
                    Files.createDirectory(Path.of(outputDirectory + toTranslate.getName())); // create output directory
                } catch (FileAlreadyExistsException ignored) {
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                FileFilter jackFiles = (file) -> (file.getName().endsWith(".jack"));
                filesList = toTranslate.listFiles(jackFiles);


            } else {
                filesList = new File[]{toTranslate};
            }

            if (filesList != null) {
                CompilationEngine engine = new CompilationEngine();
                for (File f : filesList) {
                    System.out.println(f.getPath());
                    engine.init(f.getPath(), inputDirectory, outputDirectory);
                    engine.setGenerateTokenXML();
                    engine.compileClass();
                    engine.close();
                }
            }

    }
    public void translateVMDirectory() throws IOException {
        File toTranslate = new File(outputDirectory + fileName + "\\vm"); //Set up file
        if (!toTranslate.exists()){
            throw new FileNotFoundException("File not found");
        }

        File[] filesList = toTranslate.listFiles();

        CodeWriter assemblyCodeWriter = new CodeWriter(fileName.replace(".vm", ""));

        if (filesList == null){
            throw new NullPointerException("No files were found");
        }

        assemblyCodeWriter.writeInit(); // Start by writing bootstrap code

        for (File vm:filesList) {
            // For each file in the file list, make a new parser
            VMParser vmCodeParser = new VMParser(vm);
            vmCodeParser.advance();

            // Parse each file and translate
            while (vmCodeParser.hasMoreLines()) {

                boolean currentLineIsACommand = false;

                // Write each line as a comment, break loop when a command is found.
                while (!currentLineIsACommand) {
                    String currentLine = vmCodeParser.getCurrentLine();
                    assemblyCodeWriter.writeAsComment(currentLine);
                    if (vmCodeParser.isValidCommand(currentLine)){
                        currentLineIsACommand = true;
                    } else {
                        vmCodeParser.advance();
                    }
                }

                // Handle writing commands
                VMParser.command currentCommand = vmCodeParser.commandType();
                if (currentCommand == VMParser.command.C_PUSH) {
                    assemblyCodeWriter.writePushPop("push", vmCodeParser.arg1(), vmCodeParser.arg2());
                } else if (currentCommand == VMParser.command.C_POP) {
                    assemblyCodeWriter.writePushPop("pop", vmCodeParser.arg1(), vmCodeParser.arg2());
                } else if (currentCommand == VMParser.command.C_LABEL) {
                    assemblyCodeWriter.writeLabel(vmCodeParser.arg1());
                } else if (currentCommand == VMParser.command.C_GOTO) {
                    assemblyCodeWriter.writeGoTo(vmCodeParser.arg1());
                } else if (currentCommand == VMParser.command.C_IF_GOTO) {
                    assemblyCodeWriter.writeIf(vmCodeParser.arg1());
                } else if (currentCommand == VMParser.command.C_FUNCTION) {
                    assemblyCodeWriter.writeFunction(vmCodeParser.arg1(), vmCodeParser.arg2());
                }else if (currentCommand == VMParser.command.C_CALL) {
                    assemblyCodeWriter.writeCall(vmCodeParser.arg1(), vmCodeParser.arg2());
                }else if (currentCommand == VMParser.command.C_RETURN) {
                    assemblyCodeWriter.writeReturn();
                }else {
                    assemblyCodeWriter.writeArithmetic(vmCodeParser.arg1());
                }
                // Advance when command is written
                vmCodeParser.advance();
            }
            //At the end of the file write some blank space
            assemblyCodeWriter.writeBlankSpace();
        }

        assemblyCodeWriter.close();
        System.out.println("DONE");
    }

    public void translateASMDirectory() throws IOException {
        HackAssembler h = new HackAssembler(fileName, outputDirectory + fileName + "\\asm\\", outputDirectory, 1);
        h.firstPass();
        h.secondPass();
        h.closeWriters();
    }
}
