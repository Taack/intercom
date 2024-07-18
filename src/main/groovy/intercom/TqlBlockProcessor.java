package intercom;

import groovy.transform.CompileStatic;
import org.asciidoctor.ast.*;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Contexts;
import org.asciidoctor.extension.Name;
import org.asciidoctor.extension.Reader;
import taack.domain.TaackJdbcService;
import taack.jdbc.TaackJdbcError;
import taack.jdbc.common.TaackResultSetOuterClass;
import taack.jdbc.common.tql.listener.TQLTranslator;

import java.util.Map;

@CompileStatic
@Name("tql")
@Contexts({Contexts.LISTING})
@ContentModel(ContentModel.SIMPLE)
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
            TaackResultSetOuterClass.TaackResultSet res = taackJdbcService.protoFromTranslator(t, 20, 0);
            int lineCount = res.getCellsCount() / res.getColumnsCount();

            Table table = createTable(parent);

            Column[] columns = new Column[res.getColumnsCount()];
            Row rh = createTableRow(table);
            for(int i = 0; i < res.getColumnsCount(); i++) {
                columns[i] = createTableColumn(table, i);
                rh.getCells().add(createTableCell(columns[i], res.getColumns(i).getName()));
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
                        case STRING -> c = createTableCell(columns[j], res.getCells(index).getStringValue() + "");
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
        } else {
            System.out.println("Errors " + t.errors);
            return createBlock(parent, "paragraph", t.errors.toString(), attributes);
        }
    }
}
