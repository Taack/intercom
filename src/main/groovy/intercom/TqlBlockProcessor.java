package intercom;

import grails.util.Pair;
import groovy.transform.CompileStatic;
import org.asciidoctor.ast.ContentModel;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Contexts;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Reader;
import taack.domain.TaackJdbcService;
import taack.jdbc.TaackJdbcError;
import taack.jdbc.common.tql.listener.TQLTranslator;

import java.util.List;
import java.util.Map;

@CompileStatic
@Name("tql")
//@Contexts({Contexts.PARAGRAPH})
@Contexts({Contexts.LISTING})
//@ContentModel(ContentModel.SIMPLE)
@ContentModel(ContentModel.COMPOUND)
class TqlBlockProcessor extends BlockProcessor {

    final TaackJdbcService taackJdbcService;

    TqlBlockProcessor(TaackJdbcService taackJdbcService) {
        this.taackJdbcService = taackJdbcService;
    }

    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        String content = reader.read();
        System.out.println("TQL " + content);
        TQLTranslator t = null;
        try {
            t = TaackJdbcService.translatorFromTql(content);
        } catch (TaackJdbcError e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        if (t.errors.isEmpty()) {
            Pair<List<Object[]>, Long> p = taackJdbcService.listFromTranslator(t, 20, 0);
            return createBlock(parent, "paragraph", p.getaValue().toString(), attributes);
        } else {
            return createBlock(parent, "paragraph", t.errors.toString(), attributes);
        }
    }
}
