package intercom;

import grails.util.Triple;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.Treeprocessor;

import java.util.List;

public class MenuTreeprocessor extends Treeprocessor {

    private final List<Triple<String, String, Integer>> menus;

    public MenuTreeprocessor(List<Triple<String, String, Integer>> menus) {
        this.menus = menus;
    }

    @Override
    public Document process(Document document) {
        for (StructuralNode n : document.getBlocks()) {
            if (n != null) {
                if ("section".equals(n.getContext())) {
                    Section s = (Section) n;
                    menus.add(new Triple<>(s.getTitle(), s.getId(), s.getLevel()));
                    for (StructuralNode n1 : s.getBlocks()) {
                        if (n1 != null) {
                            if ("section".equals(n1.getContext())) {
                                Section s1 = (Section) n1;
                                menus.add(new Triple<>(s1.getTitle(), s1.getId(), s1.getLevel()));
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
