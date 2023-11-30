# Any2Json

![Build](https://github.com/RomualdRousseau/Any2Json/workflows/Build/badge.svg)

This is a java API to manipulate semi structured documents and extract data from them.

Semi structured documents are documents with a natural layout and structure, such as scientific papers, excel files, web pages ... Such documents contain structured entities such as numbers, dates, labels but within a format not well-defined. But because of the nature of their content, the document possess an inherent structure linking the different entities given by their layout. For example, a document may contain a title, some texts, some tables and captions. The meaning of the data contained in the tables are explained by the caption, the text around them and the title.

Any2Json helps to parse semi stuctured documents (in different format such as Excel, text, Html ...), walk through its different elements and extract data with their context (or metadata).

Technically, Any2json builds a graph of structured entities and tags them with a given dictionary. By searching data through their tags, Any2Json cano extract structured data.

The construction of the graph use several technics from computer vision to neural network. Computer vision algorithms are used to find area and shapes in the document such as tables, block of text, etc ... Then a neural network will infere the inherent structure of the document to link the entities, weighted by their semantic relations. From the graph, another neural network will tag the entities by doing a NLP anaylis of the text around them.

The biggest part of the API is the inference of table layouts to extract their data. Tables may contains headers, sub headers, pivot columns, footers, captions. A technic close to deep reinforcement learning is used to define each part of the table. The parts of a table are supposed to be a chain of markov where the type of a part is defined by the previous part (header precedes a data row) and the probability sequences store in a neural network.

The tags are based on a word2vec approach (onehot + pca reduction) with a neural network to classify thoe words by the tags.

## Project Documentation

https://romualdrousseau.github.io/Any2Json/

## User Guide

https://github.com/RomualdRousseau/Any2Json/wiki
