package intercom;

import diagram.SvgDiagramRender;
import diagram.scene.BarDiagramScene;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.asciidoctor.ast.*;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Contexts;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Reader;
import taack.domain.TaackJdbcService;
import taack.jdbc.TaackANTLRErrorListener;
import taack.jdbc.TaackJdbcError;
import taack.jdbc.common.TaackResultSetOuterClass;
import taack.jdbc.common.tql.gen.TDLLexer;
import taack.jdbc.common.tql.gen.TDLParser;
import taack.jdbc.common.tql.listener.TDLTranslator;
import taack.jdbc.common.tql.listener.TQLTranslator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

@Name("tql")
@Contexts({Contexts.LISTING})
@ContentModel(ContentModel.RAW)
class TqlBlockProcessor extends BlockProcessor {

    final TaackJdbcService taackJdbcService;

    TqlBlockProcessor(TaackJdbcService taackJdbcService) {
        this.taackJdbcService = taackJdbcService;
    }



    private Table tdlTable(StructuralNode parent, TaackResultSetOuterClass.TaackResultSet res, int lineCount, SortedMap<String, String> cols) {
        Table table = createTable(parent);

        Column[] columns = new Column[res.getColumnsCount()];
        Row rh = createTableRow(table);
        for (int i = 0; i < res.getColumnsCount(); i++) {
            columns[i] = createTableColumn(table, i);
            String name = res.getColumns(i).getName();
            if (cols != null && cols.containsKey(name)) {
                name = cols.get(name);
            }
            rh.getCells().add(createTableCell(columns[i], name));
        }
        table.getHeader().add(rh);

        for (int i = 0; i < lineCount; i++) {
            Row r = createTableRow(table);
            for (int j = 0; j < res.getColumnsCount(); j++) {
                int index = i * res.getColumnsCount() + j;
                Cell c = null;
                switch (res.getColumns(j).getJavaType()) {
                    case DATE -> c = createTableCell(columns[j], res.getCells(index).getDateValue() + "");
                    case LONG -> c = createTableCell(columns[j], res.getCells(index).getLongValue() + "");
                    case BIG_DECIMAL -> c = createTableCell(columns[j], res.getCells(index).getBigDecimal() + "");
                    case STRING -> {
                        String cc = res.getCells(index).getStringValue();
                        if (cc.startsWith("<")) {
                            cc = "\n+++\n" + cc + "\n+++\n";
                            c = createTableCell(columns[j], cc);
                        } else c = createTableCell(columns[j], cc + "");
                    }
                    case BOOL -> c = createTableCell(columns[j], res.getCells(index).getBoolValue() + "");
                    case BYTE -> c = createTableCell(columns[j], res.getCells(index).getByteValue() + "");
                    case SHORT -> c = createTableCell(columns[j], res.getCells(index).getShortValue() + "");
                    case INT -> c = createTableCell(columns[j], res.getCells(index).getIntValue() + "");
                    case BYTES -> c = createTableCell(columns[j], res.getCells(index).getBytesValue() + "");
                    case UNRECOGNIZED -> System.out.println("|UNRECOGNIZED " + columns[j]);
                }
                r.getCells().add(c);
            }
            table.getBody().add(r);
        }
        return table;
    }

