# Tutorial 5 - Data extraction with pivot

[View source on GitHub](https://github.com/RomualdRousseau/Archery-Examples).

This tutoral is a continuation of the [Tutorial 4](tutorial_4.md).

This tutorial will demonstrate how to use [Archery](https://github.com/RomualdRousseau/Archery) to extract data from
one Excel spreadsheet with pivot. To demonstrate the usage of this framework, we will load a document
with a somewhat complex layout, as seen here:

![document with multiple tables](images/tutorial5_data.png)

## Setup Archery

### Import the packages and setup the main class:

```java
package com.github.romualdrousseau.archery.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.archery.Document;
import com.github.romualdrousseau.archery.DocumentFactory;
import com.github.romualdrousseau.archery.parser.LayexTableParser;

public class Tutorial5 implements Runnable {

    public Tutorial5() {
    }

    @Override
    public void run() {
        // Code will come here
    }

    public static void main(final String[] args) {
        new Tutorial5().run();
    }
}
```

### pom.xml

Archery has a very modular design where each functionality can be loaded separatly. We add the "archery-net-classifier"
module to enable the tagging capabilities. This module use [TensorFlow](https://www.tensorflow.org/) for Java. The
following depedencies are required to run the code of this tutorial:

```xml
<!-- Archery Framework -->
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>archery</artifactId>
    <version>${archery.version}</version>
</dependency>
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>archery-layex-parser</artifactId>
    <version>${archery.version}</version>
</dependency>
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>archery-net-classifier</artifactId>
    <version>${archery.version}</version>
</dependency>
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>archery-csv</artifactId>
    <version>${archery.version}</version>
</dependency>
<dependency>
    <groupId>com.github.romualdrousseau</groupId>
    <artifactId>archery-excel</artifactId>
    <version>${archery.version}</version>
</dependency>
```

## Load base model

To parse a document, Archery needs a model that will contains the parameters required to the parsing. Instead to start
from an empty Model (See [Tutorial 10](tutorial_10.md)), we will start from an existing one and we will adapt it for our
document. You can find a list and details of all models [here](https://github.com/RomualdRousseau/Archery-Models/).

The base model, we will use, is "sales-english" that has been trained on 200+ english documents containing distributor
data and with a large range of different layouts.

The base model already recognize some entities such as DATE and NUMBER. We will setup the model to add one new entity
PRODUCTNAME and we will configure a layex to extract the different elements of the documents. You can find more details
about layex [here](white_papers.md).

```java
final var model = Common.loadModelFromGitHub("sales-english");

// Add product name entity to the model

model.getEntityList().add("PRODUCTNAME");
model.getPatternMap().put("\\D+\\dml", "PRODUCTNAME");
model.getPatternMap().put("(?i)((20|19)\\d{2}-(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)-\\d{2})", "DATE");
model.update();

// Add a layex to the model

final var tableParser = new LayexTableParser(
        List.of("(v.$)+"),
        List.of("(()(S+$S+$))(()([/^TOTAL/|v].+$)())+(/TOTAL/.+$)"));
model.registerTableParser(tableParser);
```

### Load the document

We load the document by creating a document instance with the model. The hint "Document.Hint.INTELLI_LAYOUT" tells
the document instance that the document has a complex layout. We also add the hint "Document.Hint.INTELLI_TAG" to tell
that the tabular result must be tagged. The recipe "sheet.setCapillarityThreshold(0)" tells the parser engine to extract
the features as ***small*** as possible. The recipe "sheet.setPivotOption(\"WITH_TYPE_AND_VALUE\")" tells to manage the
pivot:

```java
final var file = Common.loadData("document with pivot.xlsx", this.getClass());
try (final var doc = DocumentFactory.createInstance(file, "UTF-8")
        .setModel(model)
        .setHints(EnumSet.of(Document.Hint.INTELLI_LAYOUT, Document.Hint.INTELLI_TAG))
        .setRecipe(
                "sheet.setCapillarityThreshold(0)",
                "sheet.setPivotOption(\"WITH_TYPE_AND_VALUE\")",
                "sheet.setPivotTypeFormat(\"%s\")")) {
    ...
}
```

### Output the tabular result

Finally, we iterate over the sheets, rows and cells and output the data on the console:

```java
doc.sheets().forEach(s -> Common.addSheetDebugger(s).getTable().ifPresent(t -> {
    Common.printTags(t.headers());
    Common.printRows(t.rows());
}));
```

```bash
2024-03-11 20:03:41 INFO  Common:42 - Loaded model: sales-english
2024-03-11 20:03:41 INFO  Common:59 - Loaded resource: /data/document with pivot.xlsx
2024-03-11 20:03:44 DEBUG Common:86 - Extracting features ...
2024-03-11 20:03:44 DEBUG Common:90 - Generating Layout Graph ...
2024-03-11 20:03:44 DEBUG Common:94 - Assembling Tabular Output ...
============================== DUMP GRAPH ===============================
Sheet1
|- A document very important DATE META(1, 1, 7, 1, 1, 1)
|- |- PRODUCTNAME META(1, 4, 1, 4, 1, 1)
|- |- |- Client DATE #PIVOT? DATA(1, 5, 7, 11, 7, 4) (1)
|- |- PRODUCTNAME META(1, 12, 1, 12, 1, 1)
|- |- |- Client DATE #PIVOT? DATA(1, 13, 7, 19, 7, 4) (2)
================================== END ==================================
2024-03-11 20:03:44.868213: I external/org_tensorflow/tensorflow/cc/saved_model/reader.cc:45] Reading SavedModel from: /tmp/model-937345648011368689
2024-03-11 20:03:44.870396: I external/org_tensorflow/tensorflow/cc/saved_model/reader.cc:89] Reading meta graph with tags { serve }
2024-03-11 20:03:44.870431: I external/org_tensorflow/tensorflow/cc/saved_model/reader.cc:130] Reading SavedModel debug info (if present) from: /tmp/model-937345648011368689
2024-03-11 20:03:44.870492: I external/org_tensorflow/tensorflow/core/platform/cpu_feature_guard.cc:193] This TensorFlow binary is optimized with oneAPI Deep Neural Network Library (oneDNN) to use the following CPU instructions in performance-critical operations:  AVX2 FMA
To enable them in other operations, rebuild TensorFlow with the appropriate compiler flags.
2024-03-11 20:03:44.895354: I external/org_tensorflow/tensorflow/compiler/mlir/mlir_graph_optimization_pass.cc:354] MLIR V1 optimization pass is not enabled
2024-03-11 20:03:44.897818: I external/org_tensorflow/tensorflow/cc/saved_model/loader.cc:229] Restoring SavedModel bundle.
2024-03-11 20:03:44.978069: I external/org_tensorflow/tensorflow/cc/saved_model/loader.cc:213] Running initialization op on SavedModel bundle at path: /tmp/model-937345648011368689
2024-03-11 20:03:44.997561: I external/org_tensorflow/tensorflow/cc/saved_model/loader.cc:305] SavedModel load for tags { serve }; Status: success: OK. Took 129361 microseconds.
2024-03-11 20:03:45 DEBUG Common:99 - Done.
            none                    date             productName            customerName                    date                  amount                quantity
A document very              2023-Mar-02             Product 1ml                     AAA             2023-Jan-01                     100                       1
A document very              2023-Mar-02             Product 1ml                     AAA             2023-Feb-01                     100                       1
A document very              2023-Mar-02             Product 1ml                     AAA             2023-Mar-02                     100                       1
A document very              2023-Mar-02             Product 1ml                     BBB             2023-Jan-01                     100                       1
A document very              2023-Mar-02             Product 1ml                     BBB             2023-Feb-01                     100                       1
A document very              2023-Mar-02             Product 1ml                     BBB             2023-Mar-02                     100                       1
A document very              2023-Mar-02             Product 1ml                     BBB             2023-Jan-01                     300                       3
A document very              2023-Mar-02             Product 1ml                     BBB             2023-Feb-01                     300                       3
A document very              2023-Mar-02             Product 1ml                     BBB             2023-Mar-02                     300                       3
A document very              2023-Mar-02             Product 1ml                     AAA             2023-Jan-01                     100                       1
A document very              2023-Mar-02             Product 1ml                     AAA             2023-Feb-01                     100                       1
A document very              2023-Mar-02             Product 1ml                     AAA             2023-Mar-02                     100                       1
A document very              2023-Mar-02             Product 2ml                     AAA             2023-Jan-01                     100                       1
A document very              2023-Mar-02             Product 2ml                     AAA             2023-Feb-01                     100                       1
A document very              2023-Mar-02             Product 2ml                     AAA             2023-Mar-02                     100                       1
A document very              2023-Mar-02             Product 2ml                     BBB             2023-Jan-01                     100                       1
A document very              2023-Mar-02             Product 2ml                     BBB             2023-Feb-01                     100                       1
A document very              2023-Mar-02             Product 2ml                     BBB             2023-Mar-02                     100                       1
A document very              2023-Mar-02             Product 2ml                     BBB             2023-Jan-01                     300                       3
A document very              2023-Mar-02             Product 2ml                     BBB             2023-Feb-01                     300                       3
A document very              2023-Mar-02             Product 2ml                     BBB             2023-Mar-02                     300                       3
A document very              2023-Mar-02             Product 2ml                     AAA             2023-Jan-01                     100                       1
A document very              2023-Mar-02             Product 2ml                     AAA             2023-Feb-01                     100                       1
A document very              2023-Mar-02             Product 2ml                     AAA             2023-Mar-02                     100                       1
```

On this output, we print out the graph of the document built during the parsing and we can see clearly the relation
between the elements of the spreadsheet and how there are structured in tabular form. Observe how the date columns has
been unpivoted.

## Conclusion

Congratulations! You have loaded documents using Archery.

For more examples of using Archery, check out the [tutorials](index.md).
