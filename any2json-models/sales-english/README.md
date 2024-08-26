# sales-english

## Training Analytics

| Documents | Mon Records | Max Records | Avg Records | ngram |
|-----------|-------------|-------------|-------------|-------|
|       222 |           8 |     1048575 |        5218 |     0 |

## Entities and Patterns

```json
"entities": [
    "DATE",
    "REFERENCE",
    "PACKAGE",
    "SMALL",
    "NUMBER",
    "TOTAL",
    "PRODUCTCODE"
],
"patterns": [
    {
        "key": "[s|q]\\w{3}\\d{4}",
        "value": "DATE"
    },
    {
        "key": "(20|19)\\d{6}",
        "value": "DATE"
    },
    {
        "key": "(?i)(\\b)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)(([\\w|\\s|,]*(20|19)\\d{2})|\\b)",
        "value": "DATE"
    },
    {
        "key": "\\d{1,2}-\\w{3}-\\d{2,4}",
        "value": "DATE"
    },
    {
        "key": "\\d{1,4}[/-]\\d{1,2}[/-]\\d{1,4}",
        "value": "DATE"
    },
    {
        "key": "^0+\\d+$",
        "value": "REFERENCE"
    },
    {
        "key": "^[\\d\\-]{8,}$",
        "value": "REFERENCE"
    },
    {
        "key": "(\\d[\\d,]*(\\.\\d+)?\\s*[mglptcds'’]+)\\b(?!total\\b)|cream|tablet",
        "value": "PACKAGE"
    },
    {
        "key": "^-?[\\d,]{1,4}(\\.\\d+)?$",
        "value": "SMALL"
    },
    {
        "key": "^-?[\\d,]+(\\.\\d+)?([e|E]-?\\d+)?$",
        "value": "NUMBER"
    },
    {
        "key": "total\\b(?!.*(care|pharmacy).*\\b)",
        "value": "TOTAL"
    },
    {
        "key": "^URSR\\d+$",
        "value": "PRODUCTCODE"
    },
    {
        "key": "^ITEM \\d+$",
        "value": "PRODUCTCODE"
    }
]
```

## Tags

```json
"tags": [
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
    "bonusQuantity",
    "quantity",
    "billToCode",
    "billToName",
    "transactionType",
    "invoiceNumber",
    "invoiceLineNumber",
    "batchNumber",
    "expiryDate",
    "creditReasonCode",
    "requesterName",
    "speciality",
    "detailSpeciality",
    "numberOfBeds",
    "typeOfInsurance",
    "patientTreatmentDays",
    "numberOfPatients"
]
```

## Filters

```json
"filters": [
    "(\\(\\$.*\\))$",
    "[\\\\!\"'#$%&()*+,\\-./:;<=>?@\\[\\]^_`{|}~▼\\t\\n]",
    "\\s+"
]
```