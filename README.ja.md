MR<sup>3</sup>
===

## Webサイト
* <https://mr-3.github.io/>

## 概要
MR<sup>3</sup> (Meta-Model Management based on RDFs Revision Reflection) は，次世代Webの候補の1つであるセマンティックWebにおけるRDF (Resource Description Framework)及びRDFS (RDF Schema)の視覚的な編集とそれらの間の関係を管理する機能を持つエディタです．

セマンティックWebを実現するための基盤技術として，計算機が理解可能なメタデータを記述するためのフレームワークであるRDFやオントロジー記述言語RDFS，OWL (Web Ontology Language)の 標準化がW3Cにより行われています．RDFはURIで表現されるリソース相互の関係を主語，述語，目的語のトリプルにより表現するためのデータモデルと 構文を提供しています．また，RDFSではリソースのタイプ (クラス)及びリソース間の関係 (プロパティ)を定義するための語彙 (オントロジー)を提供しています．計算機がメタデータをより明確に理解・推論するためには，RDFSやOWLなどのオントロジー記述言語を用いてクラス 及びプロパティを定義し，それらを用いてインスタンス (RDF)を記述する必要があります．

上記のように，RDFとRDFSはインスタンス (モデル)とオントロジー (モデルの構成要素を定義するメタモデル)という意味的に異なる記述を行います．しかし，RDFSはRDFデータモデル及び構文を用いて記述されるため， RDFとRDFSが混在するコンテンツを扱う際には，両者を区別することはユーザの負担となります．特に，特定の目的のためのメタデータを記述するため に，RDFSとRDFをユーザが定義する初期段階においては，両者の間を双方向に頻繁に編集するため，RDFSとRDFを分離し，両者の整合性を保ちなが ら記述を支援することが重要となります．

MR<sup>3</sup>はRDFSとRDFを分離し，両者の間の整合性を保ちながら視覚的に編集する機能を提供することでユーザの負担を軽減します．

## 特徴
MR<sup>3</sup>には主に以下の3つの特徴があります．

* RDF (インスタンス)の視覚的編集機能
  * RDFデータモデルに基づき，主語，述語，目的語のトリプルを視覚的に編集する機能
* RDFS (オントロジー)の視覚的編集機能
	* RDFSモデルに基づき，クラス及びプロパティの上位・下位関係，プロパティの定義域及び値域の視覚的編集機能
* メタモデル管理機能
	* RDFとRDFSの間の整合性を管理し，変更を双方向に反映させる機能

上記の機能の詳細は以下のようになっています．

* RDFファイルをインポートし，RDFデータモデルを視覚的に表示・編集する機能
* RDFデータグラフを様々なRDF構文 (RDF/XML, N-Triples, N3)に基づいてファイルにエクスポートする機能
* RDFSファイルをインポートし，RDFSデータモデルを視覚的に表示・編集する機能
* RDFSデータグラフを様々なRDF構文 (RDF/XML, N-Triples, N3)に基づいてRDFSファイルにエクスポートする機能
* RDFリソースタイプの変更をRDFSクラス及びRDFSプロパティの定義域及び値域に反映させる機能
* RDFプロパティの変更をRDFSプロパティに反映させる機能
* RDFSクラス及びプロパティの変更をRDFリソースのタイプ及びプロパティに反映させる機能

# 発表論文
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

## ライセンス
MR<sup>3</sup>はフリーソフトウェアです．Free Software Foundation による [GNU Generic Public License](http://www.gnu.org/copyleft/gpl.html) のバージョン2 （または，それ以降のバージョン）に従う限り自由に変更し再配布することができます．

## 利用ライブラリとライセンス
* [FlatLaf - Flat Look and Feel](https://www.formdev.com/flatlaf/)([License](http://www.apache.org/licenses/LICENSE-2.0))
* [JGraph and JGraphAddons](http://www.jgraph.com/)([License](https://github.com/jgraph/legacy-jgraph5/blob/master/LICENSE)]
* [Apache Jena](https://jena.apache.org/)([License](http://www.apache.org/licenses/LICENSE-2.0))
* [Apache Batik SVG Toolkit](https://xmlgraphics.apache.org/batik/)([License](https://xmlgraphics.apache.org/batik/license.html))
* [Drawing Graphs with VGJ](http://www.eng.auburn.edu/department/cse/research/graph_drawing/graph_drawing.html)([License](http://www.eng.auburn.edu/department/cse/research/graph_drawing/COPYING))
* [Material Design icons by Google](https://github.com/google/material-design-icons)([License](https://www.apache.org/licenses/LICENSE-2.0.txt))


## 連絡先
* 森田武史 (morita@it.aoyama.ac.jp)

