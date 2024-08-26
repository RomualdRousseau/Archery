# Tutorial 4 - Data extraction with defects

[View source on GitHub](https://github.com/RomualdRousseau/Any2Json-Examples).

This tutoral is a continuation of the [Tutorial 3](tutorial_3.md).

This tutorial will demonstrate how to use [Any2Json](https://github.com/RomualdRousseau/Any2Json) to extract data from
one Excel spreadsheet with defects and its tagging capabilities. Tagging enable to fix a schema for the extracted data
and ease the loading into a database for example. To demonstrate the usage of this framework, we will load a document
with a somewhat complex layout, as seen here:

![document with multiple tables](images/tutorial3_data.png)

## Setup Any2Json

### Import the packages and setup the main class:

```java
package com.github.romualdrousseau.any2json.examples;

import java.util.EnumSet;
import java.util.List;

import com.github.romualdrousseau.any2json.Document;
import com.github.romualdrousseau.any2json.DocumentFactory;
import com.github.romualdrousseau.any2json.parser.LayexTableParser;

public class Tutorial4 implements Runnable {

    public Tutorial4() {
    }

    @Override
    public void run() {
        // Code will come here
    }

    public static void main(final String[] args) {
        new Tutorial4().run();
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

Because we use the tagging capabilities in this tutorial, here are a subset of tags recognized by the base model:

```json
[
  {
    "name" : "tags",
    "doc" : "Tags recognized by sales-english model.",
    "settings" : {
      "types" : [
        "none",
        "date",
        "dateYear",
        "dateMonth",
        "wholesalerCode",
        "wholesalerName",
        "customerCode",
        "customerName",
        "customerType",
        "customerGroup",
        "country",
        "postalCode",
        "adminArea1",
        "adminArea2",
        "adminArea3",
        "adminArea4",
        "locality",
        "address",
        "productCode",
        "productName",
        "amount",
        "unitPrice",
        "quantity",
        "bonusQuantity",
        "returnQuantity",
        "totalQuantity",
        "billToCode",
        "billToName",
        "transactionType",
        "invoiceNumber",
        "invoiceLineNumber",
        "batchNumber",
        "expiryDate",
        "creditReasonCode",
        "requesterName"
      ],
      "requiredTags" : [
        "quantity",
        "productCode,productName"
      ]
    }
  }
]
```

The base model already recognize some entities such as DATE and NUMBER. We will setup the model to add one new entity
PRODUCTNAME and we will configure a layex to extract the different elements of the documents. You can find more details
about layex [here](white_papers.md).

```java
final var model = Common.loadModelFromGitHub("sales-english");

// Add product name entity to the model

model.getEntityList().add("PRODUCTNAME");
model.getPatternMap().put("\\D+\\dml", "PRODUCTNAME");
model.update();

// Add a layex to the model

final var tableParser = new LayexTableParser(
        List.of("(v.$)+"),
        List.of("(()(S+$))(()([/^TOTAL/|v].+$)())+(/TOTAL/.+$)"));
model.registerTableParser(tableParser);
```

### Load the document

We load the document by creating a document instance with the model. The hint "Document.Hint.INTELLI_LAYOUT" tell
the document instance that the document has a complex layout. We also add the hint "Document.Hint.INTELLI_TAG" to tell
that the tabular result must be tagged. The recipe "sheet.setCapillarityThreshold(0)" tell the parser engine to extract
the features as ***small*** as possible:

```java
final var file = Common.loadData("document with defect.xlsx", this.getClass());
try (final var doc = DocumentFactory.createInstance(file, "UTF-8")
        .setModel(model)
        .setHints(EnumSet.of(Document.Hint.INTELLI_LAYOUT, Document.Hint.INTELLI_TAG))
        .setRecipe("sheet.setCapillarityThreshold(0)")) {
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

Note that now we are printing the tags of the headers and not their names.

```bash
2024-03-09 23:54:59 INFO  Common:37 - Loaded resource: /models/sales-english.json
2024-03-09 23:54:59 INFO  Common:37 - Loaded resource: /data/document with defect.xlsx
2024-03-09 23:55:02 DEBUG Common:64 - Extracting features ...
2024-03-09 23:55:02 DEBUG Common:68 - Generating Layout Graph ...
2024-03-09 23:55:02 DEBUG Common:72 - Assembling Tabular Output ...
============================== DUMP GRAPH ===============================
Sheet1
|- A document very important DATE META(1, 1, 4, 1, 1, 1)
|- |- PRODUCTNAME META(1, 4, 1, 4, 1, 1)
|- |- |- Date Client Qty Amount DATA(1, 5, 4, 10, 6, 4) (1)
|- |- PRODUCTNAME META(1, 11, 1, 11, 1, 1)
|- |- |- Date Client Qty Amount DATA(1, 12, 4, 17, 6, 4) (2)
|- |- PRODUCTNAME META(1, 19, 1, 19, 1, 1)
|- |- |- Date Client Qty Amount DATA(1, 20, 4, 25, 6, 4) (3)
================================== END ==================================
2024-03-09 23:55:03.459511: I external/org_tensorflow/tensorflow/cc/saved_model/reader.cc:45] Reading SavedModel from: /tmp/model-9696004103989867291
2024-03-09 23:55:03.461712: I external/org_tensorflow/tensorflow/cc/saved_model/reader.cc:89] Reading meta graph with tags { serve }
2024-03-09 23:55:03.461749: I external/org_tensorflow/tensorflow/cc/saved_model/reader.cc:130] Reading SavedModel debug info (if present) from: /tmp/model-9696004103989867291
2024-03-09 23:55:03.461804: I external/org_tensorflow/tensorflow/core/platform/cpu_feature_guard.cc:193] This TensorFlow binary is optimized with oneAPI Deep Neural Network Library (oneDNN) to use the following CPU instructions in performance-critical operations:  AVX2 FMA
To enable them in other operations, rebuild TensorFlow with the appropriate compiler flags.
2024-03-09 23:55:03.477397: I external/org_tensorflow/tensorflow/compiler/mlir/mlir_graph_optimization_pass.cc:354] MLIR V1 optimization pass is not enabled
2024-03-09 23:55:03.478886: I external/org_tensorflow/tensorflow/cc/saved_model/loader.cc:229] Restoring SavedModel bundle.
2024-03-09 23:55:03.537380: I external/org_tensorflow/tensorflow/cc/saved_model/loader.cc:213] Running initialization op on SavedModel bundle at path: /tmp/model-9696004103989867291
2024-03-09 23:55:03.550411: I external/org_tensorflow/tensorflow/cc/saved_model/loader.cc:305] SavedModel load for tags { serve }; Status: success: OK. Took 90916 microseconds.
2024-03-09 23:55:03 DEBUG Common:77 - Done.
            none                    date             productName            customerName                quantity                  amount
A document very               2023-02-01             Product 1ml                     AAA                       1                     100
A document very               2023-02-01             Product 1ml                     BBB                       1                     100
A document very               2023-02-01             Product 1ml                     BBB                       3                     300
A document very               2023-02-01             Product 1ml                     AAA                       1                     100
A document very               2023-02-01             Product 2ml                     AAA                       1                     100
A document very               2023-02-01             Product 2ml                     BBB                       2                     200
A document very               2023-02-01             Product 2ml                     CCC                       4                     400
A document very               2023-02-01             Product 2ml                     DDD                       1                     100
A document very               2023-02-01             Product 3ml                     AAA                       1                     100
A document very               2023-02-01             Product 3ml                     CCC                       1                     100
A document very               2023-02-01             Product 3ml                     AAA                       1                     100
A document very               2023-02-01             Product 3ml                     DDD                       1                     100
```

On this output, we print out the graph of the document built during the parsing and we can see clearly the relation
between the elements of the spreadsheet and how there are structured in tabular form. Observe how the column names have
been replaced by tags describing the recognized columns.

## Conclusion

Congratulations! You have loaded documents using Any2Json.

For more examples of using Any2Json, check out the [tutorials](index.md).
