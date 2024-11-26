package intercom;


import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockMacroProcessor;
import org.asciidoctor.extension.Name;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@Name("slide")
public class SlideBlockMacroProcessor extends BlockMacroProcessor {
    final String outputPath;

    public SlideBlockMacroProcessor(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public StructuralNode process(StructuralNode parent, String s, Map<String, Object> map) {
        String fn = map.get("fn") != null ? map.get("fn").toString() + ".html" : null;
        fn = outputPath + "/" + fn;

        if (new File(fn).exists()) {
            String content;
            try (FileInputStream fileInputStream = new FileInputStream(fn)) {
                content = new String(fileInputStream.readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return createBlock(parent, "pass", content);
        } else {
            System.out.println("File not found: " + fn);
        }
        return null;
    }

}
