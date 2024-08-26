# Tutorial 6 - More complex noise reduction

[View source on GitHub](https://github.com/RomualdRousseau/Any2Json-Examples).

This tutoral is a continuation of the [Tutorial 5](tutorial_5.md).

This tutorial will demonstrate how to use [Any2Json](https://github.com/RomualdRousseau/Any2Json) to extract data from
one Excel spreadsheet. The document is not well-formed and very noisy. To demonstrate the usage of this framework, we
will load a document with a somewhat complex layout, as seen here:

![document with noises](images/tutorial6_data.png)

## Setup Any2Json

### Import the packages and setup the main class:

```java
package com.github.romualdrousseau.any2json.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.parser.LayexTableParser;

public class Tutorial6 implements Runnable {

    public Tutorial6() {
    }

    @Override
    public void run() {
        // Code will come here
    }

    public static void main(final String[] args) {
        new Tutorial6().run();
    }
}
```

### pom.xml

Any2Json has a very modular design where each functionality can be loaded separatly. We add the "any2json-net-classifier"
module to enable the tagging capabilities. This module use [TensorFlow](https://www.tensorflow.org/) for Java. The
following depedencies are required to run the code of this tutorial:

```xml
<!-- ShuJu Framework -->
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>shuju</artifactId>
    <version>${shuju.version}</version>
</dependency>
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>shuju-jackson-json</artifactId>
    <version>${shuju.version}</version>
</dependency>
<!-- Any2Json Framework -->
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>any2json</artifactId>
    <version>${any2json.version}</version>
</dependency>
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>any2json-layex-parser</artifactId>
    <version>${any2json.version}</version>
</dependency>
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>any2json-net-classifier</artifactId>
    <version>${any2json.version}</version>
</dependency>
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>any2json-csv</artifactId>
    <version>${any2json.version}</version>
</dependency>
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>any2json-excel</artifactId>
    <version>${any2json.version}</version>
</dependency>
```

## Load base model

To parse a document, any2Json needs a model that will contains the parameters required to the parsing. Instead to start
from an empty Model (See [Tutorial 10](tutorial_10.md)), we will start from an existing one and we will adapt it for our
document. You can find a list and details of all models [here](https://github.com/RomualdRousseau/Any2Json-Models/).

The base model, we will use, is "sales-english" that has been trained on 200+ english documents containing distributor
data and with a large range of different layouts.

The base model already recognize some entities such as DATE and NUMBER. We will configure a layex to extract the
different elements of the documents. You can find more details about layex [here](white_papers.md).

```java
final var model = Common.loadModelFromGitHub("sales-english");

// Add a layex to the model

final var tableParser = new LayexTableParser(
        List.of("(v.$)+"),
        List.of("(()(E+$E+$))(()(/^PRODUCTCODE/.+$)*(/PRODUCTCODE/.+$))+()"));
model.registerTableParser(tableParser);
```

### Load the document

We load the document by creating a document instance with the model. The hint "Document.Hint.INTELLI_LAYOUT" tells
the document instance that the document has a complex layout. WThe recipe "sheet.setCapillarityThreshold(1.5)" tells the
parser engine to extract the features as ***big*** as possible. The recipe "sheet.setDataTableParserFactory(\"DataTableGroupSubFooterParserFactory\")"
tells to that the footer continas info we are interrested in. Finallym the recipe "sheet.dropRowsWhenFillRatioLessThan(0.2)" 
tells to cleanup almost empty rows:

```java
final var file = Common.loadData("document with noises.xls", this.getClass());
try (final var doc = DocumentFactory.createInstance(file, "UTF-8")
        .setModel(model)
        .setHints(EnumSet.of(Document.Hint.INTELLI_LAYOUT))
        .setRecipe(
            "sheet.setCapillarityThreshold(1.5)",
                "sheet.setDataTableParserFactory(\"DataTableGroupSubFooterParserFactory\")",
                "sheet.dropRowsWhenFillRatioLessThan(0.2)")) {
    ...
}
```

### Output the tabular result

Finally, we iterate over the sheets, rows and cells and output the data on the console:

```java
doc.sheets().forEach(s -> Common.addSheetDebugger(s).getTable().ifPresent(t -> {
    Common.printHeaders(t.headers());
    Common.printRows(t.rows());
}));
```

```bash
2024-03-13 09:51:56 INFO  Common:43 - Loaded model: sales-english
2024-03-13 09:51:56 INFO  Common:60 - Loaded resource: /data/document with noises.xls
2024-03-13 09:51:58 DEBUG Common:87 - Extracting features ...
2024-03-13 09:51:58 DEBUG Common:91 - Generating Layout Graph ...
2024-03-13 09:51:58 DEBUG Common:95 - Assembling Tabular Output ...
============================== DUMP GRAPH ===============================
MSRVTA.rpt
|- Item no. Description Start stock Qty rec. Start +rec. Qty sold Month sales Sampl/exp. Dam./corr. Closing stock Qty on order Ytd qty Ytd sales PRODUCTCODE #GROUP? DATA(0, 0, 11, 18, 19, 17) (1)
================================== END ==================================
2024-03-13 09:51:59 DEBUG Common:100 - Done.
        Item no.             Description             Start stock                Qty rec.             Start +rec.                Qty sold             Month sales        Sampl/exp. Dam./           Closing stock     Qty on order                 Ytd qty               Ytd sales        PRODUCTCODE #GRO
           10255                     AAA                                                                                               3                                                                                                                3                                        URSR0009
           10143                     BBB                                                                                              10                                                                                                               10                                        URSR0014
           10203                     CCC                                                                                               7                                                                                                                7                                        URSR0014
           10209                     DDD                                                                                              16                                                                                                               16                                        URSR0014
           10211                     EEE                                                                                               4                                                                                                                4                                        URSR0014
           10248                     FFF                                                                                              40                                                                                                               40                                        URSR0014
           10197                     BBB                                                                                               8                                                                                                                8                                        URSR0015
           10200                     CCC                                                                                              20                                                                                                               20                                        URSR0015
           10202                     DDD                                                                                               8                                                                                                                8                                        URSR0015
           10248                     EEE                                                                                              14                                                                                                               14                                        URSR0015
           10255                     FFF                                                                                              10                                                                                                               10                                        URSR0015
```

On this output, we print out the graph of the document built during the parsing and we can see clearly the relation
between the elements of the spreadsheet and how there are structured in tabular form.

## Conclusion

Congratulations! You have loaded documents using Any2Json.

For more examples of using Any2Json, check out the [tutorials](index.md).