    @Override
    public Object process(StructuralNode parent, Reader reader, Map<String, Object> attributes) {
        String content = reader.read();
        String[] contentSplit = content.split("--");
        String tql = contentSplit[0];
        System.out.println("TQL " + tql);
        TQLTranslator t;
        try {
            t = TaackJdbcService.translatorFromTql(tql);
        } catch (TaackJdbcError e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        if (t.errors.isEmpty()) {
            TaackResultSetOuterClass.TaackResultSet res = taackJdbcService.protoFromTranslator(t, 20, 0);
            int lineCount = res.getCellsCount() / res.getColumnsCount();
            if (contentSplit.length > 1) {
//            Block outterBlock = createBlock(parent, "div", "");
                for (int i = 1; i < contentSplit.length; i++) {
                    String tdl = contentSplit[i].strip();
                    System.out.println("TDL " + tdl);
                    TDLTranslator tdlTranslator = translatorFromTdl(tdl);
                    if (tdlTranslator.kind == TDLTranslator.Kind.TABLE) {
                        return tdlTable(parent, res, lineCount, tdlTranslator.cols);
                    } else {
                        return tdlBarChart(parent, res, lineCount, tdlTranslator.cols);
                    }
                }
            } else {
                return tdlTable(parent, res, lineCount, null);
            }
        } else {
            System.out.println("Errors " + t.errors);
            return createBlock(parent, "paragraph", t.errors.toString(), attributes);
        }
        return null;
    }


    private Block tdlBarChart(StructuralNode parent, TaackResultSetOuterClass.TaackResultSet res, int lineCount, SortedMap<String, String> cols) {
        ArrayList<String> xLabelList = new ArrayList<>();
        Map<String, ArrayList<BigDecimal>> yDataListPerKey = new HashMap<>();
        for (int i = 0; i < lineCount; i++) {
            ArrayList<String> xLabel = new ArrayList<>();
            for (int j = 0; j < res.getColumnsCount(); j++) {
                String columnHeaderName = res.getColumns(j).getName();
                if (cols != null && cols.containsKey(columnHeaderName)) {
                    columnHeaderName = cols.get(columnHeaderName);
                }
                int index = i * res.getColumnsCount() + j;
                switch (res.getColumns(j).getJavaType()) {
                    // combine non-numeral columns, and use the value as xLabel
                    case DATE -> xLabel.add(res.getCells(index).getDateValue() + "");
                    case STRING -> {
                        String cc = res.getCells(index).getStringValue();
                        if (cc.startsWith("<")) {
                            cc = "\n+++\n" + cc + "\n+++\n";
                        }
                        xLabel.add(cc);
                    }
                    case BOOL -> xLabel.add(res.getCells(index).getBoolValue() + "");
                    case BYTE -> xLabel.add(res.getCells(index).getByteValue() + "");
                    case BYTES -> xLabel.add(res.getCells(index).getBytesValue() + "");

                    // use the value of numeral column as yData
                    case LONG -> {
                        if (!yDataListPerKey.containsKey(columnHeaderName)) {
                            yDataListPerKey.put(columnHeaderName, new ArrayList<>());
                        }
                        yDataListPerKey.get(columnHeaderName).add(new BigDecimal(res.getCells(index).getLongValue()));
                    }
                    case BIG_DECIMAL -> {
                        if (!yDataListPerKey.containsKey(columnHeaderName)) {
                            yDataListPerKey.put(columnHeaderName, new ArrayList<>());
                        }
                        yDataListPerKey.get(columnHeaderName).add(new BigDecimal(res.getCells(index).getBigDecimal()));
                    }
                    case SHORT -> {
                        if (!yDataListPerKey.containsKey(columnHeaderName)) {
                            yDataListPerKey.put(columnHeaderName, new ArrayList<>());
                        }
                        yDataListPerKey.get(columnHeaderName).add(new BigDecimal(res.getCells(index).getShortValue()));
                    }
                    case INT -> {
                        if (!yDataListPerKey.containsKey(columnHeaderName)) {
                            yDataListPerKey.put(columnHeaderName, new ArrayList<>());
                        }
                        yDataListPerKey.get(columnHeaderName).add(new BigDecimal(res.getCells(index).getIntValue()));
                    }

                    case UNRECOGNIZED -> System.out.println("|UNRECOGNIZED Column index " + j);
                }
            }
            xLabelList.add(String.join(" ", xLabel));
        }

        Map<String, Map<Object, BigDecimal>> dataPerKey = new HashMap<>();
        yDataListPerKey.forEach((key, yDataList) -> {
            Map<Object, BigDecimal> data = new HashMap<>();
            for (int i = 0; i < xLabelList.size(); i++) {
                data.put(xLabelList.get(i), i < yDataList.size() ? yDataList.get(i) : BigDecimal.ZERO);
            }
            dataPerKey.put(key, data);
        });

        // when the third parameter is TRUE, the diagram width will auto-fit to 100% of section width by doing ZOOM. The zoom rate is "sectionWidth / ${firstParameter}"
        // when the third parameter is FALSE, the diagram width will be fixed to ${firstParameter}
        SvgDiagramRender render = new SvgDiagramRender(new BigDecimal("600.0"), new BigDecimal("300.0"), true);
        BarDiagramScene scene = new BarDiagramScene(render, dataPerKey, true);
        scene.draw();
        return createBlock(parent, "pass", render.getRendered());
    }

    static TDLTranslator translatorFromTdl(String tdl) {
        TDLLexer lexer = new TDLLexer(CharStreams.fromString(tdl));
        System.out.println("AUO lexer " + lexer.getGrammarFileName());
        TaackANTLRErrorListener errors = new TaackANTLRErrorListener();
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
        lexer.addErrorListener(errors);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        System.out.println("AUO tokens " + tokens.getTokens());
        TDLParser parser = new TDLParser(tokens);
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
        parser.addErrorListener(errors);
        System.out.println("AUO parse tokens " + parser.getTokenStream() + "|||" + tokens.getTokens());
        TDLParser.TdlContext tree = parser.tdl();
        System.out.println("AUO tree");
        ParseTreeWalker walker = new ParseTreeWalker();
        TDLTranslator translator = new TDLTranslator(tdl);
        walker.walk(translator, tree);
        System.out.println("AUO walker done");
        return translator;
    }

}
