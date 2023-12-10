MR<sup>3</sup>
==========

## What is MR<sup>3</sup> ?
[MR<sup>3</sup>](https://mrcube.org/) (Meta-Model Management based on RDFs Revision Reflection) is an editing tool of RDF-based contents developed for managing a relationship between RDF and RDFS contents.

### Introduction
The Semantic Web is one of the most promised candidates as the Web tomorrow, whose basis is on RDF and RDF Schema recommended by the World Wide Web Consortium. The purpose of the idea is to make the data on the Web available not only for displaying but also for automation, integration and reuse of data across various applications. At the moment, a number of supporting environment have been developed as the adopted tools of traditional knowledge engineering based ontologies. These products are mostly concentrating on creating ontologies and managing ontology-based semantic markup. From the standpoint of a significance of information lifecycle on the Semantic Web, in this work, an editing tool of RDF-based contents is developed for managing a relationship between RDF and RDFS contents.

### Features

MR<sup>3</sup> provides the three main functions to edit and to manage the several sorts of relationship among RDF and RDFS contents as follows.

#### Graphical Editing of RDF Descriptions
Based on the semantics of RDF data model, the tool supports to edit the resource-property-value relation.
#### Graphical Editing of RDFS Descriptions
Based on the semantics of RDF Schema model, the tool supports to edit the class-subclass relation and to add the information of properties such as range, domain, sub-property, and so on.
#### Meta-Model Management Facilities
In order to reflect the change of RDF or RDFS contents on the other, the tool supports the correspondence between them.

The above function consisted of the following sub-functions.

* Importing an RDF(S) document and editing the RDF(S) data model graphically.
* Exporting an RDF(S) data model as an RDF(S) document based on various formats such as Turtle, JSONLD, RDF/XML, and N-Triples.
* Reflecting changes of RDFS class to RDF resource type.
* Reflecting changes of RDFS property to RDF property.
* Reflecting the change of RDF resource type to the RDFS class and domains and ranges of RDFS property.
* Reflecting changes of RDF property to RDFS property.

## Paper
Takeshi Morita, Noriaki Izumi, Naoki Fukuta, Takahira Yamaguchi, “A Graphical RDF-based Meta-Model Management Tool”, IEICE Transactions on Information and Systems, Special Issue on Knowledge-Based Software Engineering Vol.E89-D No.4 pp.1368-1377, (2006), DOI: [10.1093/ietisy/e89-d.4.1368](http://doi.org/10.1093/ietisy/e89-d.4.1368)

```
@article{Morita2006,
  title={A Graphical RDF-based Meta-Model Management Tool},
  author={Takeshi MORITA and Naoki FUKUTA and Noriaki IZUMI and Takahira YAMAGUCHI},
  journal={IEICE Transactions on Information and Systems},
  volume={E89.D},
  number={4},
  pages={1368-1377},
  year={2006},
  doi={10.1093/ietisy/e89-d.4.1368}
}
```

## License
MR<sup>3</sup> is Free Software; you may redistribute it and/or modify it under the terms of the [GNU Generic Public License](http://www.gnu.org/copyleft/gpl.html) as published by the Free Software Foundation; either version 2, or (at your option) any later version. 

## Acknowledgements
MR<sup>3</sup> uses the following libraries.

* [FlatLaf - Flat Look and Feel](https://www.formdev.com/flatlaf/)([License](http://www.apache.org/licenses/LICENSE-2.0))
* [JGraph and JGraphAddons](http://www.jgraph.com/)([License](https://github.com/jgraph/legacy-jgraph5/blob/master/LICENSE)]
* [Apache Jena](https://jena.apache.org/)([License](http://www.apache.org/licenses/LICENSE-2.0))
* [Apache Batik SVG Toolkit](https://xmlgraphics.apache.org/batik/)([License](https://xmlgraphics.apache.org/batik/license.html))
* [Drawing Graphs with VGJ](http://www.eng.auburn.edu/department/cse/research/graph_drawing/graph_drawing.html)([License](http://www.eng.auburn.edu/department/cse/research/graph_drawing/COPYING))
* [Material Design icons by Google](https://github.com/google/material-design-icons)([License](https://www.apache.org/licenses/LICENSE-2.0.txt))

## Contact
* Takeshi Morita (morita [at] it.aoyama.ac.jp)

